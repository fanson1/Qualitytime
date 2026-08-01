package com.finley.android.qualitytime.ui.poem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: PoemState,
    onRateChange: (Float) -> Unit,
    onAutoPlayToggle: (Boolean) -> Unit,
    onTogglePinyin: () -> Unit,
    onVoiceChange: (String) -> Unit,
    onRefreshVoices: () -> Unit = {}
) {
    var showVoiceDialog by remember { mutableStateOf(false) }

    if (showVoiceDialog) {
        AlertDialog(
            onDismissRequest = { showVoiceDialog = false },
            title = { 
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("选择朗读人", fontWeight = FontWeight.Bold)
                    IconButton(onClick = onRefreshVoices) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                }
            },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(state.availableVoices) { voice ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    onVoiceChange(voice.id)
                                    showVoiceDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = voice.id == state.selectedVoiceId,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(voice.name, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    if (state.availableVoices.isEmpty()) {
                        item {
                            Text(
                                "系统未检测到可选语音",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVoiceDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 播放设置
        SettingsCard(title = "播放设置") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("自动连读", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text("当前诗词读完后自动播放下一首", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = state.isAutoPlay, onCheckedChange = onAutoPlayToggle)
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            
            val displayRate = ((state.speechRate * 10).toInt() / 10.0).toString()
            Column {
                Text("语速倍率: ${displayRate}x", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Slider(
                    value = state.speechRate,
                    onValueChange = onRateChange,
                    valueRange = 0.5f..2.0f,
                    steps = 15
                )
            }
        }

        // 显示设置
        SettingsCard(title = "显示设置") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("显示全文拼音", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text("在古诗正文上方标注拼音", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = state.showPinyin, onCheckedChange = { onTogglePinyin() })
            }
        }

        // 语音设置
        SettingsCard(title = "语音引擎") {
            val currentVoiceName = state.availableVoices.find { it.id == state.selectedVoiceId }?.name ?: "系统默认"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("朗读人音色", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text(currentVoiceName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
                Button(
                    onClick = { showVoiceDialog = true },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("更换")
                }
            }
        }
    }
}

@Composable
fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}
