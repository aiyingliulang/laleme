package com.example.lifeassets.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.lifeassets.data.AssetEntity
import com.example.lifeassets.data.AssetStatus
import com.example.lifeassets.data.ExpenseEntity
import com.example.lifeassets.data.ExpenseType
import com.example.lifeassets.domain.formatCents
import com.example.lifeassets.domain.daysHeld
import com.example.lifeassets.domain.parseDateOrNull
import java.time.LocalDate

@Composable
fun AssetsHomeScreen(
    state: MainUiState,
    viewModel: MainViewModel,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    onDetail: (String) -> Unit
) {
    var deleteTarget by remember { mutableStateOf<AssetEntity?>(null) }
    var filterOpen by remember { mutableStateOf(false) }
    val activeAssets = state.allAssets.filter { it.status == AssetStatus.ACTIVE.name }
    val total = activeAssets.sumOf { it.priceCents }
    val daily = activeAssets.sumOf {
        val purchase = parseDateOrNull(it.purchaseDate) ?: LocalDate.now()
        val held = daysHeld(purchase, LocalDate.now())
        (it.priceCents.toDouble() / held).toLong()
    }
    val categories = listOf("全部") + state.allAssets.map { it.category }.distinct()

    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
        item {
            PageHeader("我的资产", state.assetQuery, viewModel::setAssetQuery, onAdd)
            SummaryCard(
                title = "资产总览",
                count = state.allAssets.size,
                leftLabel = "资产总额",
                leftValue = formatCents(total),
                rightLabel = "日均成本",
                rightValue = formatCents(daily),
                footer = listOf(
                    AppGreen to "持有中 ${activeAssets.size}",
                    Color(0xFFB0B2B3) to "已退役 ${state.allAssets.count { it.status == AssetStatus.RETIRED.name }}",
                    AppBlue to "已出售 ${state.allAssets.count { it.status == AssetStatus.SOLD.name }}"
                )
            )
            CategoryTabs(categories, state.assetCategory, viewModel::setAssetCategory)
            FilterRow(
                labels = listOf("全部", "持有中", "已退役", "已出售"),
                selected = state.assetStatus?.label,
                onSelected = { value ->
                    viewModel.setAssetStatus(value?.let { label -> AssetStatus.entries.firstOrNull { it.label == label } })
                },
                onFilter = { filterOpen = true }
            )
            Spacer(Modifier.height(15.dp))
        }
        if (state.visibleAssets.isEmpty()) {
            item { EmptyState("还没有资产记录", "把重要的物品和它们的时间价值记下来", onAdd) }
        } else {
            items(state.visibleAssets.chunked(2)) { row ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { asset ->
                        AssetCard(asset, Modifier.weight(1f), onClick = { onDetail(asset.id) }, onEdit = { onEdit(asset.id) }, onDelete = { deleteTarget = asset })
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        item { Spacer(Modifier.height(18.dp)) }
    }
    if (deleteTarget != null) {
        ConfirmDeleteDialog(
            title = "删除资产？",
            message = "“${deleteTarget?.name}”将从本机数据中移除，此操作无法撤销。",
            onConfirm = { deleteTarget?.let { viewModel.deleteAsset(it.id) }; deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
    if (filterOpen) {
        FilterInfoDialog("资产筛选", "你可以使用上方的分类和状态标签组合筛选记录。") { filterOpen = false }
    }
}

@Composable
fun ExpensesHomeScreen(
    state: MainUiState,
    viewModel: MainViewModel,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    onDetail: (String) -> Unit
) {
    var deleteTarget by remember { mutableStateOf<ExpenseEntity?>(null) }
    var filterOpen by remember { mutableStateOf(false) }
    val active = state.allExpenses.filter { it.status == com.example.lifeassets.data.ExpenseStatus.ACTIVE.name }
    val monthly = active.sumOf(::monthlyAmount)
    val now = LocalDate.now()
    val thisMonth = active.filter { parseDateOrNull(it.nextDueDate)?.let { d -> d.year == now.year && d.month == now.month } == true }
        .sumOf { it.periodAmountCents }
    val categories = listOf("全部") + state.allExpenses.map { it.category }.distinct()

    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
        item {
            PageHeader("每月支出", state.expenseQuery, viewModel::setExpenseQuery, onAdd)
            SummaryCard(
                title = "支出总览",
                count = state.allExpenses.size,
                leftLabel = "月均固定支出",
                leftValue = formatCents(monthly),
                rightLabel = "本月待支付",
                rightValue = formatCents(thisMonth),
                footer = listOf(
                    AppOrange to "贷款月供 ${active.count { it.type == ExpenseType.INSTALLMENT.name }}",
                    AppGreen to "固定支出 ${active.count { it.type == ExpenseType.SUBSCRIPTION.name }}",
                    AppOrange to "近 7 天 ${active.count { parseDateOrNull(it.nextDueDate)?.let { d -> java.time.temporal.ChronoUnit.DAYS.between(now, d) in 0..7 } == true }}"
                )
            )
            CategoryTabs(categories, state.expenseCategory, viewModel::setExpenseCategory)
            FilterRow(
                labels = listOf("全部", "贷款月供", "固定支出"),
                selected = state.expenseType?.label,
                onSelected = { value ->
                    viewModel.setExpenseType(value?.let { label -> ExpenseType.entries.firstOrNull { it.label == label } })
                },
                onFilter = { filterOpen = true }
            )
            Spacer(Modifier.height(15.dp))
        }
        if (state.visibleExpenses.isEmpty()) {
            item { EmptyState("还没有支出记录", "记录贷款月供和周期订阅，掌握每月固定负担", onAdd) }
        } else {
            items(state.visibleExpenses.chunked(2)) { row ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { expense ->
                        ExpenseCard(expense, onClick = { onDetail(expense.id) }, onEdit = { onEdit(expense.id) }, onDelete = { deleteTarget = expense }, onMarkPaid = { viewModel.markPaid(expense) })
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        item { Spacer(Modifier.height(18.dp)) }
    }
    if (deleteTarget != null) {
        ConfirmDeleteDialog(
            title = "删除支出？",
            message = "“${deleteTarget?.name}”将从本机数据中移除，此操作无法撤销。",
            onConfirm = { deleteTarget?.let { viewModel.deleteExpense(it.id) }; deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
    if (filterOpen) {
        FilterInfoDialog("支出筛选", "你可以使用上方的分类和类型标签组合筛选记录。") { filterOpen = false }
    }
}

private fun monthlyAmount(expense: ExpenseEntity): Long {
    val amount = expense.periodAmountCents
    return if (expense.periodUnit == com.example.lifeassets.data.PeriodUnit.YEAR.name) amount / 12 else amount
}

@Composable
fun ConfirmDeleteDialog(title: String, message: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { Button(onClick = onConfirm) { Text("确认删除") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
fun FilterInfoDialog(title: String, message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("知道了") } }
    )
}
