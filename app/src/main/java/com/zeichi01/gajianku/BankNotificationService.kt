package com.zeichi01.gajianku

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.zeichi01.gajianku.data.local.AppDatabase
import com.zeichi01.gajianku.domain.BankNotificationParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BankNotificationService : NotificationListenerService() {

    private val db by lazy { AppDatabase.getInstance(this) }
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        // 1. Ambil package name aplikasi yang mengirim notifikasi
        val packageName = sbn.packageName ?: return

        // 2. Ambil isi teks notifikasi
        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getString(Notification.EXTRA_TEXT) ?: ""
        val fullText = "$title $text"

        // 3. Ambil data user dari database untuk mengecek Bank pilihannya
        scope.launch {
            val user = db.userDao().getUserOnce() ?: return@launch

            // Daftar package bank (contoh, nanti bisa dikembangkan)
            val validPackages = mapOf(
                "com.bca" to "BCA",
                "com.gojek.gopay" to "GoPay",
                "id.co.bri.brimo" to "BRI"
                // Nanti tambahkan package lain sesuai kebutuhan
            )

            // Cek apakah notifikasi ini dari bank yang sesuai pilihan user
            // (Asumsi: kita menyimpan nama bank di user.bankName, misal "BCA")
            val senderBank = validPackages[packageName]

            if (senderBank != null && senderBank == user.bankName) {
                Log.d("NotifListener", "Notif Bank Terdeteksi: $fullText")

                // Cek apakah ini transaksi masuk
                if (BankNotificationParser.isIncomingTransaction(fullText)) {
                    val amount = BankNotificationParser.parseAmount(fullText)

                    if (amount != null) {
                        // Cek apakah ini awal bulan (Gaji) atau bukan
                        if (BankNotificationParser.isEarlyMonth()) {
                            Log.d("NotifListener", "GAJI TERDETEKSI: Rp $amount")
                            // TODO: Simpan sebagai gaji dan picu alokasi
                        } else {
                            Log.d("NotifListener", "PEMASUKAN LAIN: Rp $amount (Masuk ke Tabungan)")
                            // TODO: Masukkan ke tabungan
                        }
                    }
                }
            }
        }
    }
}