package com.zeichi01.gajianku.domain

object BankNotificationParser {
    fun parseAmount(text: String): Long? {
        val cleanText = text.replace("_", " ").replace("-", " ")
        val regex = Regex("""(?:Rp|IDR)\.?\s?([\d.,]+)""", RegexOption.IGNORE_CASE)
        val match = regex.find(cleanText) ?: return null
        var amountStr = match.groupValues[1]
        amountStr = amountStr.replace(".", "").replace(",", ".")
        return amountStr.toDoubleOrNull()?.toLong()
    }

    fun isIncomingTransaction(text: String): Boolean {
        val lowerText = text.lowercase()
        return lowerText.contains("kredit") ||
                lowerText.contains("masuk") ||
                lowerText.contains("menerima") ||
                lowerText.contains("diterima") ||
                lowerText.contains("top up berhasil")
    }

    fun isEarlyMonth(): Boolean {
        val calendar = java.util.Calendar.getInstance()
        val dayOfMonth = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        return dayOfMonth in 1..10
    }
}