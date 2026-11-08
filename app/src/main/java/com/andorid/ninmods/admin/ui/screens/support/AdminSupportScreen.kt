package com.andorid.ninmods.admin.ui.screens.support

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andorid.ninmods.admin.data.model.AdminTicketItem
import com.andorid.ninmods.admin.data.model.AdminTicketReplyItem
import com.andorid.ninmods.admin.ui.components.*
import com.andorid.ninmods.admin.ui.dialogs.NewTicketBottomSheet

@Composable
fun AdminSupportScreen(
    tickets: List<AdminTicketItem>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onLoadTicketReplies: (ticketId: Int, onResult: (List<AdminTicketReplyItem>) -> Unit) -> Unit,
    onReplyTicket: (ticketId: Int, message: String, onComplete: () -> Unit) -> Unit,
    onUpdateTicketStatus: (ticketId: Int, status: String) -> Unit,
    onCreateTicket: (targetUserId: Int, subject: String, message: String, priority: String) -> Unit = { _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    var activeTicketForChat by remember { mutableStateOf<AdminTicketItem?>(null) }
    var repliesList by remember { mutableStateOf<List<AdminTicketReplyItem>>(emptyList()) }
    var isLoadingReplies by remember { mutableStateOf(false) }
    var showNewTicketSheet by remember { mutableStateOf(false) }

    val filterOptions = listOf("ALL", "OPEN", "ANSWERED", "CLOSED")

    val filteredTickets = remember(tickets, selectedFilter) {
        tickets.filter {
            if (selectedFilter == "ALL") true else it.status.equals(selectedFilter, ignoreCase = true)
        }
    }

    val chatListState = rememberLazyListState()

    // Automatic reply fetching when a ticket is opened
    LaunchedEffect(activeTicketForChat?.id) {
        val ticket = activeTicketForChat
        if (ticket != null) {
            isLoadingReplies = true
            onLoadTicketReplies(ticket.id) { replies ->
                repliesList = replies
                isLoadingReplies = false
            }
        } else {
            repliesList = emptyList()
        }
    }

    LaunchedEffect(repliesList.size) {
        if (repliesList.isNotEmpty()) {
            chatListState.animateScrollToItem(repliesList.size - 1)
        }
    }

    if (activeTicketForChat != null) {
        val ticket = activeTicketForChat!!
        var replyInputText by remember { mutableStateOf("") }
        var isSubmittingReply by remember { mutableStateOf(false) }

        ResponsiveContentBox(modifier = modifier) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Ticket Detail Header Card
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { activeTicketForChat = null }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Queue",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ticket.subject,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Ticket #${ticket.id} • ${ticket.username}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        StatusBadgePill(text = ticket.status, status = ticket.status)

                        IconButton(onClick = {
                            val newStatus = if (ticket.status.equals("closed", ignoreCase = true)) "open" else "closed"
                            onUpdateTicketStatus(ticket.id, newStatus)
                            activeTicketForChat = ticket.copy(status = newStatus)
                        }) {
                            Icon(
                                imageVector = if (ticket.status.equals("closed", ignoreCase = true)) Icons.Filled.LockOpen else Icons.Filled.Lock,
                                contentDescription = "Toggle Status",
                                tint = if (ticket.status.equals("closed", ignoreCase = true)) MetricGreen else MetricRed
                            )
                        }
                    }
                }

                // Message Bubble History
                if (isLoadingReplies) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    LazyColumn(
                        state = chatListState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(repliesList, key = { it.id }) { reply ->
                            val isAdmin = reply.adminId != null
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isAdmin) Arrangement.End else Arrangement.Start
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isAdmin) 16.dp else 4.dp,
                                        bottomEnd = if (isAdmin) 4.dp else 16.dp
                                    ),
                                    color = if (isAdmin) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.widthIn(max = 280.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = if (isAdmin) "Staff Support" else (reply.userName ?: ticket.username),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = reply.message,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isAdmin) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = reply.createdAt,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Reply Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = replyInputText,
                            onValueChange = { replyInputText = it },
                            placeholder = { Text("Write response to client...", style = MaterialTheme.typography.bodyMedium) },
                            singleLine = false,
                            maxLines = 4,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (replyInputText.isNotBlank() && !isSubmittingReply) {
                                    isSubmittingReply = true
                                    onReplyTicket(ticket.id, replyInputText) {
                                        replyInputText = ""
                                        isSubmittingReply = false
                                        onLoadTicketReplies(ticket.id) { replies ->
                                            repliesList = replies
                                        }
                                    }
                                }
                            },
                            enabled = replyInputText.isNotBlank() && !isSubmittingReply,
                            modifier = Modifier.size(48.dp)
                        ) {
                            if (isSubmittingReply) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = if (replyInputText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Tickets List View with Floating Action Button
        ResponsiveContentBox(modifier = modifier) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
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
                                        .clickable { activeTicketForChat = item },
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

                // Standard Floating Action Button at Bottom-Right
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
    }

    if (showNewTicketSheet) {
        NewTicketBottomSheet(
            onDismiss = { showNewTicketSheet = false },
            onCreateTicket = { uid, subj, msg, prio ->
                onCreateTicket(uid, subj, msg, prio)
            }
        )
    }
}
