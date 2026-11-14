package com.example.lifeassets.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DateMathTest {
    private val today = LocalDate.of(2026, 9, 11)

    @Test
    fun purchaseDayCountsAsTheFirstDay() {
        assertEquals(1, daysHeld(today, today))
        assertEquals(11, daysHeld(LocalDate.of(2026, 9, 1), today))
    }

    @Test
    fun remainingDaysNeverBecomesNegative() {
        assertEquals(0, remainingDays(LocalDate.of(2026, 9, 1), today))
        assertEquals(20, remainingDays(LocalDate.of(2026, 10, 1), today))
    }

    @Test
    fun lifecycleProgressUsesHeldAndRemainingDays() {
        val progress = lifecycleProgress(
            LocalDate.of(2026, 9, 1),
            LocalDate.of(2026, 9, 21),
            today
        )
        assertTrue(progress in 0.5f..0.6f)
    }
}
