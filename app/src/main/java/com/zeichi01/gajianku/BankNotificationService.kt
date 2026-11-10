package com.zeichi01.gajianku

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.zeichi01.gajianku.data.local.AppDatabase
import com.zeichi01.gajianku.data.local.entity.AllocationItem
import com.zeichi01.gajianku.data.local.entity.User
import com.zeichi01.gajianku.data.repository.AllocationRepository
import com.zeichi01.gajianku.data.repository.EmergencyFundRepository
import com.zeichi01.gajianku.data.repository.PresetRepository
import com.zeichi01.gajianku.domain.BankNotificationParser
import com.zeichi01.gajianku.domain.SalaryAllocator
import com.zeichi01.gajianku.domain.SavingsDistributor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BankNotificationService : NotificationListenerService() {

    private val db by lazy { AppDatabase.getInstance(this) }
    private val scope = CoroutineScope(Dispatchers.IO)

    // BARU: Dedupe in-memory untuk mencegah notifikasi yang sama diproses dua kali
    private val seen = LinkedHashMap<String, Long>()

    private fun isDuplicate(packageName: String, text: String): Boolean {
        val key = "$packageName|${text.hashCode()}"
        val now = System.currentTimeMillis()
        synchronized(seen) {
            // Bersihkan cache yang lebih dari 60 detik
            seen.entries.removeIf { now - it.value > 60_000 }
            if (seen.containsKey(key)) return true
            seen[key] = now
        }
        return false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        val packageName = sbn.packageName ?: return
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val fullText = "$title $text"

        Log.d("NotifListener", "PANCINGAN: Service menerima notifikasi dari: $packageName")
        Log.d("NotifListener", "ISI TEKS: $fullText")

        // DIUBAH: whitelist bank, tanpa instagram, tambah DANA
        val validPackages = mapOf(
            "com.bca" to "BCA",
            "com.gojek.gopay" to "GoPay",
            "id.co.bri.brimo" to "BRI",
            "com.bankmandiri.mandiri" to "Mandiri",
            "id.dana" to "DANA",
            "com.android.shell" to "BCA" // Simulasi ADB untuk testing
        )

        // BARU: Cek dedupe sebelum diproses
        if (isDuplicate(packageName, fullText)) {
            Log.d("NotifListener", "DUPLIKAT dilewati: $fullText")
            return
        }

        scope.launch {
            val user = db.userDao().getUserOnce() ?: return@launch
            val senderBank = validPackages[packageName]

            Log.d("NotifListener", "BANK TERDETEKSI: $senderBank | BANK USER: ${user.bankName}")

            if (senderBank != null && senderBank == user.bankName) {
                val isIncoming = BankNotificationParser.isIncomingTransaction(fullText)
                Log.d("NotifListener", "APAKAH TRANSAKSI MASUK? $isIncoming")

                if (isIncoming) {
                    val amount = BankNotificationParser.parseAmount(fullText)
                    Log.d("NotifListener", "NOMINAL TERBACA: $amount")

                    if (amount != null && amount > 0) {
                        Log.d("NotifListener", "Transaksi Masuk: Rp $amount")

                        if (BankNotificationParser.isEarlyMonth()) {
                            Log.d("NotifListener", "GAJI TERDETEKSI! Memproses alokasi...")
                            processSalary(user, amount)
                        } else {
                            Log.d("NotifListener", "Pemasukan lain terdeteksi. Membagi ke Tabungan Darurat & Wajib...")
                            val emergencyShare = amount / 2
                            val savingsShare = amount - emergencyShare

                            val emergencyRepo = EmergencyFundRepository(db.emergencyFundDao())
                            emergencyRepo.addFunds(emergencyShare, "Pemasukan otomatis dari $senderBank")

                            if (savingsShare > 0) {
                                val activeTargets = db.savingsTargetDao().getActiveTargets().first()
                                if (activeTargets.isNotEmpty()) {
                                    val target = activeTargets.first()
                                    val newAmount = target.currentAmount + savingsShare
                                    db.savingsTargetDao().updateAmount(target.id, newAmount)
                                    if (newAmount >= target.targetAmount && target.status == "AKTIF") {
                                        db.savingsTargetDao().updateStatus(target.id, "SIAP_DIBELI")
                                    }
                                    Log.d("NotifListener", "Rp $savingsShare masuk ke target: ${target.name}")
                                } else {
                                    emergencyRepo.addFunds(savingsShare, "Pemasukan otomatis (tidak ada target aktif)")
                                    Log.d("NotifListener", "Tidak ada target aktif, Rp $savingsShare dialihkan ke Darurat.")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIUBAH: processSalary sekarang pakai activePresetId, cek Result, dan hanya update monthlySalary jika berhasil
    private suspend fun processSalary(user: User, amount: Long) {
        val presetRepo = PresetRepository(db.presetDao(), db.categoryDao())
        val allocationRepo = AllocationRepository(db.allocationDao())

        // Ambil preset: prioritas activePresetId, fallback ke default
        val presets = presetRepo.getAllPresets().first()
        val preset = user.activePresetId?.let { id -> presets.firstOrNull { it.id == id } }
            ?: presets.firstOrNull { it.isDefault }
            ?: presets.firstOrNull()
            ?: run {
                Log.w("NotifListener", "Tidak ada preset tersedia, alokasi dibatalkan")
                return
            }

        val presetItems = presetRepo.getPresetItemsOnce(preset.id)
        val categories = presetRepo.getAllCategories().first()

        val allocationResult = SalaryAllocator.allocate(
            salary = amount,
            presetItems = presetItems,
            categories = categories,
            currentDeficit = 0
        )

        val allocationItems = allocationResult.items.map { item ->
            AllocationItem(
                allocationId = 0,
                categoryId = item.categoryId,
                amount = item.amount
            )
        }

        val saved = allocationRepo.saveAllocation(
            month = allocationRepo.getCurrentMonth(),
            salaryAmount = amount,
            presetId = preset.id,
            deficitPaid = allocationResult.deficitPaid,
            items = allocationItems
        )

        // BARU: Jika gagal (bulan ini sudah ada alokasi), berhenti. Jangan lanjut ke target.
        if (saved.isFailure) {
            Log.w("NotifListener", "Alokasi dilewati: ${saved.exceptionOrNull()?.message}")
            return
        }

        // BARU: Update monthlySalary HANYA setelah alokasi berhasil
        db.userDao().update(user.copy(monthlySalary = amount))

        // BARU: Simpan Tabungan Darurat ke EmergencyFund
        if (allocationResult.savingsDaruratAmount > 0) {
            val emergencyRepo = EmergencyFundRepository(db.emergencyFundDao())
            emergencyRepo.addFunds(
                amount = allocationResult.savingsDaruratAmount,
                note = "Alokasi gaji bulan ${allocationRepo.getCurrentMonth()}"
            )
            Log.d("NotifListener", "Tabungan Darurat: Rp ${allocationResult.savingsDaruratAmount} disimpan")
        }

        // Distribusi Tabungan Wajib ke Target
        if (allocationResult.savingsWajibAmount > 0) {
            val activeTargets = db.savingsTargetDao().getActiveTargets().first()

            if (activeTargets.isNotEmpty()) {
                val distribution = SavingsDistributor.distribute(
                    availableAmount = allocationResult.savingsWajibAmount,
                    targets = activeTargets
                )

                distribution.allocations.forEach { (targetId, allocatedAmount) ->
                    if (allocatedAmount > 0) {
                        val target = activeTargets.find { it.id == targetId }
                        if (target != null) {
                            val newAmount = target.currentAmount + allocatedAmount
                            db.savingsTargetDao().updateAmount(target.id, newAmount)
                            if (newAmount >= target.targetAmount && target.status == "AKTIF") {
                                db.savingsTargetDao().updateStatus(target.id, "SIAP_DIBELI")
                            }
                            Log.d("NotifListener", "Distribusi Gaji: Rp $allocatedAmount masuk ke target ${target.name}")
                        }
                    }
                }
            } else {
                Log.d("NotifListener", "Gaji masuk, tapi belum ada target tabungan aktif.")
            }
        }
    }
}