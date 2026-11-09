package com.andorid.ninmods.admin.ui.screens.support

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andorid.ninmods.admin.data.model.AdminTicketItem
import com.andorid.ninmods.admin.data.model.AdminUserItem
import com.andorid.ninmods.admin.ui.components.*
import com.andorid.ninmods.admin.ui.dialogs.NewTicketBottomSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun AdminSupportScreen(
    tickets: List<AdminTicketItem>,
    users: List<AdminUserItem> = emptyList(),
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onSelectTicket: (AdminTicketItem) -> Unit,
    onCreateTicket: (targetUserId: Int, subject: String, message: String, priority: String) -> Unit = { _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var showNewTicketSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(4000L) // Real-time poll tickets every 4 seconds
            onRefresh()
        }
    }

    val filterOptions = listOf("ALL", "OPEN", "ANSWERED", "CLOSED")

    val filteredTickets = remember(tickets, selectedFilter, searchQuery) {
        tickets.filter { item ->
            val matchesFilter = if (selectedFilter == "ALL") true else item.status.equals(selectedFilter, ignoreCase = true)
            val matchesSearch = if (searchQuery.isBlank()) true else {
                item.subject.contains(searchQuery, ignoreCase = true) ||
                item.username.contains(searchQuery, ignoreCase = true) ||
                item.email.contains(searchQuery, ignoreCase = true) ||
                item.id.toString() == searchQuery.trim()
            }
            matchesFilter && matchesSearch
        }
    }

    ResponsiveContentBox(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Bar
                AdminSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search messages by User, Subject, or ID...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )

                // Status Filter Tabs
                AdminFilterChipRow(
                    items = filterOptions,
                    selectedItem = selectedFilter,
                    onSelect = { selectedFilter = it }
                )

                if (isLoading && tickets.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (filteredTickets.isEmpty()) {
                    AdminEmptyState(
                        title = "No Support Tickets",
                        subtitle = "All customer inquiries matching the selected filter ($selectedFilter) have been resolved.",
                        icon = Icons.Filled.ConfirmationNumber,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredTickets, key = { it.id }) { item ->
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectTicket(item) },
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
                                            Text(
                                                text = item.subject,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "User: ${item.username} • Dept: ${item.department}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        StatusBadgePill(text = item.status, status = item.status)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Priority: ${item.priority.uppercase()}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.priority.equals("urgent", ignoreCase = true) || item.priority.equals("high", ignoreCase = true)) MetricRed else MetricOrange
                                        )

                                        Text(
                                            text = item.lastReplyAt ?: item.createdAt,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Compose New Ticket FAB
            FloatingActionButton(
                onClick = { showNewTicketSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Compose Message",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    if (showNewTicketSheet) {
        NewTicketBottomSheet(
            users = users,
            onDismiss = { showNewTicketSheet = false },
            onCreateTicket = { uid, subj, msg, prio ->
                onCreateTicket(uid, subj, msg, prio)
            }
        )
    }
}
