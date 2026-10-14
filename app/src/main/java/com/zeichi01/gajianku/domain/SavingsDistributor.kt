package com.zeichi01.gajianku.domain

import com.zeichi01.gajianku.data.local.entity.SavingsTarget

/**
 * Hasil distribusi ke target.
 */
data class DistributionResult(
    val allocations: Map<Long, Long>,  // targetId → nominal
    val totalAllocated: Long,
    val remaining: Long                 // sisa uang (kalau ada)
)

object SavingsDistributor {

    private val PRIORITY_ORDER = mapOf(
        "TINGGI" to 1,
        "SEDANG" to 2,
        "RENDAH" to 3
    )

    /**
     * Bagi uang tabungan wajib ke target berdasarkan prioritas.
     *
     * Aturan:
     * - Level TINGGI diisi dulu sampai penuh
     * - Kalau uang tidak cukup di satu level, dibagi PROPORSIONAL ke semua target di level itu
     * - Tidak "bocor" ke level bawah kalau level atas belum penuh
     *
     * @param availableAmount Uang Tabungan Wajib yang tersedia
     * @param targets Daftar target aktif (status AKTIF)
     * @return DistributionResult — berapa untuk tiap target
     */
    fun distribute(
        availableAmount: Long,
        targets: List<SavingsTarget>
    ): DistributionResult {
        if (availableAmount <= 0 || targets.isEmpty()) {
            return DistributionResult(
                allocations = emptyMap(),
                totalAllocated = 0,
                remaining = availableAmount.coerceAtLeast(0)
            )
        }

        // Kelompokkan target berdasarkan prioritas
        val byPriority = targets
            .filter { it.status == "AKTIF" }
            .groupBy { it.priority }

        val allocations = mutableMapOf<Long, Long>()
        var remaining = availableAmount

        // Proses level per level: TINGGI → SEDANG → RENDAH
        for (level in listOf("TINGGI", "SEDANG", "RENDAH")) {
            if (remaining <= 0) break

            val levelTargets = byPriority[level] ?: continue
            if (levelTargets.isEmpty()) continue

            // Hitung kebutuhan tiap target di level ini
            val needs = levelTargets.associate { target ->
                target.id to (target.targetAmount - target.currentAmount).coerceAtLeast(0)
            }

            val totalNeed = needs.values.sum()

            if (totalNeed <= 0) continue

            if (remaining >= totalNeed) {
                // Uang cukup untuk semua target di level ini
                for (target in levelTargets) {
                    val need = needs[target.id] ?: 0
                    allocations[target.id] = need
                    remaining -= need
                }
            } else {
                // Uang tidak cukup — bagi PROPORSIONAL
                var totalGiven = 0L
                for (target in levelTargets) {
                    val need = needs[target.id] ?: 0
                    val share = (remaining.toDouble() * need / totalNeed).toLong()
                    allocations[target.id] = share
                    totalGiven += share
                }

                // Sisa pembulatan → berikan ke target pertama
                val rounding = remaining - totalGiven
                if (rounding > 0) {
                    val firstTargetId = levelTargets.first().id
                    allocations[firstTargetId] = (allocations[firstTargetId] ?: 0) + rounding
                }

                remaining = 0
                break
            }
        }

        return DistributionResult(
            allocations = allocations,
            totalAllocated = allocations.values.sum(),
            remaining = remaining
        )
    }
}