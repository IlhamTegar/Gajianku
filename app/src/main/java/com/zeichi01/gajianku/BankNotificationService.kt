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

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        val packageName = sbn.packageName ?: return
        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getString(Notification.EXTRA_TEXT) ?: ""
        val fullText = "$title $text"

        // LOG BARU: Cek package apa yang masuk
        Log.d("NotifListener", "PANCINGAN: Service menerima notifikasi dari: $packageName")
        // LOG BARU: Cek isi teks notifikasinya
        Log.d("NotifListener", "ISI TEKS: $fullText")

        // Daftar package bank
        val validPackages = mapOf(
            "com.bca" to "BCA",
            "com.instagram.android" to "BCA",
            "com.android.shell" to "BCA", // <-- Pastikan ini ada
            "com.gojek.gopay" to "GoPay",
            "id.co.bri.brimo" to "BRI",
            "com.bankmandiri.mandiri" to "Mandiri"
        )

        scope.launch {
            val user = db.userDao().getUserOnce() ?: return@launch
            val senderBank = validPackages[packageName]

            // LOG BARU: Cek apakah bank dikenali dan cocok dengan pilihan user
            Log.d("NotifListener", "BANK TERDETEKSI: $senderBank | BANK USER: ${user.bankName}")

            // Cek apakah notifikasi dari Bank yang dipilih user
            if (senderBank != null && senderBank == user.bankName) {

                // LOG BARU: Cek apakah dianggap transaksi masuk
                val isIncoming = BankNotificationParser.isIncomingTransaction(fullText)
                Log.d("NotifListener", "APAKAH TRANSAKSI MASUK? $isIncoming")

                if (isIncoming) {
                    val amount = BankNotificationParser.parseAmount(fullText)

                    // LOG BARU: Cek berapa nominal yang berhasil di-parse
                    Log.d("NotifListener", "NOMINAL TERBACA: $amount")

                    if (amount != null && amount > 0) {
                        Log.d("NotifListener", "Transaksi Masuk: Rp $amount")

                        if (BankNotificationParser.isEarlyMonth()) {
                            Log.d("NotifListener", "GAJI TERDETEKSI! Memproses alokasi...")
                            processSalary(user, amount)
                        } else {
                            // --- KASUS 2: PEMASUKAN LAIN (Tanggal > 10) ---
                            Log.d("NotifListener", "Pemasukan lain terdeteksi. Membagi ke Tabungan Darurat & Wajib...")

                            // 1. Bagi 50:50 (bisa diubah nanti)
                            val emergencyShare = amount / 2
                            val savingsShare = amount - emergencyShare

                            // 2. Masukkan ke Tabungan Darurat
                            val emergencyRepo = EmergencyFundRepository(db.emergencyFundDao())
                            emergencyRepo.addFunds(emergencyShare, "Pemasukan otomatis dari $senderBank")

                            // 3. Masukkan ke Tabungan Wajib (Target)
                            if (savingsShare > 0) {
                                // Cari target aktif yang prioritasnya TINGGI untuk dimasukkan uangnya
                                val activeTargets = db.savingsTargetDao().getActiveTargets().first()

                                if (activeTargets.isNotEmpty()) {
                                    // Ambil target pertama (yang paling prioritas)
                                    val target = activeTargets.first()
                                    val newAmount = target.currentAmount + savingsShare

                                    // Simpan ke target
                                    db.savingsTargetDao().updateAmount(target.id, newAmount)

                                    // Cek apakah target jadi terpenuhi
                                    if (newAmount >= target.targetAmount && target.status == "AKTIF") {
                                        db.savingsTargetDao().updateStatus(target.id, "SIAP_DIBELI")
                                    }
                                    Log.d("NotifListener", "Rp $savingsShare masuk ke target: ${target.name}")
                                } else {
                                    // Kalau tidak ada target aktif, semuanya masuk ke Darurat
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

    private suspend fun processSalary(user: User, amount: Long) {
        // 1. Update gaji user
        db.userDao().update(user.copy(monthlySalary = amount))

        // 2. Ambil preset default & kategori
        val presetRepo = PresetRepository(db.presetDao(), db.categoryDao())
        val allocationRepo = AllocationRepository(db.allocationDao())

        val presets = presetRepo.getAllPresets().first()
        val defaultPreset = presets.firstOrNull { it.isDefault } ?: presets.firstOrNull() ?: return

        val presetItems = presetRepo.getPresetItemsOnce(defaultPreset.id)
        val categories = presetRepo.getAllCategories().first()

        // 3. Hitung Alokasi
        val allocationResult = SalaryAllocator.allocate(
            salary = amount,
            presetItems = presetItems,
            categories = categories,
            currentDeficit = 0
        )

        // 4. Simpan Alokasi ke Database
        val allocationItems = allocationResult.items.map { item ->
            AllocationItem(
                allocationId = 0,
                categoryId = item.categoryId,
                amount = item.amount
            )
        }

        allocationRepo.saveAllocation(
            month = allocationRepo.getCurrentMonth(),
            salaryAmount = amount,
            presetId = defaultPreset.id,
            deficitPaid = allocationResult.deficitPaid,
            items = allocationItems
        )

        // --- 5. LOGIKA BARU: DISTRIBUSI TABUNGAN WAJIB KE TARGET ---
        // Jika ada uang di Tabungan Wajib (setelah dipotong minus)
        if (allocationResult.savingsWajibAmount > 0) {
            val activeTargets = db.savingsTargetDao().getActiveTargets().first()

            if (activeTargets.isNotEmpty()) {
                // Gunakan SavingsDistributor untuk membagi uang ke target
                val distribution = SavingsDistributor.distribute(
                    availableAmount = allocationResult.savingsWajibAmount,
                    targets = activeTargets
                )

                // Simpan hasil distribusi ke database
                distribution.allocations.forEach { (targetId, allocatedAmount) ->
                    if (allocatedAmount > 0) {
                        val target = activeTargets.find { it.id == targetId }
                        if (target != null) {
                            val newAmount = target.currentAmount + allocatedAmount
                            db.savingsTargetDao().updateAmount(target.id, newAmount)

                            // Cek apakah target jadi terpenuhi
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