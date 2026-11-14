package com.example.lifeassets.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

fun parseYuanToCents(value: String): Long? = runCatching {
    value.replace(",", "").trim()
        .takeIf { it.isNotBlank() }
        ?.let { BigDecimal(it).setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact() }
}.getOrNull()

fun formatCents(cents: Long, decimals: Int = 0): String {
    val formatter = NumberFormat.getNumberInstance(Locale.CHINA).apply {
        minimumFractionDigits = decimals
        maximumFractionDigits = decimals
    }
    return "¥${formatter.format(BigDecimal.valueOf(cents, 2))}"
}

fun formatDailyCost(cents: Long, days: Long): String =
    formatCents(
        if (days <= 0) cents
        else BigDecimal.valueOf(cents).divide(BigDecimal.valueOf(days), 0, RoundingMode.HALF_UP).longValueExact(),
        2
    )
