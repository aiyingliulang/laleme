package com.example.lifeassets.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import org.junit.Test;

public class DateMathJavaTest {
    private final LocalDate today = LocalDate.of(2026, 9, 11);

    @Test
    public void purchaseDayCountsAsTheFirstDay() {
        assertEquals(1L, DateMathKt.daysHeld(today, today));
        assertEquals(11L, DateMathKt.daysHeld(LocalDate.of(2026, 9, 1), today));
    }

    @Test
    public void remainingDaysNeverBecomesNegative() {
        assertEquals(0L, DateMathKt.remainingDays(LocalDate.of(2026, 9, 1), today));
        assertEquals(20L, DateMathKt.remainingDays(LocalDate.of(2026, 10, 1), today));
    }

    @Test
    public void lifecycleProgressUsesHeldAndRemainingDays() {
        float progress = DateMathKt.lifecycleProgress(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 21),
                today
        );
        assertTrue(progress >= 0.5f && progress <= 0.6f);
    }
}
