package com.example.lifeassets.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AssetStatus(val label: String) {
    ACTIVE("持有中"),
    RETIRED("已退役"),
    SOLD("已出售")
}

enum class ExpenseType(val label: String) {
    INSTALLMENT("贷款月供"),
    SUBSCRIPTION("固定支出")
}

enum class ExpenseStatus(val label: String) {
    ACTIVE("使用中"),
    COMPLETED("已完成"),
    STOPPED("已停止")
}

enum class PeriodUnit(val label: String) {
    MONTH("每月"),
    YEAR("每年")
}

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val priceCents: Long,
    val purchaseDate: String,
    val targetDate: String?,
    val status: String,
    val imagePath: String?,
    val note: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val type: String,
    val totalAmountCents: Long,
    val periodAmountCents: Long,
    val periodUnit: String,
    val totalPeriods: Int,
    val paidPeriods: Int,
    val nextDueDate: String,
    val autoRenew: Boolean,
    val status: String,
    val imagePath: String?,
    val note: String,
    val createdAt: Long,
    val updatedAt: Long
)
