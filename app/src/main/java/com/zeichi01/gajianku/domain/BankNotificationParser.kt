package com.zeichi01.gajianku.domain

object BankNotificationParser {

    /**
     * Mencoba mengekstrak nominal uang dari teks notifikasi.
     * Contoh teks BCA: "Transaksi kredit Rp 5.000.000. Saldo Rp 10.000.000"
     * Contoh teks GoPay: "Kamu menerima Rp 50.000 dari Budi"
     */
    fun parseAmount(text: String): Long? {
        // Regex untuk mencari format "Rp 5.000.000", "IDR 50,000", atau "Rp50000"
        val regex = Regex("""(?:Rp|IDR)\.?\s?([\d.,]+)""", RegexOption.IGNORE_CASE)
        val match = regex.find(text) ?: return null

        // Ambil angkanya saja (misal: "5.000.000" atau "50,000")
        var amountStr = match.groupValues[1]

        // Bersihkan titik atau koma (tergantung format bank)
        // Asumsi: titik adalah pemisah ribuan, koma adalah desimal (standar Indonesia)
        amountStr = amountStr.replace(".", "").replace(",", ".")

        return amountStr.toDoubleOrNull()?.toLong()
    }

    /**
     * Cek apakah teks notifikasi ini menandakan uang MASUK.
     */
    fun isIncomingTransaction(text: String): Boolean {
        val lowerText = text.lowercase()
        return lowerText.contains("kredit") ||
                lowerText.contains("masuk") ||
                lowerText.contains("menerima") ||
                lowerText.contains("diterima") ||
                lowerText.contains("top up berhasil")
    }

    /**
     * Cek apakah ini tanggal muda (1 - 10).
     */
    fun isEarlyMonth(): Boolean {
        val calendar = java.util.Calendar.getInstance()
        val dayOfMonth = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        return dayOfMonth in 1..10
    }
}