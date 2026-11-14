package com.example.lifeassets.ui

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.NumberPicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import com.example.lifeassets.data.AssetEntity
import com.example.lifeassets.data.AssetStatus
import com.example.lifeassets.data.ExpenseEntity
import com.example.lifeassets.data.ExpenseStatus
import com.example.lifeassets.data.ExpenseType
import com.example.lifeassets.data.PeriodUnit
import com.example.lifeassets.domain.parseDateOrNull
import com.example.lifeassets.domain.parseYuanToCents
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetEditorScreen(id: String?, viewModel: MainViewModel, onBack: () -> Unit) {
    val existing = viewModel.asset(id)
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var name by remember(id, existing?.updatedAt) { mutableStateOf(existing?.name.orEmpty()) }
    var category by remember(id, existing?.updatedAt) { mutableStateOf(existing?.category ?: "数码电子") }
    var price by remember(id, existing?.updatedAt) { mutableStateOf(existing?.priceCents?.let { "%.2f".format(it / 100.0) }.orEmpty()) }
    var purchaseDate by remember(id, existing?.updatedAt) { mutableStateOf(existing?.purchaseDate ?: LocalDate.now().toString()) }
    var targetDate by remember(id, existing?.updatedAt) { mutableStateOf(existing?.targetDate.orEmpty()) }
    var status by remember(id, existing?.updatedAt) { mutableStateOf(existing?.status ?: AssetStatus.ACTIVE.name) }
    var imagePath by remember(id, existing?.updatedAt) { mutableStateOf(existing?.imagePath) }
    var note by remember(id, existing?.updatedAt) { mutableStateOf(existing?.note.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch { imagePath = copyImageToInternalStorage(context, uri) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (id == null) "新增资产" else "编辑资产") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                actions = { TextButton(onClick = {
                    val cents = parseYuanToCents(price)
                    val purchase = parseDateOrNull(purchaseDate)
                    val target = targetDate.takeIf { it.isNotBlank() }?.let(::parseDateOrNull)
                    error = when {
                        name.isBlank() -> "请填写资产名称"
                        cents == null || cents < 0 -> "请输入有效的金额"
                        purchase == null -> "购买日期格式应为 yyyy-MM-dd"
                        targetDate.isNotBlank() && target == null -> "目标日期格式应为 yyyy-MM-dd"
                        target != null && target.isBefore(purchase) -> "目标日期不能早于购买日期"
                        else -> null
                    }
                    if (error == null) {
                        val now = System.currentTimeMillis()
                        viewModel.saveAsset(
                            AssetEntity(
                                id = id ?: UUID.randomUUID().toString(),
                                name = name.trim(),
                                category = category,
                                priceCents = cents ?: 0,
                                purchaseDate = purchaseDate.trim(),
                                targetDate = targetDate.trim().ifBlank { null },
                                status = status,
                                imagePath = imagePath,
                                note = note.trim(),
                                createdAt = existing?.createdAt ?: now,
                                updatedAt = now
                            )
                        )
                        onBack()
                    }
                }) { Text("保存") } }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            RecordImagePicker(imagePath, category) { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
            FormTextField("名称", name, { name = it }, "例如：通勤车")
            ChipGroup("分类", listOf("数码电子", "交通工具", "家居", "学习", "其他"), category) { category = it }
            FormTextField("购买价格（元）", price, { price = it }, "0.00", KeyboardType.Decimal)
            DateInput("购买日期", purchaseDate) { purchaseDate = it }
            DateInput("目标日期（可选）", targetDate) { targetDate = it }
            ChipGroup("状态", AssetStatus.entries.map { it.label }, AssetStatus.valueOf(status).label) { label -> status = AssetStatus.entries.first { it.label == label }.name }
            FormTextField("备注（可选）", note, { note = it }, "补充说明")
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(30.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEditorScreen(id: String?, viewModel: MainViewModel, onBack: () -> Unit) {
    val existing = viewModel.expense(id)
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var type by remember(id, existing?.updatedAt) { mutableStateOf(existing?.type?.let { runCatching { ExpenseType.valueOf(it) }.getOrNull() } ?: ExpenseType.SUBSCRIPTION) }
    var name by remember(id, existing?.updatedAt) { mutableStateOf(existing?.name.orEmpty()) }
    var category by remember(id, existing?.updatedAt) { mutableStateOf(existing?.category ?: "数码电子") }
    var total by remember(id, existing?.updatedAt) { mutableStateOf(existing?.totalAmountCents?.takeIf { it > 0 }?.let { "%.2f".format(it / 100.0) }.orEmpty()) }
    var periodAmount by remember(id, existing?.updatedAt) { mutableStateOf(existing?.periodAmountCents?.let { "%.2f".format(it / 100.0) }.orEmpty()) }
    var unit by remember(id, existing?.updatedAt) { mutableStateOf(existing?.periodUnit?.let { runCatching { PeriodUnit.valueOf(it) }.getOrNull() } ?: PeriodUnit.MONTH) }
    var totalPeriods by remember(id, existing?.updatedAt) { mutableStateOf(existing?.totalPeriods?.takeIf { it > 0 }?.toString().orEmpty()) }
    var paidPeriods by remember(id, existing?.updatedAt) { mutableStateOf(existing?.paidPeriods?.toString() ?: "0") }
    var nextDueDate by remember(id, existing?.updatedAt) { mutableStateOf(existing?.nextDueDate ?: LocalDate.now().toString()) }
    var autoRenew by remember(id, existing?.updatedAt) { mutableStateOf(existing?.autoRenew ?: true) }
    var imagePath by remember(id, existing?.updatedAt) { mutableStateOf(existing?.imagePath) }
    var note by remember(id, existing?.updatedAt) { mutableStateOf(existing?.note.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch { imagePath = copyImageToInternalStorage(context, uri) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (id == null) "新增支出" else "编辑支出") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                actions = { TextButton(onClick = {
                    val totalCents = parseYuanToCents(total) ?: 0L
                    val periodCents = parseYuanToCents(periodAmount)
                    val periods = totalPeriods.toIntOrNull() ?: 0
                    val paid = paidPeriods.toIntOrNull() ?: 0
                    error = when {
                        name.isBlank() -> "请填写支出名称"
                        periodCents == null || periodCents < 0 -> "请输入有效的周期金额"
                        type == ExpenseType.INSTALLMENT && totalCents <= 0 -> "分期记录需要填写总金额"
                        type == ExpenseType.INSTALLMENT && periods <= 0 -> "请输入总期数"
                        type == ExpenseType.INSTALLMENT && (paid < 0 || paid > periods) -> "已付期数不合法"
                        parseDateOrNull(nextDueDate) == null -> "下次支付日期格式应为 yyyy-MM-dd"
                        else -> null
                    }
                    if (error == null) {
                        val now = System.currentTimeMillis()
                        viewModel.saveExpense(
                            ExpenseEntity(
                                id = id ?: UUID.randomUUID().toString(),
                                name = name.trim(),
                                category = category,
                                type = type.name,
                                totalAmountCents = if (type == ExpenseType.INSTALLMENT) totalCents else 0,
                                periodAmountCents = periodCents ?: 0,
                                periodUnit = unit.name,
                                totalPeriods = if (type == ExpenseType.INSTALLMENT) periods else 0,
                                paidPeriods = if (type == ExpenseType.INSTALLMENT) paid.coerceAtMost(periods) else (existing?.paidPeriods ?: 0),
                                nextDueDate = nextDueDate.trim(),
                                autoRenew = autoRenew,
                                status = existing?.status ?: ExpenseStatus.ACTIVE.name,
                                imagePath = imagePath,
                                note = note.trim(),
                                createdAt = existing?.createdAt ?: now,
                                updatedAt = now
                            )
                        )
                        onBack()
                    }
                }) { Text("保存") } }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            RecordImagePicker(imagePath, category) { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
            ChipGroup("类型", ExpenseType.entries.map { it.label }, type.label) { label -> type = ExpenseType.entries.first { it.label == label } }
            FormTextField("名称", name, { name = it }, "例如：家庭宽带")
            ChipGroup("分类", listOf("数码电子", "交通工具", "居住", "服务", "其他"), category) { category = it }
            if (type == ExpenseType.INSTALLMENT) {
                FormTextField("总金额（元）", total, { total = it }, "0.00", KeyboardType.Decimal)
            }
            FormTextField(if (type == ExpenseType.INSTALLMENT) "每期金额（元）" else "周期金额（元）", periodAmount, { periodAmount = it }, "0.00", KeyboardType.Decimal)
            ChipGroup("周期", PeriodUnit.entries.map { it.label }, unit.label) { label -> unit = PeriodUnit.entries.first { it.label == label } }
            if (type == ExpenseType.INSTALLMENT) {
                FormTextField("总期数", totalPeriods, { totalPeriods = it.filter(Char::isDigit) }, "例如：60", KeyboardType.Number)
                FormTextField("已付期数", paidPeriods, { paidPeriods = it.filter(Char::isDigit) }, "例如：7", KeyboardType.Number)
            } else {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("自动续费", style = MaterialTheme.typography.titleMedium)
                        Text("支付后自动推进下次日期", color = AppGray, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = autoRenew, onCheckedChange = { autoRenew = it })
                }
            }
            DateInput("下次支付日期", nextDueDate) { nextDueDate = it }
            FormTextField("备注（可选）", note, { note = it }, "补充说明")
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun RecordImagePicker(path: String?, category: String, onPick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        ImageThumbnail(path, Modifier.size(76.dp), category)
        Spacer(Modifier.width(15.dp))
        TextButton(onClick = onPick) {
            Icon(Icons.Default.Image, contentDescription = null)
            Spacer(Modifier.width(7.dp))
            Text(if (path == null) "添加图片" else "更换图片")
        }
    }
}

@Composable
private fun FormTextField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, keyboardType: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Composable
private fun ChipGroup(title: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelected(option) },
                    label = { Text(option) },
                    leadingIcon = { CategoryIcon(option, modifier = Modifier.size(18.dp)) }
                )
            }
        }
    }
}

@Composable
private fun DateInput(label: String, value: String, onValueChange: (String) -> Unit) {
    var showPicker by remember(value) { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth().clickable { showPicker = true }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            label = { Text(label) },
            placeholder = { Text("请选择日期") },
            singleLine = true,
            trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = AppOrange) },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = AppNavy,
                disabledLabelColor = AppGray,
                disabledPlaceholderColor = AppGray,
                disabledBorderColor = AppLine,
                disabledTrailingIconColor = AppOrange
            )
        )
    }
    if (showPicker) {
        WheelDatePickerDialog(
            initialDate = parseDateOrNull(value) ?: LocalDate.now(),
            onDismiss = { showPicker = false },
            onConfirm = { date ->
                onValueChange(date.toString())
                showPicker = false
            }
        )
    }
}

