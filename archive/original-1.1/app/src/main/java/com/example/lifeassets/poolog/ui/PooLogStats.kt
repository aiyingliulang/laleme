package com.example.lifeassets.poolog.ui

import com.example.lifeassets.poolog.data.ToiletVisitEntity
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

data class ChartPoint(
    val label: String,
    val value: Int,
    val fullLabel: String
)

fun List<ToiletVisitEntity>.countsByDate(zone: ZoneId = ZoneId.systemDefault()): Map<LocalDate, Int> =
    groupingBy { Instant.ofEpochMilli(it.occurredAt).atZone(zone).toLocalDate() }.eachCount()

fun List<ToiletVisitEntity>.todaysCount(
    today: LocalDate = LocalDate.now(),
    zone: ZoneId = ZoneId.systemDefault()
): Int = countsByDate(zone)[today] ?: 0

fun List<ToiletVisitEntity>.streak(
    today: LocalDate = LocalDate.now(),
    zone: ZoneId = ZoneId.systemDefault()
): Int {
    val counts = countsByDate(zone)
    var cursor = if ((counts[today] ?: 0) > 0) today else today.minusDays(1)
    var days = 0
    while ((counts[cursor] ?: 0) > 0) {
        days++
        cursor = cursor.minusDays(1)
    }
    return days
}

fun List<ToiletVisitEntity>.barPoints(
    period: ChartPeriod,
    today: LocalDate = LocalDate.now(),
    zone: ZoneId = ZoneId.systemDefault()
): List<ChartPoint> {
    val counts = countsByDate(zone)
    return when (period) {
        ChartPeriod.DAY -> (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            ChartPoint(if (offset == 0) "今" else "${date.monthValue}/${date.dayOfMonth}", counts[date] ?: 0, date.toString())
        }

        ChartPeriod.WEEK -> {
            val thisMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            (7 downTo 0).map { offset ->
                val start = thisMonday.minusWeeks(offset.toLong())
                val value = (0..6).sumOf { day -> counts[start.plusDays(day.toLong())] ?: 0 }
                ChartPoint("${start.monthValue}/${start.dayOfMonth}", value, "${start.monthValue}月${start.dayOfMonth}日当周")
            }
        }

        ChartPeriod.MONTH -> {
            val current = YearMonth.from(today)
            (5 downTo 0).map { offset ->
                val month = current.minusMonths(offset.toLong())
                val value = counts.entries.filter { YearMonth.from(it.key) == month }.sumOf { it.value }
                ChartPoint("${month.monthValue}月", value, "${month.year}年${month.monthValue}月")
            }
        }

        ChartPeriod.YEAR -> (4 downTo 0).map { offset ->
            val year = today.year - offset
            val value = counts.entries.filter { it.key.year == year }.sumOf { it.value }
            ChartPoint("$year", value, "$year 年")
        }
    }
}

fun List<ToiletVisitEntity>.linePoints(
    range: LineRange,
    today: LocalDate = LocalDate.now(),
    zone: ZoneId = ZoneId.systemDefault()
): List<ChartPoint> {
    val counts = countsByDate(zone)
    val numberOfDays = if (range == LineRange.WEEK) 7 else 30
    val formatter = if (range == LineRange.WEEK) DateTimeFormatter.ofPattern("E", Locale.CHINA)
    else DateTimeFormatter.ofPattern("d", Locale.CHINA)
    return (numberOfDays - 1 downTo 0).map { offset ->
        val date = today.minusDays(offset.toLong())
        ChartPoint(formatter.format(date), counts[date] ?: 0, date.toString())
    }
}

enum class ChartPeriod(val label: String) {
    DAY("日"), WEEK("周"), MONTH("月"), YEAR("年")
}

enum class LineRange(val label: String) {
    WEEK("近一周"), MONTH("近一月")
}
