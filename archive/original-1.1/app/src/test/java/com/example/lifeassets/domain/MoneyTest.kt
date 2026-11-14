package com.example.lifeassets.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyTest {
    @Test
    fun parsesChineseCurrencyInputToCents() {
        assertEquals(123456L, parseYuanToCents("1,234.56"))
        assertEquals(100L, parseYuanToCents("1"))
        assertEquals(null, parseYuanToCents("not-money"))
    }

    @Test
    fun dailyCostRoundsToNearestCent() {
        assertEquals("¥1,449.28", formatDailyCost(30_000_000L, 207))
        assertEquals("¥29.91", formatDailyCost(379_900L, 127))
        assertEquals("¥0.00", formatDailyCost(0L, 10))
    }
}
