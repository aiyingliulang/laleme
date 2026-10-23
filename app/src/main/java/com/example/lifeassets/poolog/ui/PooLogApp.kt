package com.example.lifeassets.poolog.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lifeassets.poolog.data.ToiletVisitEntity
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.max

private enum class PooLogTab(val label: String) {
    TODAY("记录"), CALENDAR("日历")
}

private val ChineseLocale = Locale.CHINA
private val FullDateFormatter = DateTimeFormatter.ofPattern("M月d日 · EEEE", ChineseLocale)
private val ShortDateFormatter = DateTimeFormatter.ofPattern("M月d日", ChineseLocale)
private val TimeFormatter = DateTimeFormatter.ofPattern("HH:mm", ChineseLocale)

/** The full application shell. Data stays in Room on-device and is never sent anywhere. */
@Composable
fun PooLogApp(viewModel: PooLogViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<ToiletVisitEntity?>(null) }
    androidx.compose.runtime.CompositionLocalProvider(LocalDeleteVisit provides { visit -> pendingDelete = visit }) {
    pendingDelete?.let { visit ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除这条记录？") },
            text = { Text("删除后无法恢复，确定删除所选记录吗？") },
            confirmButton = { androidx.compose.material3.TextButton(onClick = { viewModel.deleteVisit(visit.id); pendingDelete = null }) { Text("删除") } },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { pendingDelete = null }) { Text("取消") } }
        )
    }
    var selectedTab by remember { mutableStateOf(PooLogTab.TODAY) }
    var isRecording by remember { mutableStateOf(false) }
    var loggedAt by remember { mutableStateOf(0L) }
    var celebrationIndex by remember { mutableStateOf(0) }
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(celebrationIndex) {
        if (celebrationIndex > 0) {
            delay(2_350)
            isRecording = false
        }
    }

    Box(Modifier.fillMaxSize()) {
        androidx.compose.material3.Scaffold(
            containerColor = PooCanvas,
            bottomBar = {
                NavigationBar(
                    containerColor = PooPaper,
                    contentColor = PooInk,
                    tonalElevation = 0.dp
                ) {
                    PooLogTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = {
                                if (tab == PooLogTab.TODAY) {
                                    PoopMark(Modifier.size(22.dp), if (selectedTab == tab) PooCocoa else PooMuted)
                                } else {
                                    CalendarMark(Modifier.size(22.dp), if (selectedTab == tab) PooSage else PooMuted)
                                }
                            },
                            label = { Text(tab.label) },
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                selectedIconColor = PooCocoa,
                                selectedTextColor = PooInk,
                                indicatorColor = PooBlush,
                                unselectedIconColor = PooMuted,
                                unselectedTextColor = PooMuted
                            )
                        )
                    }
                }
            }
        ) { padding ->
            when (selectedTab) {
                PooLogTab.TODAY -> TodayScreen(
                    visits = state.visits,
                    isRecording = isRecording,
                    modifier = Modifier.padding(padding),
                    onUndo = { viewModel.undoLast() },
                    onLog = {
                        if (!isRecording) {
                            loggedAt = System.currentTimeMillis()
                            isRecording = true
                            celebrationIndex += 1
                            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            viewModel.logNow(loggedAt)
                        }
                    }
                )

                PooLogTab.CALENDAR -> CalendarAndTrendsScreen(
                    visits = state.visits,
                    modifier = Modifier.padding(padding)
                )
            }
        }

        if (isRecording && celebrationIndex > 0) {
            LogCelebration(
                loggedAt = loggedAt,
                message = encouragements[(celebrationIndex - 1) % encouragements.size],
                onDismiss = { isRecording = false }
            )
        }
    }
}

}

private val LocalDeleteVisit = androidx.compose.runtime.staticCompositionLocalOf<(ToiletVisitEntity) -> Unit> { {} }

