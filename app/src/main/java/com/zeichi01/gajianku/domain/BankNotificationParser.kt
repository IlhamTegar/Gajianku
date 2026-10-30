package com.zeichi01.gajianku.domain

object BankNotificationParser {

    /**
     * Mencoba mengekstrak nominal uang dari teks notifikasi.
     * Contoh teks BCA: "Transaksi kredit Rp 5.000.000. Saldo Rp 10.000.000"
     * Contoh teks GoPay: "Kamu menerima Rp 50.000 dari Budi"
     */
    fun parseAmount(text: String): Long? {
        // 1. Bersihkan teks dari karakter aneh (_ atau -) yang bisa mengganggu
        val cleanText = text.replace("_", " ").replace("-", " ")

        // 2. Regex mencari "Rp" diikuti angka (mendukung spasi, titik, koma)
        val regex = Regex("""(?:Rp|IDR)\.?\s?([\d.,]+)""", RegexOption.IGNORE_CASE)
        val match = regex.find(cleanText) ?: return null

        // 3. Ambil angkanya saja
        var amountStr = match.groupValues[1]

        // 4. Bersihkan titik atau koma
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