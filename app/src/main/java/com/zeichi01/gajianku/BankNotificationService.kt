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

        // Daftar package bank (Bisa kamu tambah sendiri sesuai kebutuhan)
        val validPackages = mapOf(
            "com.bca" to "BCA",
            "com.instagram.android" to "BCA",
            "com.gojek.gopay" to "GoPay",
            "id.co.bri.brimo" to "BRI",
            "com.bankmandiri.mandiri" to "Mandiri"
        )

        scope.launch {
            val user = db.userDao().getUserOnce() ?: return@launch
            val senderBank = validPackages[packageName]

            // Cek apakah notifikasi dari Bank yang dipilih user
            if (senderBank != null && senderBank == user.bankName) {

                if (BankNotificationParser.isIncomingTransaction(fullText)) {
                    val amount = BankNotificationParser.parseAmount(fullText)

                    if (amount != null && amount > 0) {
                        Log.d("NotifListener", "Transaksi Masuk: Rp $amount")

                        if (BankNotificationParser.isEarlyMonth()) {
                            // --- KASUS 1: GAJI MASUK (Tanggal 1-10) ---
                            Log.d("NotifListener", "GAJI TERDETEKSI! Memproses alokasi...")
                            processSalary(user, amount)
                        } else {
                            // --- KASUS 2: PEMASUKAN LAIN (Tanggal > 10) ---
                            Log.d("NotifListener", "Pemasukan lain. Masuk ke Tabungan Darurat.")
                            val emergencyRepo = EmergencyFundRepository(db.emergencyFundDao())
                            emergencyRepo.addFunds(amount, "Pemasukan otomatis dari $senderBank")
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
            currentDeficit = 0 // (Nanti kita ambil dari DB jika ada minus)
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
    }
}