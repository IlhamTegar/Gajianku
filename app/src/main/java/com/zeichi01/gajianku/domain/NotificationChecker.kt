package com.zeichi01.gajianku.domain

import com.zeichi01.gajianku.data.local.entity.SavingsTarget

data class NotificationDecision(
    val shouldNotify: Boolean,
    val targetId: Long?,
    val targetName: String?,
    val priority: String?,
    val title: String,
    val message: String
)

object NotificationChecker {

    /**
     * Cek apakah target yang baru tercapai layak dikirim notifikasi.
     *
     * Aturan (Tafsiran B):
     * - TINGGI: selalu muncul
     * - SEDANG: muncul kalau TIDAK ADA target Tinggi yang tertunda
     *   (belum tercapai ATAU sudah tercapai tapi belum dibeli)
     * - RENDAH: muncul kalau TIDAK ADA target Tinggi & Sedang yang tertunda
     *
     * @param achievedTarget Target yang baru tercapai
     * @param allActiveTargets Semua target aktif (untuk cek level atas)
     */
    fun check(
        achievedTarget: SavingsTarget,
        allActiveTargets: List<SavingsTarget>
    ): NotificationDecision {
        val priority = achievedTarget.priority

        // Cek "tertunda" di level atas
        val higherPriorities = when (priority) {
            "RENDAH" -> listOf("TINGGI", "SEDANG")
            "SEDANG" -> listOf("TINGGI")
            else -> emptyList()  // TINGGI selalu boleh
        }

        val hasHigherPending = allActiveTargets.any { target ->
            target.priority in higherPriorities &&
                    target.id != achievedTarget.id &&
                    isPending(target)
        }

        if (hasHigherPending) {
            return NotificationDecision(
                shouldNotify = false,
                targetId = achievedTarget.id,
                targetName = achievedTarget.name,
                priority = priority,
                title = "",
                message = ""
            )
        }

        // Tentukan tone berdasarkan prioritas
        val (emoji, title) = when (priority) {
            "TINGGI" -> "🎯" to "Target Prioritas Tinggi Tercapai!"
            "SEDANG" -> "✅" to "Target Tercapai!"
            else -> "💡" to "Target Tercapai"
        }

        return NotificationDecision(
            shouldNotify = true,
            targetId = achievedTarget.id,
            targetName = achievedTarget.name,
            priority = priority,
            title = "$emoji $title",
            message = "${achievedTarget.name} siap dibeli. Terkumpul Rp ${formatRupiah(achievedTarget.currentAmount)} dari target Rp ${formatRupiah(achievedTarget.targetAmount)}."
        )
    }

    /**
     * Target dianggap "tertunda" kalau:
     * - Belum tercapai (currentAmount < targetAmount), ATAU
     * - Sudah tercapai tapi belum dibeli (status SIAP_DIBELI)
     */
    private fun isPending(target: SavingsTarget): Boolean {
        return when (target.status) {
            "AKTIF" -> target.currentAmount < target.targetAmount
            "SIAP_DIBELI" -> true  // sudah tercapai tapi belum dibeli
            else -> false  // SELESAI
        }
    }

    private fun formatRupiah(amount: Long): String {
        return "%,d".format(amount).replace(',', '.')
    }
}