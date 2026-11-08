package com.andorid.ninmods.admin.ui.screens.keys

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andorid.ninmods.admin.data.model.AdminLicenseKeyItem
import com.andorid.ninmods.admin.ui.components.AdminTopBar
import com.andorid.ninmods.admin.ui.components.CyberCard
import com.andorid.ninmods.admin.ui.components.StatusBadgePill
import com.andorid.ninmods.admin.ui.theme.*

@Composable
fun AdminKeysScreen(
    keys: List<AdminLicenseKeyItem>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onActionClick: (keyId: Int, action: String, hours: Int) -> Unit,
    onGenerateBulk: (quantity: Int, durationHours: Int, durationType: String, gameType: String, maxDevices: Int, notes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var showBulkDialog by remember { mutableStateOf(false) }
    var actionDialogKey by remember { mutableStateOf<AdminLicenseKeyItem?>(null) }
    val clipboardManager = LocalClipboardManager.current

    val filterOptions = listOf("ALL", "ACTIVE", "UNUSED", "EXPIRED", "BANNED")

    val filteredKeys = remember(keys, searchQuery, selectedFilter) {
        keys.filter { item ->
            val matchesFilter = when (selectedFilter) {
                "ALL" -> true
                else -> item.status.equals(selectedFilter, ignoreCase = true)
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                item.licenseKey.contains(searchQuery, ignoreCase = true) ||
                (item.hwid?.contains(searchQuery, ignoreCase = true) ?: false) ||
                (item.deviceModel?.contains(searchQuery, ignoreCase = true) ?: false)
            }
            matchesFilter && matchesSearch
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            AdminTopBar(
                title = "LICENSE KEYS",
                subtitle = "${keys.size} keys synchronized",
                onRefresh = onRefresh,
                actionIcon = Icons.Filled.Add,
                onActionClick = { showBulkDialog = true }
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by Key, HWID, Device...", color = TextSecondary) },
                leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = CyberPurple) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberPurple,
                    unfocusedBorderColor = CyberBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { opt ->
                    val isSelected = selectedFilter == opt
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) CyberPurple else CyberSurface,
                        modifier = Modifier.clickable { selectedFilter = opt }
                    ) {
                        Text(
                            text = opt,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else TextSecondary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Keys List
            if (isLoading && keys.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CyberPurple)
                }
            } else if (filteredKeys.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No keys match the criteria", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredKeys, key = { it.id }) { item ->
                        CyberCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.licenseKey,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Filled.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = CyberPurple,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable {
                                                    clipboardManager.setText(AnnotatedString(item.licenseKey))
                                                }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "Duration: ${item.durationHours} hrs (${item.durationType}) • Devices: ${item.maxDevices}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )

                                    if (!item.hwid.isNullOrBlank()) {
                                        Text(
                                            text = "HWID: ${item.hwid.take(16)}... • Model: ${item.deviceModel ?: "Unknown"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = FontFamily.Monospace,
                                            color = CyberCyan,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (!item.expiresAt.isNullOrBlank()) {
                                        Text(
                                            text = "Expires: ${item.expiresAt}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextTertiary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(horizontalAlignment = Alignment.End) {
                                    StatusBadgePill(text = item.status, status = item.status)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    IconButton(
                                        onClick = { actionDialogKey = item },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.MoreVert,
                                            contentDescription = "Actions",
                                            tint = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Options Dialog
        actionDialogKey?.let { k ->
            AlertDialog(
                onDismissRequest = { actionDialogKey = null },
                containerColor = CyberCardBg,
                shape = RoundedCornerShape(18.dp),
                title = {
                    Text(text = "Manage Key: ${k.licenseKey}", color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Select management action to execute:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)

                        Button(
                            onClick = {
                                onActionClick(k.id, "reset_hwid", 0)
                                actionDialogKey = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Filled.RestartAlt, contentDescription = null, tint = CyberCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset Hardware ID (HWID)", color = CyberCyan, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                onActionClick(k.id, "extend", 24)
                                actionDialogKey = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPurple.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Filled.Timer, contentDescription = null, tint = CyberPurple)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Extend Duration (+24 Hours)", color = CyberPurple, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                onActionClick(k.id, "extend", 168)
                                actionDialogKey = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPurple.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Filled.Timer, contentDescription = null, tint = CyberPurple)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Extend Duration (+7 Days)", color = CyberPurple, fontWeight = FontWeight.Bold)
                        }

                        if (k.status.lowercase() == "banned") {
                            Button(
                                onClick = {
                                    onActionClick(k.id, "unban", 0)
                                    actionDialogKey = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberGreen.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Filled.LockOpen, contentDescription = null, tint = CyberGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Unban Key", color = CyberGreen, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    onActionClick(k.id, "ban", 0)
                                    actionDialogKey = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberRed.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Filled.Block, contentDescription = null, tint = CyberRed)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ban License Key", color = CyberRed, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { actionDialogKey = null }) {
                        Text("CLOSE", color = TextSecondary)
                    }
                }
            )
        }

        // Bulk Generator Dialog
        if (showBulkDialog) {
            BulkKeyGeneratorDialog(
                onDismiss = { showBulkDialog = false },
                onGenerate = { q, dh, dt, gt, md, n ->
                    onGenerateBulk(q, dh, dt, gt, md, n)
                    showBulkDialog = false
                }
            )
        }
    }
}
