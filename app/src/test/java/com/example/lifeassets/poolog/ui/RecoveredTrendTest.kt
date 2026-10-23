package com.example.lifeassets.poolog.ui

import com.example.lifeassets.poolog.data.ToiletVisitEntity
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.*
import org.junit.Test

class RecoveredTrendTest {
    private val zone = ZoneId.of("Asia/Shanghai")
    private val today = LocalDate.of(2026, 10, 23)
    private fun visit(day: LocalDate, hour: Int = 12) = ToiletVisitEntity(occurredAt = day.atTime(hour,0).atZone(zone).toInstant().toEpochMilli())
    @Test fun dailyBucketsRespectLocalDayAndMidnight() {
        val points = listOf(visit(today,0), visit(today,23), visit(today.minusDays(1),23)).barPoints(ChartPeriod.DAY,today,zone)
        assertEquals(24,points.size); assertEquals(2,points.sumOf{it.value})
        assertEquals(1,points[0].value); assertEquals(1,points[23].value)
    }
    @Test fun weeklyBucketsStartOnMonday() {
        val points = listOf(visit(LocalDate.of(2026,10,19)),visit(LocalDate.of(2026,10,18)),visit(today)).barPoints(ChartPeriod.WEEK,today,zone)
        assertEquals(7,points.size); assertEquals(2,points.sumOf{it.value})
        assertEquals(1,points[0].value); assertEquals(1,points[4].value)
    }
    @Test fun monthlyAndYearlyWindowsExcludeOldRecords() {
        val visits = listOf(visit(LocalDate.of(2025,11,1)),visit(LocalDate.of(2025,10,31)),visit(today))
        val months = visits.barPoints(ChartPeriod.MONTH,today,zone)
        assertEquals(12,months.size); assertEquals(2,months.sumOf{it.value})
        assertEquals(1,months.first().value); assertEquals(1,months.last().value)
        val years = listOf(visit(LocalDate.of(2022,1,1)),visit(LocalDate.of(2021,12,31)),visit(today)).barPoints(ChartPeriod.YEAR,today,zone)
        assertEquals(5,years.size); assertEquals(2,years.sumOf{it.value})
    }
    @Test fun emptyHistoryHasZeroCounts() {
        ChartPeriod.entries.forEach { period -> assertTrue(emptyList<ToiletVisitEntity>().barPoints(period,today,zone).all{it.value == 0}) }
    }
}