@Composable
private fun WheelDatePickerDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    var selectedDate by remember(initialDate) { mutableStateOf(initialDate) }
    Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Surface(
            modifier = Modifier.width(340.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            color = AppSurface
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("选择日期", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text("上下滚动选择年、月、日", color = AppGray, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                AndroidView(
                    modifier = Modifier.fillMaxWidth().height(170.dp),
                    factory = { viewContext ->
                        createDateWheel(viewContext, initialDate) { selectedDate = it }
                    },
                    update = {}
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("取消") }
                    Button(onClick = { onConfirm(selectedDate) }) { Text("确定") }
                }
            }
        }
    }
}

private fun createDateWheel(
    context: Context,
    initialDate: LocalDate,
    onDateChanged: (LocalDate) -> Unit
): LinearLayout {
    val yearStart = initialDate.year - 50
    val yearPicker = numberPicker(context)
    val monthPicker = numberPicker(context)
    val dayPicker = numberPicker(context)

    yearPicker.minValue = 0
    yearPicker.maxValue = 100
    yearPicker.displayedValues = (0..100).map { "${yearStart + it}年" }.toTypedArray()
    yearPicker.value = initialDate.year - yearStart

    monthPicker.minValue = 0
    monthPicker.maxValue = 11
    monthPicker.displayedValues = (1..12).map { "${it}月" }.toTypedArray()
    monthPicker.value = initialDate.monthValue - 1

    fun maxDay(): Int = java.time.YearMonth.of(yearStart + yearPicker.value, monthPicker.value + 1).lengthOfMonth()
    fun currentDate(): LocalDate = LocalDate.of(yearStart + yearPicker.value, monthPicker.value + 1, dayPicker.value + 1)
    fun refreshDayPicker() {
        val max = maxDay()
        val oldValue = dayPicker.value
        dayPicker.displayedValues = null
        dayPicker.minValue = 0
        dayPicker.maxValue = max - 1
        dayPicker.displayedValues = (1..max).map { "${it}日" }.toTypedArray()
        dayPicker.value = oldValue.coerceAtMost(max - 1)
    }

    dayPicker.minValue = 0
    dayPicker.maxValue = initialDate.lengthOfMonth() - 1
    dayPicker.displayedValues = (1..initialDate.lengthOfMonth()).map { "${it}日" }.toTypedArray()
    dayPicker.value = initialDate.dayOfMonth - 1

    yearPicker.setOnValueChangedListener { _, _, _ -> refreshDayPicker(); onDateChanged(currentDate()) }
    monthPicker.setOnValueChangedListener { _, _, _ -> refreshDayPicker(); onDateChanged(currentDate()) }
    dayPicker.setOnValueChangedListener { _, _, _ -> onDateChanged(currentDate()) }
    onDateChanged(initialDate)

    return LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = android.view.Gravity.CENTER
        addView(yearPicker)
        addView(monthPicker)
        addView(dayPicker)
    }
}

private fun numberPicker(context: Context): NumberPicker = NumberPicker(context).apply {
    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
    wrapSelectorWheel = false
    descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
}
