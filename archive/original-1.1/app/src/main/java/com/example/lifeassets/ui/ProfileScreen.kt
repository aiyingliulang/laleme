package com.example.lifeassets.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lifeassets.data.ImportMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var exportFormat by remember { mutableStateOf<String?>(null) }
    var importBytes by remember { mutableStateOf<ByteArray?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showImportMode by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var showReplaceConfirm by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri != null && exportFormat != null) {
            scope.launch {
                val bytes = if (exportFormat == "json") viewModel.exportJson() else viewModel.exportCsv()
                withContext(Dispatchers.IO) { context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) } }
                message = "已导出到所选位置"
                exportFormat = null
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            importBytes = withContext(Dispatchers.IO) { context.contentResolver.openInputStream(uri)?.use { it.readBytes() } }
            showImportMode = importBytes != null
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(top = 18.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("我的", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            Icon(Icons.Default.Settings, contentDescription = "设置", tint = AppNavy)
        }
        Card(colors = CardDefaults.cardColors(containerColor = AppSurface), shape = RoundedCornerShape(25.dp)) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("本地数据", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                Text("所有记录只保存在这台手机上，不需要注册和联网。", color = AppGray)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("资产 ${state.allAssets.size} 项")
                    Text("支出 ${state.allExpenses.size} 项")
                }
            }
        }
        Text("备份与恢复", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
        OutlinedButton(onClick = { exportFormat = "json"; exportLauncher.launch("人生资产备份.json") }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.FileDownload, contentDescription = null)
            Spacer(Modifier.padding(4.dp))
            Text("导出 JSON 备份")
        }
        OutlinedButton(onClick = { exportFormat = "csv"; exportLauncher.launch("人生资产导出.csv") }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.FileDownload, contentDescription = null)
            Spacer(Modifier.padding(4.dp))
            Text("导出 CSV 表格")
        }
        OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.FileUpload, contentDescription = null)
            Spacer(Modifier.padding(4.dp))
            Text("导入 JSON 备份")
        }
        Spacer(Modifier.height(8.dp))
        Text("危险操作", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
        Button(onClick = { showClearConfirm = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.DeleteForever, contentDescription = null)
            Spacer(Modifier.padding(4.dp))
            Text("清空本机所有数据")
        }
    }

    message?.let { text ->
        AlertDialog(onDismissRequest = { message = null }, title = { Text("操作完成") }, text = { Text(text) }, confirmButton = { TextButton(onClick = { message = null }) { Text("知道了") } })
    }
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("清空所有数据？") },
            text = { Text("资产、支出和本地记录都会被删除，此操作无法撤销。请确认你已经完成备份。") },
            confirmButton = { Button(onClick = { viewModel.clearAll(); showClearConfirm = false }) { Text("确认清空") } },
            dismissButton = { TextButton(onClick = { showClearConfirm = false }) { Text("取消") } }
        )
    }
    if (showImportMode) {
        AlertDialog(
            onDismissRequest = { showImportMode = false; importBytes = null },
            title = { Text("导入备份") },
            text = { Text("合并导入会保留现有记录；全部替换会先删除本机现有记录。") },
            confirmButton = {
                Button(onClick = {
                    scope.launch { importBytes?.let { viewModel.importJson(it, ImportMode.MERGE) }; showImportMode = false; importBytes = null; message = "已合并导入" }
                }) { Text("合并导入") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImportMode = false
                    showReplaceConfirm = true
                }) { Text("全部替换") }
            }
        )
    }
    if (showReplaceConfirm) {
        AlertDialog(
            onDismissRequest = { showReplaceConfirm = false; importBytes = null },
            title = { Text("确认替换本机数据？") },
            text = { Text("当前资产和支出记录会先被删除，再写入备份内容。请确认已经备份当前数据。") },
            confirmButton = {
                Button(onClick = {
                    scope.launch { importBytes?.let { viewModel.importJson(it, ImportMode.REPLACE) }; showReplaceConfirm = false; importBytes = null; message = "已替换本机数据" }
                }) { Text("确认替换") }
            },
            dismissButton = { TextButton(onClick = { showReplaceConfirm = false; importBytes = null }) { Text("取消") } }
        )
    }
}
