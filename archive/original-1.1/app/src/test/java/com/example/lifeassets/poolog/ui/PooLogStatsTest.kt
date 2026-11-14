package com.example.lifeassets.poolog.ui

import com.example.lifeassets.poolog.data.ToiletVisitEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class PooLogStatsTest {
    private val utc: ZoneId = ZoneId.of("UTC")

    @Test
    fun countsByDateUsesTheSuppliedZoneAtAMidnightBoundary() {
        val visits = listOf(
            ToiletVisitEntity(
                id = "boundary",
                occurredAt = Instant.parse("2026-09-10T16:30:00Z").toEpochMilli()
            )
        )

        assertEquals(
            mapOf(LocalDate.of(2026, 9, 10) to 1),
            visits.countsByDate(utc)
        )
        assertEquals(
            mapOf(LocalDate.of(2026, 9, 11) to 1),
            visits.countsByDate(ZoneId.of("Asia/Shanghai"))
        )
    }

    @Test
    fun todayCountAndStreakUseDistinctConsecutiveDates() {
        val today = LocalDate.of(2026, 9, 11)
        val visits = listOf(
            visit("sep9", today.minusDays(2)),
            visit("sep10", today.minusDays(1)),
            visit("sep11-first", today),
            visit("sep11-second", today)
        )

        assertEquals(2, visits.todaysCount(today, utc))
        assertEquals(3, visits.streak(today, utc))
    }

    @Test
    fun streakStartsFromYesterdayWhenTodayHasNoVisit() {
        val today = LocalDate.of(2026, 9, 11)
        val visits = listOf(
            visit("sep8", today.minusDays(3)),
            visit("sep9", today.minusDays(2)),
            visit("sep10", today.minusDays(1))
        )

        assertEquals(3, visits.streak(today, utc))
    }

    @Test
    fun dailyBarsCoverSevenDatesAndFillMissingDatesWithZero() {
        val today = LocalDate.of(2026, 9, 11)
        val visits = listOf(
            visit("sep5-a", today.minusDays(6)),
            visit("sep5-b", today.minusDays(6)),
            visit("sep8", today.minusDays(3)),
            visit("sep11", today)
        )

        val points = visits.barPoints(ChartPeriod.DAY, today, utc)

        assertEquals(7, points.size)
        assertEquals(
            listOf(2, 0, 0, 1, 0, 0, 1),
            points.map { it.value }
        )
        assertEquals("2026-09-05", points.first().fullLabel)
        assertEquals("今", points.last().label)
        assertEquals("2026-09-11", points.last().fullLabel)
    }

    @Test
    fun weeklyBarsUseMondayBucketsAcrossAYearBoundary() {
        val today = LocalDate.of(2026, 1, 2)
        val visits = listOf(
            visit("previous-week", LocalDate.of(2025, 12, 22)),
            visit("monday", LocalDate.of(2025, 12, 29)),
            visit("new-year-a", LocalDate.of(2026, 1, 1)),
            visit("new-year-b", LocalDate.of(2026, 1, 1))
        )

        val points = visits.barPoints(ChartPeriod.WEEK, today, utc)

        assertEquals(8, points.size)
        assertEquals(1, points[points.lastIndex - 1].value)
        assertEquals(3, points.last().value)
        assertEquals("12月29日当周", points.last().fullLabel)
    }

    @Test
    fun monthAndYearBarsAggregateVisitsAndRetainEmptyBuckets() {
        val today = LocalDate.of(2026, 1, 15)
        val visits = listOf(
            visit("2022", LocalDate.of(2022, 3, 1)),
            visit("2024-a", LocalDate.of(2024, 3, 1)),
            visit("2024-b", LocalDate.of(2024, 3, 2)),
            visit("aug", LocalDate.of(2025, 8, 1)),
            visit("dec-a", LocalDate.of(2025, 12, 1)),
            visit("dec-b", LocalDate.of(2025, 12, 2)),
            visit("jan-a", LocalDate.of(2026, 1, 1)),
            visit("jan-b", LocalDate.of(2026, 1, 2)),
            visit("jan-c", LocalDate.of(2026, 1, 3))
        )

        assertEquals(
            listOf(1, 0, 0, 0, 2, 3),
            visits.barPoints(ChartPeriod.MONTH, today, utc).map { it.value }
        )
        assertEquals(
            listOf(1, 0, 2, 3, 3),
            visits.barPoints(ChartPeriod.YEAR, today, utc).map { it.value }
        )
    }

    @Test
    fun lineRangesHaveFixedLengthsAndIncludeBothEndpoints() {
        val today = LocalDate.of(2026, 9, 11)
        val visits = listOf(
            visit("month-start-a", today.minusDays(29)),
            visit("month-start-b", today.minusDays(29)),
            visit("week-start", today.minusDays(6)),
            visit("today", today)
        )

        val week = visits.linePoints(LineRange.WEEK, today, utc)
        val month = visits.linePoints(LineRange.MONTH, today, utc)

        assertEquals(7, week.size)
        assertEquals(listOf(1, 0, 0, 0, 0, 0, 1), week.map { it.value })
        assertEquals("2026-09-05", week.first().fullLabel)
        assertEquals("2026-09-11", week.last().fullLabel)
        assertEquals(30, month.size)
        assertEquals(2, month.first().value)
        assertEquals(1, month.last().value)
        assertEquals("2026-08-13", month.first().fullLabel)
        assertEquals("2026-09-11", month.last().fullLabel)
    }

    private fun visit(id: String, date: LocalDate): ToiletVisitEntity = ToiletVisitEntity(
        id = id,
        occurredAt = date.atTime(12, 0).atZone(utc).toInstant().toEpochMilli()
    )
}
