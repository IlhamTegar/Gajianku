package com.zeichi01.gajianku.domain

import android.util.Log
import com.zeichi01.gajianku.data.local.entity.SavingsTarget

data class DistributionResult(
    val allocations: Map<Long, Long>,
    val totalAllocated: Long,
    val remaining: Long
)

object SavingsDistributor {

    private val PRIORITY_ORDER = mapOf("TINGGI" to 1, "SEDANG" to 2, "RENDAH" to 3)

    fun distribute(
        availableAmount: Long,
        targets: List<SavingsTarget>
    ): DistributionResult {
        if (availableAmount <= 0 || targets.isEmpty()) {
            return DistributionResult(emptyMap(), 0, availableAmount.coerceAtLeast(0))
        }

        // 1. Filter target AKTIF dan urutkan: prioritas dulu, lalu sortOrder
        val sortedTargets = targets
            .filter { it.status == "AKTIF" }
            .sortedWith(
                compareBy(
                    { PRIORITY_ORDER[it.priority] ?: 99 },
                    { it.sortOrder },
                    { it.createdAt }
                )
            )

        Log.d("SavingsDistributor", "Sorted targets: ${sortedTargets.map { "${it.name} (${it.priority}, sortOrder=${it.sortOrder}, need=${it.targetAmount - it.currentAmount})" }}")
        Log.d("SavingsDistributor", "Available: $availableAmount")

        val allocations = mutableMapOf<Long, Long>()
        var remaining = availableAmount

        // 2. Proses per level prioritas
        val byPriority = sortedTargets.groupBy { it.priority }
        for (priority in listOf("TINGGI", "SEDANG", "RENDAH")) {
            if (remaining <= 0) break
            val levelTargets = byPriority[priority] ?: continue
            if (levelTargets.isEmpty()) continue

            // 3. Hitung kebutuhan dan bobot per target
            val needs = levelTargets.associate { it.id to (it.targetAmount - it.currentAmount).coerceAtLeast(0) }
            val totalNeed = needs.values.sum()
            if (totalNeed <= 0) continue

            // 4. Jika uang cukup untuk semua di level ini, isi penuh
            if (remaining >= totalNeed) {
                for (target in levelTargets) {
                    val need = needs[target.id] ?: 0
                    allocations[target.id] = need
                    remaining -= need
                }
            } else {
                // 5. Jika tidak cukup, bagi proporsional dengan bobot urutan
                // Bobot: urutan 1 = 3x, urutan 2 = 2x, urutan 3+ = 1x
                val weights = levelTargets.mapIndexed { index, t ->
                    t.id to when (index) {
                        0 -> 3.0
                        1 -> 2.0
                        else -> 1.0
                    }
                }.toMap()

                // Hitung nilai tertimbang
                val weightedValues = levelTargets.associate { t ->
                    val need = needs[t.id] ?: 0L
                    val weight = weights[t.id] ?: 1.0
                    t.id to (need * weight)
                }
                val totalWeighted = weightedValues.values.sum()

                if (totalWeighted <= 0) continue

                // Bagi proporsional berdasarkan nilai tertimbang
                var totalGiven = 0L
                for (target in levelTargets) {
                    val weighted = weightedValues[target.id] ?: 0.0
                    val share = (remaining.toDouble() * weighted / totalWeighted).toLong()
                    allocations[target.id] = share
                    totalGiven += share
                }

                Log.d("SavingsDistributor", "Allocations: $allocations")

                // Sisa pembulatan → berikan ke target pertama (urutan 1)
                val rounding = remaining - totalGiven
                if (rounding > 0) {
                    val firstId = levelTargets.first().id
                    allocations[firstId] = (allocations[firstId] ?: 0) + rounding
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