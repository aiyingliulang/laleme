package com.example.lifeassets.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lifeassets.data.AssetEntity
import com.example.lifeassets.data.AssetStatus
import com.example.lifeassets.data.ExpenseEntity
import com.example.lifeassets.data.ExpenseStatus
import com.example.lifeassets.data.ExpenseType
import com.example.lifeassets.data.PeriodUnit
import com.example.lifeassets.domain.daysHeld
import com.example.lifeassets.domain.formatCents
import com.example.lifeassets.domain.formatDailyCost
import com.example.lifeassets.domain.lifecycleProgress
import com.example.lifeassets.domain.parseDateOrNull
import com.example.lifeassets.domain.remainingDays
import java.time.LocalDate

@Composable
fun PageHeader(
    title: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAdd: () -> Unit
) {
    var searchOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 22.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (searchOpen) {
            androidx.compose.material3.OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text("搜索记录") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )
            TextButton(onClick = {
                searchOpen = false
                onSearchQueryChange("")
            }) { Text("取消") }
        } else {
            Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            IconButton(
                onClick = { searchOpen = true },
                modifier = Modifier.size(52.dp).clip(CircleShape).background(AppSurface)
            ) { Icon(Icons.Default.Search, contentDescription = "搜索") }
            Spacer(Modifier.width(10.dp))
            IconButton(
                onClick = onAdd,
                modifier = Modifier.size(52.dp).clip(CircleShape).background(AppOrange)
            ) { Icon(Icons.Default.Add, contentDescription = "新增", tint = Color.White) }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    count: Int,
    leftLabel: String,
    leftValue: String,
    rightLabel: String,
    rightValue: String,
    footer: List<Pair<Color, String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(horizontal = 22.dp, vertical = 19.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.width(10.dp))
                Text("$count 项", color = AppGray, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                SummaryValue(leftLabel, leftValue, Modifier.weight(1f))
                Box(Modifier.width(1.dp).height(48.dp).background(AppLine))
                SummaryValue(rightLabel, rightValue, Modifier.weight(1f))
            }
            Spacer(Modifier.height(14.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(AppLine))
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                footer.forEach { (color, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(9.dp).clip(CircleShape).background(color))
                        Spacer(Modifier.width(6.dp))
                        Text(label, color = AppGray, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryValue(label: String, value: String, modifier: Modifier) {
    Column(modifier.padding(horizontal = 2.dp)) {
        Text(label, color = AppGray, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            value,
            color = AppNavy,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CategoryTabs(categories: List<String>, selected: String?, onSelected: (String?) -> Unit) {
    if (categories.isEmpty()) return
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        categories.forEach { category ->
            val active = if (selected == null) category == categories.firstOrNull() else selected == category
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onSelected(if (category == categories.firstOrNull()) null else category) }
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (active) AppOrange.copy(alpha = 0.14f) else AppSurface
                ) {
                    CategoryIcon(
                        category = category,
                        tint = if (active) AppOrange else AppGray,
                        modifier = Modifier.padding(7.dp).size(22.dp)
                    )
                }
                Spacer(Modifier.height(5.dp))
                Text(
                    category,
                    color = if (active) AppNavy else AppGray,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(5.dp))
                Box(Modifier.width(if (active) 42.dp else 0.dp).height(3.dp).clip(RoundedCornerShape(3.dp)).background(AppOrange))
            }
        }
    }
}

@Composable
fun FilterRow(
    labels: List<String>,
    selected: String?,
    onSelected: (String?) -> Unit,
    onFilter: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            labels.forEach { label ->
                val active = selected == label || (selected == null && label == labels.firstOrNull())
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(25.dp)).clickable {
                        onSelected(if (label == labels.firstOrNull()) null else label)
                    },
                    color = if (active) AppNavy else AppSurface,
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        label,
                        color = if (active) Color.White else AppGray,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        IconButton(
            onClick = onFilter,
            modifier = Modifier.size(48.dp).clip(CircleShape).background(AppSurface)
        ) { Icon(Icons.Default.FilterAlt, contentDescription = "筛选", tint = AppNavy) }
    }
}

@Composable
fun AssetCard(
    asset: AssetEntity,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val purchaseDate = parseDateOrNull(asset.purchaseDate) ?: today
    val targetDate = parseDateOrNull(asset.targetDate.orEmpty())
    val held = daysHeld(purchaseDate, today)
    val remaining = remainingDays(targetDate, today)
    val progress = lifecycleProgress(purchaseDate, targetDate, today)
    var menuOpen by remember { mutableStateOf(false) }
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(15.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                ImageThumbnail(asset.imagePath, Modifier.size(72.dp), asset.category)
                Spacer(Modifier.weight(1f))
                Box {
                    StatusPill(assetStatusLabel(asset.status), AppGreen)
                    IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(30.dp).align(Alignment.TopEnd)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多操作", tint = AppGray)
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(text = { Text("编辑") }, onClick = { menuOpen = false; onEdit() }, leadingIcon = { Icon(Icons.Default.Edit, null) })
                        DropdownMenuItem(text = { Text("删除") }, onClick = { menuOpen = false; onDelete() }, leadingIcon = { Icon(Icons.Default.DeleteOutline, null) })
                    }
                }
            }
            Spacer(Modifier.height(13.dp))
            Text(asset.name, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${formatCents(asset.priceCents)} · $held 天", color = AppGray, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(formatDailyCost(asset.priceCents, held), fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(5.dp))
                Text("/天", color = AppGray, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(8.dp)),
                color = if (progress < .5f) AppOrange else Color(0xFFB6B52A),
                trackColor = AppLine
            )
            Spacer(Modifier.height(7.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("目标 ${asset.targetDate ?: "未设置"}", color = AppGray, style = MaterialTheme.typography.bodySmall)
                Text("还剩 $remaining 天", color = AppGray, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun ExpenseCard(
    expense: ExpenseEntity,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMarkPaid: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    val type = runCatching { ExpenseType.valueOf(expense.type) }.getOrDefault(ExpenseType.SUBSCRIPTION)
    val unit = runCatching { PeriodUnit.valueOf(expense.periodUnit) }.getOrDefault(PeriodUnit.MONTH)
    val isInstallment = type == ExpenseType.INSTALLMENT
    val remainingPeriods = (expense.totalPeriods - expense.paidPeriods).coerceAtLeast(0)
    val remainingAmount = remainingPeriods.toLong() * expense.periodAmountCents
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(15.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                ImageThumbnail(expense.imagePath, Modifier.size(72.dp), expense.category)
                Spacer(Modifier.weight(1f))
                Box {
                    StatusPill(if (isInstallment) "月供中" else "使用中", AppGreen)
                    IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(30.dp).align(Alignment.TopEnd)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多操作", tint = AppGray)
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(text = { Text("标记已支付") }, onClick = { menuOpen = false; onMarkPaid() }, leadingIcon = { Icon(Icons.Default.Paid, null) })
                        DropdownMenuItem(text = { Text("编辑") }, onClick = { menuOpen = false; onEdit() }, leadingIcon = { Icon(Icons.Default.Edit, null) })
                        DropdownMenuItem(text = { Text("删除") }, onClick = { menuOpen = false; onDelete() }, leadingIcon = { Icon(Icons.Default.DeleteOutline, null) })
                    }
                }
            }
            Spacer(Modifier.height(13.dp))
            Text(expense.name, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                if (isInstallment) "${formatCents(expense.totalAmountCents)} · 已还 ${expense.paidPeriods}/${expense.totalPeriods} 期"
                else "周期费 ${formatCents(expense.periodAmountCents)} · ${unit.label}",
                color = AppGray,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(formatCents(if (isInstallment) expense.periodAmountCents else 0), fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(5.dp))
                Text(if (isInstallment) "/月" else "累计", color = AppGray, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
                if (isInstallment) Text("剩余 ${formatCents(remainingAmount)}", color = AppGray, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = AppGray, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(5.dp))
                Text("下次支付 ${expense.nextDueDate.ifBlank { "未设置" }}", color = AppGray, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun ImageThumbnail(path: String?, modifier: Modifier = Modifier.size(72.dp), category: String? = null) {
    val uri = localImageUri(path)
    if (uri != null) {
        AsyncImage(model = uri, contentDescription = "记录图片", modifier = modifier.clip(RoundedCornerShape(18.dp)), contentScale = ContentScale.Crop)
    } else {
        Box(modifier.clip(RoundedCornerShape(18.dp)).background(AppMuted), contentAlignment = Alignment.Center) {
            CategoryIcon(category.orEmpty(), tint = AppNavy, modifier = Modifier.size(31.dp))
        }
    }
}

@Composable
fun CategoryIcon(category: String, tint: Color = AppNavy, modifier: Modifier = Modifier.size(22.dp)) {
    val icon = when (category) {
        "全部" -> Icons.Default.AllInclusive
        "交通工具" -> Icons.Default.DirectionsCar
        "家居", "居住" -> Icons.Default.Home
        "学习" -> Icons.Default.School
        "数码电子" -> Icons.Default.ShoppingBag
        "服务" -> Icons.Default.Paid
        else -> Icons.Default.Category
    }
    Icon(icon, contentDescription = category, tint = tint, modifier = modifier)
}

@Composable
fun StatusPill(label: String, color: Color) {
    Surface(color = AppMuted, shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(5.dp))
            Text(label, color = AppGray, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun EmptyState(title: String, message: String, onAdd: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 30.dp, vertical = 55.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.PieChart, contentDescription = null, tint = AppOrange, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(18.dp))
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))
        Text(message, color = AppGray, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(18.dp))
        AssistChip(onClick = onAdd, label = { Text("新增一条") }, leadingIcon = { Icon(Icons.Default.Add, null) })
    }
}

@Composable
fun BottomNavigation(current: String, onChange: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 22.dp, vertical = 12.dp),
        color = AppSurface,
        shape = RoundedCornerShape(32.dp),
        tonalElevation = 1.dp
    ) {
        Row(Modifier.fillMaxWidth().padding(5.dp), verticalAlignment = Alignment.CenterVertically) {
            BottomNavItem("assets", "资产", Icons.Default.PieChart, current, onChange, Modifier.weight(1f))
            BottomNavItem("expenses", "支出", Icons.Default.TrendingDown, current, onChange, Modifier.weight(1f))
            IconButton(onClick = { onChange("profile") }, modifier = Modifier.size(60.dp).clip(CircleShape).background(if (current == "profile") AppMuted else AppSurface)) {
                Icon(Icons.Default.AccountCircle, contentDescription = "我的", tint = AppNavy, modifier = Modifier.size(30.dp))
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    key: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    current: String,
    onChange: (String) -> Unit,
    modifier: Modifier
) {
    val active = key == current
    Surface(
        modifier = modifier.clip(RoundedCornerShape(27.dp)).clickable { onChange(key) },
        color = if (active) AppMuted else Color.Transparent,
        shape = RoundedCornerShape(27.dp)
    ) {
        Column(Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = if (active) AppOrange else AppNavy)
            Text(label, color = if (active) AppOrange else AppNavy, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun assetStatusLabel(status: String): String =
    runCatching { AssetStatus.valueOf(status).label }.getOrDefault("持有中")
