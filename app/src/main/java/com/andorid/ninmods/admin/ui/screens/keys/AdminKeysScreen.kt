package com.andorid.ninmods.admin.ui.screens.keys

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.andorid.ninmods.admin.data.model.AdminLicenseKeyItem
import com.andorid.ninmods.admin.ui.components.*
import com.andorid.ninmods.admin.ui.dialogs.KeyActionBottomSheet

@Composable
fun AdminKeysScreen(
    keys: List<AdminLicenseKeyItem>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onActionClick: (keyId: Int, action: String, hours: Int) -> Unit,
    onOpenBulkGenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
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

    ResponsiveContentBox(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Enterprise Search & Action Row
            AdminSearchAndActionRow(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search by Key, HWID, Model...",
                actionButtonText = "Generate",
                actionButtonIcon = Icons.Filled.Add,
                onActionClick = onOpenBulkGenerate
            )

            // Horizontal Filter Pills Row
            AdminFilterChipRow(
                items = filterOptions,
                selectedItem = selectedFilter,
                onSelect = { selectedFilter = it }
            )

            // Keys Content
            if (isLoading && keys.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (filteredKeys.isEmpty()) {
                AdminEmptyState(
                    title = "No License Keys Found",
                    subtitle = "No license keys match the selected status filter ($selectedFilter) or search query.",
                    icon = Icons.Filled.VpnKey,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredKeys, key = { it.id }) { item ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
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
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                imageVector = Icons.Filled.ContentCopy,
                                                contentDescription = "Copy",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clickable {
                                                        clipboardManager.setText(AnnotatedString(item.licenseKey))
                                                    }
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "${item.durationHours} Hours (${item.durationType.uppercase()}) • Devices: ${item.maxDevices}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        if (!item.hwid.isNullOrBlank()) {
                                            Text(
                                                text = "HWID: ${item.hwid.take(16)}... • ${item.deviceModel ?: "Unknown"}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontFamily = FontFamily.Monospace,
                                                color = MetricBlue,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        if (!item.expiresAt.isNullOrBlank()) {
                                            Text(
                                                text = "Expires: ${item.expiresAt}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(horizontalAlignment = Alignment.End) {
                                        StatusBadgePill(text = item.status, status = item.status)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        IconButton(
                                            onClick = { actionDialogKey = item },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.MoreVert,
                                                contentDescription = "Actions",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Key Action Bottom Sheet (replaces center popup)
    actionDialogKey?.let { k ->
        KeyActionBottomSheet(
            keyItem = k,
            onDismiss = { actionDialogKey = null },
            onActionClick = onActionClick
        )
    }
}
