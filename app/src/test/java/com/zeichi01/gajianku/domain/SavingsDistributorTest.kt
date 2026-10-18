package com.zeichi01.gajianku.domain

import com.zeichi01.gajianku.data.local.entity.SavingsTarget
import org.junit.Assert.assertEquals
import org.junit.Test

class SavingsDistributorTest {

    @Test
    fun `test bagi uang ke target prioritas tinggi sampai penuh`() {
        // 1. Siapkan data target
        val targets = listOf(
            SavingsTarget(id = 1, name = "Laptop", targetAmount = 1_000_000, currentAmount = 0, priority = "TINGGI", status = "AKTIF"),
            SavingsTarget(id = 2, name = "Motor", targetAmount = 2_000_000, currentAmount = 0, priority = "TINGGI", status = "AKTIF"),
            SavingsTarget(id = 3, name = "Liburan", targetAmount = 3_000_000, currentAmount = 0, priority = "SEDANG", status = "AKTIF")
        )

        // 2. Jalankan logic
        // Uang yang tersedia 2.5 juta. (Butuh 3 juta untuk penuhi semua target TINGGI)
        val result = SavingsDistributor.distribute(availableAmount = 2_500_000, targets = targets)

        // 3. Uji hasilnya (Assertion)
        // Karena uang tidak cukup untuk semua TINGGI, maka dibagi proporsional:
        // Laptop butuh 1jt, Motor butuh 2jt. Total butuh 3jt.
        // Laptop dapat: (1.000.000 / 3.000.000) * 2.500.000 = 833.333
        // Motor dapat: (2.000.000 / 3.000.000) * 2.500.000 = 1.666.667

        // Uji apakah target TINGGI dapat bagian
        assertEquals(833_334L, result.allocations[1L])
        assertEquals(1_666_666L, result.allocations[2L])

        // Uji apakah target SEDANG (Liburan) dapat 0 (karena uang habis di TINGGI)
        assertEquals(0L, result.allocations[3L] ?: 0L)

        // Uji total alokasi = uang tersedia
        assertEquals(2_500_000L, result.totalAllocated)
        assertEquals(0L, result.remaining)
    }

    @Test
    fun `test bagi uang ke semua level jika uang sangat cukup`() {
        val targets = listOf(
            SavingsTarget(id = 1, name = "Laptop", targetAmount = 1_000_000, currentAmount = 0, priority = "TINGGI", status = "AKTIF"),
            SavingsTarget(id = 2, name = "Liburan", targetAmount = 1_000_000, currentAmount = 0, priority = "SEDANG", status = "AKTIF")
        )

        // Uang tersedia 5 juta (sangat cukup)
        val result = SavingsDistributor.distribute(availableAmount = 5_000_000, targets = targets)

        // Keduanya harus dapat penuh
        assertEquals(1_000_000L, result.allocations[1L])
        assertEquals(1_000_000L, result.allocations[2L])

        // Sisa uang harus 3 juta
        assertEquals(3_000_000L, result.remaining)
    }
}