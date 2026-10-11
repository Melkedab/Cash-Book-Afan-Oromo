package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppStrings
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IncomeGreen
import kotlinx.coroutines.launch

@Composable
fun BackupSyncScreen(
    strings: AppStrings,
    onExportJson: suspend () -> String,
    onImportJson: suspend (String, Boolean) -> Boolean,
    onExportCsv: () -> String,
    onShareSummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showExportDialog by remember { mutableStateOf(false) }
    var exportedContent by remember { mutableStateOf("") }
    var exportedContentType by remember { mutableStateOf("JSON") }

    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var importErrorMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("backup_sync_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Privacy Banner Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(IncomeGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = IncomeGreen)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = strings.offlineSyncNotice,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.offlineSyncDesc,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Actions: JSON Backup
        item {
            SyncActionCard(
                icon = Icons.Default.CloudUpload,
                iconTint = EmeraldPrimary,
                title = strings.exportJson,
                description = strings.exportJsonDesc,
                buttonLabel = "Export JSON",
                onClick = {
                    scope.launch {
                        val json = onExportJson()
                        exportedContent = json
                        exportedContentType = "JSON Backup"
                        showExportDialog = true
                    }
                }
            )
        }

        // Action: JSON Restore
        item {
            SyncActionCard(
                icon = Icons.Default.CloudDownload,
                iconTint = Color(0xFF0284C7),
                title = strings.importJson,
                description = strings.importJsonDesc,
                buttonLabel = "Import JSON",
                onClick = {
                    importJsonText = ""
                    importErrorMessage = null
                    showImportDialog = true
                }
            )
        }

        // Action: CSV Export (Excel)
        item {
            SyncActionCard(
                icon = Icons.Default.TableChart,
                iconTint = Color(0xFF059669),
                title = strings.exportCsv,
                description = strings.exportCsvDesc,
                buttonLabel = "Export CSV",
                onClick = {
                    val csv = onExportCsv()
                    exportedContent = csv
                    exportedContentType = "CSV Report"
                    showExportDialog = true
                }
            )
        }

        // Action: Share Report via Apps
        item {
            SyncActionCard(
                icon = Icons.Default.Share,
                iconTint = Color(0xFF7C3AED),
                title = strings.shareReport,
                description = strings.shareReportDesc,
                buttonLabel = "Share",
                onClick = onShareSummary
            )
        }

        // Android Studio Device Sync Guide
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FolderShared, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Android Studio Device File Sync",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. In Android Studio, open View > Tool Windows > Device File Explorer.\n2. Navigate to: /data/data/com.aistudio.cashbook.gkmzpq/databases/\n3. You can synchronize or pull galmee_qarshii_database or inspect it in App Inspection > Database Inspector anytime!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(60.dp)) }
    }

    // Export Result Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text(exportedContentType, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "You can copy this data or share it directly to backup your ledger offline:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = exportedContent,
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, exportedContent)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, exportedContentType))
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share / Save")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(exportedContentType, exportedContent)
                        clipboard.setPrimaryClip(clip)
                        showExportDialog = false
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy")
                }
            }
        )
    }

    // Import JSON Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text(strings.importJson, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Paste your previously exported JSON backup below to restore all ledger records:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = {
                            importJsonText = it
                            importErrorMessage = null
                        },
                        placeholder = { Text("{\n  \"appName\": \"Galmee Qarshii\"...\n}") },
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    if (importErrorMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = importErrorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            if (importJsonText.isBlank()) {
                                importErrorMessage = "Please paste JSON backup text"
                            } else {
                                val success = onImportJson(importJsonText, false)
                                if (success) {
                                    showImportDialog = false
                                } else {
                                    importErrorMessage = strings.invalidBackupFile
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Restore Database")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showImportDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
private fun SyncActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    description: String,
    buttonLabel: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = iconTint),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = buttonLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
