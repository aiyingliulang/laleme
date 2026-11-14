package com.example.lifeassets.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MoneyJavaTest {
    @Test
    public void parsesCurrencyInputToCents() {
        assertEquals(Long.valueOf(123456L), MoneyKt.parseYuanToCents("1,234.56"));
        assertEquals(Long.valueOf(100L), MoneyKt.parseYuanToCents("1"));
        assertEquals(null, MoneyKt.parseYuanToCents("not-money"));
    }

    @Test
    public void dailyCostRoundsToNearestCent() {
        assertEquals("¥1,449.28", MoneyKt.formatDailyCost(30000000L, 207L));
        assertEquals("¥29.91", MoneyKt.formatDailyCost(379900L, 127L));
        assertEquals("¥0.00", MoneyKt.formatDailyCost(0L, 10L));
    }
}