@Composable
private fun TodayScreen(
    visits: List<ToiletVisitEntity>,
    isRecording: Boolean,
    modifier: Modifier = Modifier,
    onUndo: () -> Unit,
    onLog: () -> Unit
) {
    val today = LocalDate.now()
    val todayVisits = remember(visits, today) { visits.filter { it.localDate() == today } }
    val streak = remember(visits, today) { visits.streak(today) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 22.dp, top = 22.dp, end = 22.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("拉了么", style = MaterialTheme.typography.displaySmall, color = PooInk)
                Spacer(Modifier.height(4.dp))
                Text(recoveredHeader(visits, FullDateFormatter.format(today)), style = MaterialTheme.typography.bodyLarge, color = PooMuted)
            }
        }

        item {
            DailySummary(count = todayVisits.size, streak = streak)
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PooLogButton(isRecording = isRecording, onClick = onLog)
                Spacer(Modifier.height(14.dp))
                Text(
                    if (todayVisits.isEmpty()) "先记下一次，今天的印章会从这里开始。" else "点一下，记录这一趟。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PooMuted,
                    textAlign = TextAlign.Center
                )
            }
        }

        item {
            Column {
                if (visits.isNotEmpty()) androidx.compose.material3.TextButton(onClick = onUndo) { Text("撤销上次记录") }
                TodayTimeline(todayVisits)
            }
        }

        item {
            Text(
                "只记录时间和次数，默认保存在本机。",
                modifier = Modifier.fillMaxWidth(),
                color = PooMuted,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DailySummary(count: Int, streak: Int) {
    Surface(
        color = PooInk,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("今天已记录", color = Color.White.copy(alpha = 0.74f), style = MaterialTheme.typography.bodyMedium)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = count.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.displayLarge.copy(fontFamily = FontFamily.Monospace)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text("次", color = Color.White.copy(alpha = 0.82f), style = MaterialTheme.typography.titleMedium)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("连续记录", color = Color.White.copy(alpha = 0.74f), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(5.dp))
                Text(
                    text = if (streak == 0) "从今天开始" else "$streak 天",
                    color = PooApricot,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Composable
private fun PooLogButton(isRecording: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (isRecording) 0.91f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "log-button-scale"
    )
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(210.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(18.dp, CircleShape, clip = false, ambientColor = PooSage.copy(alpha = 0.22f), spotColor = PooSage.copy(alpha = 0.3f))
            .clip(CircleShape)
            .background(PooCocoa)
            .border(2.dp, Color.White.copy(alpha = 0.36f), CircleShape)
            .clickable(
                enabled = !isRecording,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .semantics { contentDescription = "记录一次如厕" },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PoopMark(Modifier.size(76.dp), PooApricot)
            Spacer(Modifier.height(1.dp))
            Text(
                "拉",
                color = Color.White,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 51.sp)
            )
        }
    }
}

@Composable
private fun TodayTimeline(todayVisits: List<ToiletVisitEntity>) {
    Surface(
        color = PooPaper,
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PooLine),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("今天的印章", style = MaterialTheme.typography.titleLarge, color = PooInk)
                Text("${todayVisits.size} 次", style = MaterialTheme.typography.labelLarge, color = PooSage)
            }
            Spacer(Modifier.height(12.dp))
            if (todayVisits.isEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PoopMark(Modifier.size(22.dp), PooLine)
                    Spacer(Modifier.width(9.dp))
                    Text("还没有记录", color = PooMuted, style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                todayVisits.take(5).forEachIndexed { index, visit ->
                    TimelineRow(visit, isLast = index == minOf(todayVisits.size, 5) - 1)
                }
                if (todayVisits.size > 5) {
                    Text(
                        "还有 ${todayVisits.size - 5} 次记录",
                        modifier = Modifier.padding(start = 30.dp, top = 5.dp),
                        color = PooMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineRow(visit: ToiletVisitEntity, isLast: Boolean) {
    val delete = LocalDeleteVisit.current
    Row(
        modifier = Modifier.fillMaxWidth().height(34.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(22.dp).height(34.dp), contentAlignment = Alignment.Center) {
            PoopMark(Modifier.size(15.dp), PooCocoa)
            if (!isLast) {
                Box(
                    Modifier
                        .width(1.dp)
                        .height(16.dp)
                        .align(Alignment.BottomCenter)
                        .background(PooLine)
                )
            }
        }
        Spacer(Modifier.width(9.dp))
        Text(visit.formatTime(), style = MaterialTheme.typography.titleMedium, color = PooInk)
        Spacer(Modifier.width(10.dp))
        Text("已记录 · 删除", modifier = Modifier.clickable { delete(visit) }, style = MaterialTheme.typography.bodySmall, color = PooMuted)
    }
}

@Composable
private fun CalendarAndTrendsScreen(visits: List<ToiletVisitEntity>, modifier: Modifier = Modifier) {
    val today = LocalDate.now()
    val countByDate = remember(visits) { visits.countsByDate() }
    var displayedMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(today) }
    var chartPeriod by remember { mutableStateOf(ChartPeriod.DAY) }
    var lineRange by remember { mutableStateOf(LineRange.WEEK) }
    val selectedVisits = remember(visits, selectedDate) { visits.filter { it.localDate() == selectedDate } }
    val monthCount = remember(countByDate, displayedMonth) {
        countByDate.filterKeys { YearMonth.from(it) == displayedMonth }.values.sum()
    }
    val activeDays = remember(countByDate, displayedMonth) {
        countByDate.keys.count { YearMonth.from(it) == displayedMonth }
    }
    val barPoints = remember(visits, chartPeriod, today) { visits.barPoints(chartPeriod, today) }
    val linePoints = remember(visits, lineRange, today) { visits.linePoints(lineRange, today) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, top = 22.dp, end = 18.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("日历与趋势", style = MaterialTheme.typography.displaySmall, color = PooInk)
                Spacer(Modifier.height(4.dp))
                Text("每一枚印章，都是生活的小节奏。", style = MaterialTheme.typography.bodyMedium, color = PooMuted)
            }
        }

        item {
            CalendarCard(
                month = displayedMonth,
                today = today,
                selectedDate = selectedDate,
                countByDate = countByDate,
                onPreviousMonth = {
                    val previous = displayedMonth.minusMonths(1)
                    displayedMonth = previous
                    selectedDate = previous.atDay(1)
                },
                onNextMonth = {
                    if (displayedMonth < YearMonth.from(today)) {
                        val next = displayedMonth.plusMonths(1)
                        displayedMonth = next
                        selectedDate = if (next == YearMonth.from(today)) today else next.atDay(1)
                    }
                },
                onSelectDate = { selectedDate = it }
            )
        }

        item {
            MonthlyStatStrip(monthCount, activeDays)
        }

        item {
            SelectedDayCard(selectedDate, selectedVisits, today)
        }

        item {
            Text("柱状图", style = MaterialTheme.typography.headlineMedium, color = PooInk)
        }

        item {
            ChartPeriodSelector(selected = chartPeriod, onSelected = { chartPeriod = it })
        }

        item {
            BarChartCard(chartPeriod, barPoints)
        }

        item {
            Text("折线趋势", style = MaterialTheme.typography.headlineMedium, color = PooInk)
        }

        item {
            LineRangeSelector(selected = lineRange, onSelected = { lineRange = it })
        }

        item {
            LineChartCard(lineRange, linePoints)
        }

        item {
            Text(
                "图表只呈现次数和时间，不作健康判断。",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = PooMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun CalendarCard(
    month: YearMonth,
    today: LocalDate,
    selectedDate: LocalDate,
    countByDate: Map<LocalDate, Int>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (LocalDate) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = PooPaper,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PooLine)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonthMoveButton("‹", "上个月", enabled = true, onClick = onPreviousMonth)
                Text(
                    "${month.year} 年 ${month.monthValue} 月",
                    style = MaterialTheme.typography.titleLarge,
                    color = PooInk
                )
                MonthMoveButton("›", "下个月", enabled = month < YearMonth.from(today), onClick = onNextMonth)
            }
            Spacer(Modifier.height(12.dp))
            CalendarGrid(month, today, selectedDate, countByDate, onSelectDate)
        }
    }
}

@Composable
private fun MonthMoveButton(symbol: String, description: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(if (enabled) PooSoft else Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, color = if (enabled) PooInk else PooLine, fontSize = 28.sp, lineHeight = 28.sp)
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    today: LocalDate,
    selectedDate: LocalDate,
    counts: Map<LocalDate, Int>,
    onSelectDate: (LocalDate) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    color = PooMuted,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(Modifier.height(5.dp))
        val blankDays = month.atDay(1).dayOfWeek.value - 1
        val allCells = blankDays + month.lengthOfMonth()
        val rows = (allCells + 6) / 7
        repeat(rows) { row ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { column ->
                    val cell = row * 7 + column
                    val dayNumber = cell - blankDays + 1
                    if (dayNumber !in 1..month.lengthOfMonth()) {
                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date = month.atDay(dayNumber)
                        CalendarDayCell(
                            date = date,
                            count = counts[date] ?: 0,
                            selected = date == selectedDate,
                            isToday = date == today,
                            selectable = !date.isAfter(today),
                            onClick = { onSelectDate(date) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    count: Int,
    selected: Boolean,
    isToday: Boolean,
    selectable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    val background = when {
        selected -> PooSage.copy(alpha = 0.14f)
        count > 0 -> PooBlush
        else -> Color.Transparent
    }
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(shape)
            .background(background)
            .then(if (isToday) Modifier.border(1.dp, PooSage, shape) else Modifier)
            .clickable(enabled = selectable, onClick = onClick)
            .semantics { contentDescription = "${date.monthValue}月${date.dayOfMonth}日，${if (count == 0) "无记录" else "$count 次"}" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = if (selectable) PooInk else PooLine,
            style = MaterialTheme.typography.labelMedium
        )
        if (count > 0) {
            Spacer(Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .height(16.dp)
                    .wrapContentHeight()
                    .clip(CircleShape)
                    .background(PooCocoa),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (count > 3) "3+" else count.toString(),
                    modifier = Modifier.padding(horizontal = 5.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.sp)
                )
            }
        }
    }
}

@Composable
private fun MonthlyStatStrip(total: Int, activeDays: Int) {
    Surface(
        color = PooSoft,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("本月 $total 次", color = PooInk, style = MaterialTheme.typography.titleMedium)
            Text("$activeDays 天有记录", color = PooMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SelectedDayCard(date: LocalDate, visits: List<ToiletVisitEntity>, today: LocalDate) {
    Surface(
        color = PooPaper,
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PooLine),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                "${ShortDateFormatter.format(date)} · ${visits.size} 次",
                style = MaterialTheme.typography.titleLarge,
                color = PooInk
            )
            Spacer(Modifier.height(9.dp))
            when {
                date.isAfter(today) -> Text("未来日期不能记录。", color = PooMuted, style = MaterialTheme.typography.bodyMedium)
                visits.isEmpty() -> Text("这一天还没有留下印章。", color = PooMuted, style = MaterialTheme.typography.bodyMedium)
                else -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    visits.chunked(4).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { visit ->
                                val delete = LocalDeleteVisit.current
                                Box(Modifier.clickable { delete(visit) }) { TimeChip(visit.formatTime() + " · 删除") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeChip(time: String) {
    Surface(color = PooBlush, shape = CircleShape) {
        Text(
            text = time,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = PooCocoa,
            style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace)
        )
    }
}

@Composable
private fun ChartPeriodSelector(selected: ChartPeriod, onSelected: (ChartPeriod) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        ChartPeriod.entries.forEach { period ->
            SelectorPill(
                label = "按${period.label}",
                selected = selected == period,
                modifier = Modifier.weight(1f),
                onClick = { onSelected(period) }
            )
        }
    }
}

@Composable
private fun LineRangeSelector(selected: LineRange, onSelected: (LineRange) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LineRange.entries.forEach { range ->
            SelectorPill(
                label = range.label,
                selected = selected == range,
                modifier = Modifier.weight(1f),
                onClick = { onSelected(range) }
            )
        }
    }
}

@Composable
private fun SelectorPill(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (selected) PooSage else PooPaper)
            .border(1.dp, if (selected) PooSage else PooLine, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else PooMuted,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun BarChartCard(period: ChartPeriod, points: List<ChartPoint>) {
    val total = points.sumOf { it.value }
    val peak = points.maxOfOrNull { it.value } ?: 0
    Surface(
        color = PooPaper,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PooLine),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(when(period) { ChartPeriod.DAY -> "今日 24 小时趋势"; ChartPeriod.WEEK -> "本周 7 天趋势"; ChartPeriod.MONTH -> "近 12 个月趋势"; ChartPeriod.YEAR -> "近 5 年趋势" }, style = MaterialTheme.typography.titleLarge, color = PooInk)
            Spacer(Modifier.height(3.dp))
            Text("共 $total 次 · 峰值 $peak 次", style = MaterialTheme.typography.bodyMedium, color = PooMuted)
            Spacer(Modifier.height(16.dp))
            Column(Modifier.horizontalScroll(rememberScrollState()).width(maxOf(280, points.size * 42 + 64).dp)) {
                BarChart(points)
                Spacer(Modifier.height(8.dp))
                AxisLabels(points)
            }
        }
    }
}

@Composable
private fun BarChart(points: List<ChartPoint>) {
    val maxValue = max(1, points.maxOfOrNull { it.value } ?: 0)
    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(154.dp)
            .semantics { contentDescription = points.joinToString("；") { "${it.fullLabel} ${it.value} 次" } }
    ) {
        val top = 12.dp.toPx()
        val baseline = size.height - 8.dp.toPx()
        val chartHeight = baseline - top
        val slot = size.width / points.size
        val barWidth = (slot * 0.54f).coerceAtMost(30.dp.toPx())
        repeat(3) { index ->
            val y = top + chartHeight * index / 2f
            drawLine(PooLine.copy(alpha = 0.72f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }
        points.forEachIndexed { index, point ->
            val valueHeight = if (point.value == 0) 2.dp.toPx() else chartHeight * point.value / maxValue
            val x = index * slot + (slot - barWidth) / 2f
            drawRoundRect(
                color = if (point.value == 0) PooLine else PooCocoa,
                topLeft = Offset(x, baseline - valueHeight),
                size = Size(barWidth, valueHeight),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )
        }
    }
}

@Composable
private fun AxisLabels(points: List<ChartPoint>) {
    Row(Modifier.fillMaxWidth()) {
        points.forEach { point ->
            Text(
                text = "${point.value}次\n${point.label}",
                modifier = Modifier.weight(1f),
                color = PooMuted,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun LineChartCard(range: LineRange, points: List<ChartPoint>) {
    val total = points.sumOf { it.value }
    val peak = points.maxOfOrNull { it.value } ?: 0
    Surface(
        color = PooPaper,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PooLine),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(range.label, style = MaterialTheme.typography.titleLarge, color = PooInk)
            Spacer(Modifier.height(3.dp))
            Text("共 $total 次 · 单日最高 $peak 次", style = MaterialTheme.typography.bodyMedium, color = PooMuted)
            Spacer(Modifier.height(16.dp))
            LineChart(points)
            Spacer(Modifier.height(8.dp))
            SparseAxisLabels(points)
        }
    }
}

@Composable
private fun LineChart(points: List<ChartPoint>) {
    val maxValue = max(1, points.maxOfOrNull { it.value } ?: 0)
    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(154.dp)
            .semantics { contentDescription = points.joinToString("；") { "${it.fullLabel} ${it.value} 次" } }
    ) {
        val top = 12.dp.toPx()
        val baseline = size.height - 12.dp.toPx()
        val chartHeight = baseline - top
        repeat(3) { index ->
            val y = top + chartHeight * index / 2f
            drawLine(PooLine.copy(alpha = 0.72f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }
        val horizontalGap = if (points.size == 1) 0f else size.width / (points.size - 1)
        val path = Path()
        points.forEachIndexed { index, point ->
            val x = horizontalGap * index
            val y = baseline - chartHeight * point.value / maxValue
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, PooSage, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        points.forEachIndexed { index, point ->
            val x = horizontalGap * index
            val y = baseline - chartHeight * point.value / maxValue
            drawCircle(PooPaper, radius = 5.dp.toPx(), center = Offset(x, y))
            drawCircle(PooApricot, radius = 3.5.dp.toPx(), center = Offset(x, y))
        }
    }
}

@Composable
private fun SparseAxisLabels(points: List<ChartPoint>) {
    val shown = remember(points) {
        if (points.size <= 7) points else listOf(points.first(), points[points.size / 3], points[(points.size * 2) / 3], points.last())
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        shown.forEach { point ->
            Text(point.label, color = PooMuted, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp))
        }
    }
}

@Composable
private fun LogCelebration(loggedAt: Long, message: String, onDismiss: () -> Unit) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(160)) + scaleIn(initialScale = 0.84f, animationSpec = tween(300)),
        exit = fadeOut(tween(140)) + scaleOut(targetScale = 0.94f, animationSpec = tween(160))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PooInk.copy(alpha = 0.28f))
                .clickable(onClick = onDismiss)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = PooPaper,
                shape = RoundedCornerShape(30.dp),
                shadowElevation = 18.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RippleStamp()
                    Spacer(Modifier.height(14.dp))
                    Text("记下了！", style = MaterialTheme.typography.headlineMedium, color = PooInk)
                    Spacer(Modifier.height(7.dp))
                    Text(message, style = MaterialTheme.typography.bodyLarge, color = PooMuted, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "${Instant.ofEpochMilli(loggedAt).atZone(ZoneId.systemDefault()).format(TimeFormatter)} 已记录",
                        color = PooSage,
                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace)
                    )
                }
            }
        }
    }
}

@Composable
private fun RippleStamp() {
    Box(Modifier.size(106.dp), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
            drawCircle(PooSage.copy(alpha = 0.14f), radius = size.minDimension * 0.49f, style = Stroke(1.5.dp.toPx()))
            drawCircle(PooSage.copy(alpha = 0.22f), radius = size.minDimension * 0.37f, style = Stroke(1.dp.toPx()))
        }
        PoopMark(Modifier.size(58.dp), PooCocoa)
    }
}

@Composable
private fun PoopMark(modifier: Modifier = Modifier, color: Color) {
    androidx.compose.foundation.Canvas(modifier) {
        val w = size.width
        val h = size.height
        val pile = Path().apply {
            moveTo(w * 0.19f, h * 0.80f)
            cubicTo(w * 0.08f, h * 0.65f, w * 0.17f, h * 0.50f, w * 0.29f, h * 0.45f)
            cubicTo(w * 0.23f, h * 0.31f, w * 0.33f, h * 0.19f, w * 0.45f, h * 0.17f)
            cubicTo(w * 0.48f, h * 0.06f, w * 0.63f, h * 0.06f, w * 0.67f, h * 0.19f)
            cubicTo(w * 0.79f, h * 0.23f, w * 0.82f, h * 0.36f, w * 0.75f, h * 0.47f)
            cubicTo(w * 0.88f, h * 0.55f, w * 0.89f, h * 0.72f, w * 0.78f, h * 0.82f)
            cubicTo(w * 0.64f, h * 0.95f, w * 0.34f, h * 0.95f, w * 0.19f, h * 0.80f)
            close()
        }
        drawPath(pile, color)
        drawPath(pile, Color.White.copy(alpha = 0.22f), style = Stroke(width = (w * 0.035f).coerceAtLeast(1.dp.toPx())))
    }
}

@Composable
private fun CalendarMark(modifier: Modifier = Modifier, color: Color) {
    androidx.compose.foundation.Canvas(modifier) {
        val stroke = 1.8.dp.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(stroke / 2f, size.height * 0.17f),
            size = Size(size.width - stroke, size.height * 0.73f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = Stroke(stroke)
        )
        drawLine(color, Offset(stroke, size.height * 0.36f), Offset(size.width - stroke, size.height * 0.36f), strokeWidth = stroke)
        drawCircle(color, radius = stroke, center = Offset(size.width * 0.32f, size.height * 0.61f))
        drawCircle(color, radius = stroke, center = Offset(size.width * 0.68f, size.height * 0.61f))
    }
}

private fun ToiletVisitEntity.localDate(): LocalDate =
    Instant.ofEpochMilli(occurredAt).atZone(ZoneId.systemDefault()).toLocalDate()

private fun ToiletVisitEntity.formatTime(): String =
    Instant.ofEpochMilli(occurredAt).atZone(ZoneId.systemDefault()).format(TimeFormatter)

private val encouragements = listOf(
    "记下了，今天也照顾好自己。",
    "完成一趟，给身体一点掌声。",
    "顺顺利利，继续保持。",
    "这一枚印章已经落在今天。"
)

private fun recoveredHeader(visits: List<ToiletVisitEntity>, date: String): String {
    val thresholds = listOf(0,1,10,30,100,300,1000)
    val titles = listOf("初来乍到","便便新手","顺畅达人","规律先锋","黄金守护者","如厕大师","传奇打卡王")
    val level = thresholds.indexOfLast { visits.size >= it }
    val elapsed = visits.maxOfOrNull { it.occurredAt }?.let { ((System.currentTimeMillis()-it).coerceAtLeast(0))/60000 }
    val since = elapsed?.let { "${it/1440}天 ${it%1440/60}小时 ${it%60}分钟" } ?: "暂无记录"
    val next = thresholds.getOrNull(level+1)?.let { " · 距升级 ${it-visits.size} 次" } ?: " · 已达最高等级"
    return "$date\n距上次：$since\n称号：Lv.$level ${titles[level]}$next"
}
