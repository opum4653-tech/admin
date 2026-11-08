package com.andorid.ninmods.admin.ui.screens.bugs

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andorid.ninmods.admin.data.model.AdminBugItem
import com.andorid.ninmods.admin.ui.components.*

@Composable
fun AdminBugReportsScreen(
    bugs: List<AdminBugItem>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onUpdateStatus: (bugId: Int, status: String, notes: String, onComplete: (Boolean, String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val filterOptions = listOf("ALL", "OPEN", "INVESTIGATING", "RESOLVED", "CLOSED")

    val filteredBugs = remember(bugs, selectedFilter) {
        bugs.filter {
            if (selectedFilter == "ALL") true else it.status.equals(selectedFilter, ignoreCase = true)
        }
    }

    ResponsiveContentBox(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Filter Row
            AdminFilterChipRow(
                items = filterOptions,
                selectedItem = selectedFilter,
                onSelect = { selectedFilter = it }
            )

            if (isLoading && bugs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (filteredBugs.isEmpty()) {
                AdminEmptyState(
                    title = "No Bug Reports",
                    subtitle = "No crash or client telemetry reports matching '$selectedFilter'.",
                    icon = Icons.Filled.BugReport,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredBugs, key = { it.id }) { item ->
                        var isExpanded by remember { mutableStateOf(false) }

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
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Header: Title + Status
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Report #${item.id} • ${item.createdAt}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    StatusBadgePill(text = item.status, status = item.status)
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                // Device Telemetry Row
                                Text(
                                    text = "Device: ${item.deviceModel} • ${item.androidVersion ?: "Android"} • ${if (item.isRoot) "Rooted" else "Non-Root"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (!item.kernelVersion.isNullOrBlank()) {
                                    Text(
                                        text = "Kernel: ${item.kernelVersion}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (!item.description.isNullOrBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = item.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }

                                // Expandable Logcat Trace
                                if (!item.logcatTrace.isNullOrBlank()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { isExpanded = !isExpanded },
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (isExpanded) "Hide Diagnostic Trace" else "View Diagnostic Trace (${item.logcatTrace.length} chars)",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    AnimatedVisibility(visible = isExpanded) {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.surface,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.End
                                                    ) {
                                                        IconButton(
                                                            onClick = {
                                                                clipboardManager.setText(AnnotatedString(item.logcatTrace))
                                                                Toast.makeText(context, "Log trace copied", Toast.LENGTH_SHORT).show()
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Filled.ContentCopy,
                                                                contentDescription = "Copy",
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = item.logcatTrace.take(1500),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontFamily = FontFamily.Monospace,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Status Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (item.status.lowercase() != "resolved") {
                                        Button(
                                            onClick = {
                                                onUpdateStatus(item.id, "resolved", "Resolved by admin") { ok, msg ->
                                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MetricGreen),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Mark Resolved", color = MaterialTheme.colorScheme.surface)
                                        }
                                    }

                                    if (item.status.lowercase() != "closed") {
                                        OutlinedButton(
                                            onClick = {
                                                onUpdateStatus(item.id, "closed", "Closed") { ok, msg ->
                                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Close")
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
}
