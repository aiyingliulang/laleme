package com.example.lifeassets.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifeassets.data.ExpenseType
import com.example.lifeassets.data.PeriodUnit
import com.example.lifeassets.domain.daysHeld
import com.example.lifeassets.domain.formatCents
import com.example.lifeassets.domain.formatDailyCost
import com.example.lifeassets.domain.lifecycleProgress
import com.example.lifeassets.domain.parseDateOrNull
import com.example.lifeassets.domain.remainingDays
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetDetailScreen(id: String?, state: MainUiState, viewModel: MainViewModel, onBack: () -> Unit) {
    val asset = state.allAssets.firstOrNull { it.id == id }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("资产详情") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } }
        )
    }) { padding ->
        if (asset == null) {
            Text("记录不存在", modifier = Modifier.padding(padding).padding(22.dp))
        } else {
            val today = LocalDate.now()
            val purchase = parseDateOrNull(asset.purchaseDate) ?: today
            val target = parseDateOrNull(asset.targetDate.orEmpty())
            val held = daysHeld(purchase, today)
            val remaining = remainingDays(target, today)
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(colors = CardDefaults.cardColors(containerColor = AppSurface), shape = androidx.compose.foundation.shape.RoundedCornerShape(25.dp)) {
                    Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ImageThumbnail(asset.imagePath, Modifier.size(80.dp))
                            Spacer(Modifier.padding(5.dp))
                            Column { Text(asset.name, style = MaterialTheme.typography.headlineMedium); StatusPill(asset.status, AppGreen) }
                        }
                        DetailRow("购买价格", formatCents(asset.priceCents))
                        DetailRow("购买日期", asset.purchaseDate)
                        DetailRow("已使用", "$held 天")
                        DetailRow("每日成本", formatDailyCost(asset.priceCents, held))
                        DetailRow("目标日期", asset.targetDate ?: "未设置")
                        DetailRow("剩余时间", "$remaining 天")
                        if (asset.note.isNotBlank()) DetailRow("备注", asset.note)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(id: String?, state: MainUiState, viewModel: MainViewModel, onBack: () -> Unit) {
    val expense = state.allExpenses.firstOrNull { it.id == id }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("支出详情") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } }
        )
    }) { padding ->
        if (expense == null) {
            Text("记录不存在", modifier = Modifier.padding(padding).padding(22.dp))
        } else {
            val type = runCatching { ExpenseType.valueOf(expense.type) }.getOrDefault(ExpenseType.SUBSCRIPTION)
            val unit = runCatching { PeriodUnit.valueOf(expense.periodUnit) }.getOrDefault(PeriodUnit.MONTH)
            val remainingPeriods = (expense.totalPeriods - expense.paidPeriods).coerceAtLeast(0)
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(colors = CardDefaults.cardColors(containerColor = AppSurface), shape = androidx.compose.foundation.shape.RoundedCornerShape(25.dp)) {
                    Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ImageThumbnail(expense.imagePath, Modifier.size(80.dp))
                            Spacer(Modifier.padding(5.dp))
                            Column { Text(expense.name, style = MaterialTheme.typography.headlineMedium); StatusPill(type.label, AppGreen) }
                        }
                        DetailRow("类别", expense.category)
                        DetailRow("周期金额", "${formatCents(expense.periodAmountCents)} ${unit.label}")
                        if (type == ExpenseType.INSTALLMENT) {
                            DetailRow("总金额", formatCents(expense.totalAmountCents))
                            DetailRow("还款进度", "${expense.paidPeriods}/${expense.totalPeriods} 期")
                            DetailRow("剩余金额", formatCents(remainingPeriods.toLong() * expense.periodAmountCents))
                        }
                        DetailRow("下次支付", expense.nextDueDate)
                        DetailRow("自动续费", if (expense.autoRenew) "开启" else "关闭")
                        if (expense.note.isNotBlank()) DetailRow("备注", expense.note)
                    }
                }
                Button(onClick = { viewModel.markPaid(expense) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    Spacer(Modifier.padding(3.dp))
                    Text("标记本期已支付")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = AppGray, style = MaterialTheme.typography.bodyMedium)
        Text(value, fontSize = 17.sp, style = MaterialTheme.typography.bodyLarge)
    }
}
