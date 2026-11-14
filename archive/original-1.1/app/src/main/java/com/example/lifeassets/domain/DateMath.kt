package com.example.lifeassets.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun parseDateOrNull(value: String): LocalDate? = runCatching { LocalDate.parse(value.trim()) }.getOrNull()

fun daysHeld(purchaseDate: LocalDate, today: LocalDate): Long =
    (ChronoUnit.DAYS.between(purchaseDate, today) + 1).coerceAtLeast(1)

fun remainingDays(targetDate: LocalDate?, today: LocalDate): Long =
    targetDate?.let { ChronoUnit.DAYS.between(today, it).coerceAtLeast(0) } ?: 0

fun lifecycleProgress(purchaseDate: LocalDate, targetDate: LocalDate?, today: LocalDate): Float {
    val held = daysHeld(purchaseDate, today).toDouble()
    val remaining = remainingDays(targetDate, today).toDouble()
    return if (targetDate == null || held + remaining <= 0) 0f
    else (held / (held + remaining)).coerceIn(0.0, 1.0).toFloat()
}

fun nextDate(date: LocalDate, unit: String): LocalDate = when (unit) {
    "YEAR" -> date.plusYears(1)
    else -> date.plusMonths(1)
}
