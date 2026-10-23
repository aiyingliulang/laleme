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
    val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val firstMonth = YearMonth.from(today).minusMonths(11)
    val firstYear = today.year - 4
    val size = when(period) { ChartPeriod.DAY -> 24; ChartPeriod.WEEK -> 7; ChartPeriod.MONTH -> 12; ChartPeriod.YEAR -> 5 }
    val values = IntArray(size)
    forEach { visit ->
        val time = Instant.ofEpochMilli(visit.occurredAt).atZone(zone)
        val date = time.toLocalDate()
        val index = when(period) {
            ChartPeriod.DAY -> if(date == today) time.hour else -1
            ChartPeriod.WEEK -> java.time.temporal.ChronoUnit.DAYS.between(monday, date).toInt()
            ChartPeriod.MONTH -> java.time.temporal.ChronoUnit.MONTHS.between(firstMonth, YearMonth.from(date)).toInt()
            ChartPeriod.YEAR -> date.year - firstYear
        }
        if(index in values.indices) values[index]++
    }
    return values.mapIndexed { index, value ->
        val label = when(period) {
            ChartPeriod.DAY -> "${index}时"
            ChartPeriod.WEEK -> listOf("周一","周二","周三","周四","周五","周六","周日")[index]
            ChartPeriod.MONTH -> "${firstMonth.plusMonths(index.toLong()).monthValue}月"
            ChartPeriod.YEAR -> "${firstYear+index}年"
        }
        ChartPoint(label, value, label)
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
