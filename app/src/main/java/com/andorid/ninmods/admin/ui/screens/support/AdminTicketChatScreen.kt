package com.andorid.ninmods.admin.ui.screens.support

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.andorid.ninmods.admin.ui.components.ResponsiveContentBox
import com.andorid.ninmods.admin.ui.components.StatusBadgePill
import kotlinx.coroutines.launch

@Composable
fun AdminTicketChatScreen(
    ticket: AdminTicketItem,
    onNavigateBack: () -> Unit,
    onLoadReplies: (ticketId: Int, onResult: (List<AdminTicketReplyItem>) -> Unit) -> Unit,
    onReplyTicket: (ticketId: Int, message: String, onComplete: () -> Unit) -> Unit,
    onUpdateStatus: (ticketId: Int, status: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var replies by remember { mutableStateOf<List<AdminTicketReplyItem>>(emptyList()) }
    var isLoadingReplies by remember { mutableStateOf(true) }
    var replyText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    fun refreshReplies() {
        onLoadReplies(ticket.id) { list ->
            replies = list
            isLoadingReplies = false
            scope.launch {
                if (list.isNotEmpty()) {
                    listState.animateScrollToItem(list.size - 1)
                }
            }
        }
    }

    LaunchedEffect(ticket.id) {
        refreshReplies()
    }

    ResponsiveContentBox(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ticket #${ticket.id}: ${ticket.subject}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = "User: ${ticket.username} (${ticket.email})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        StatusBadgePill(text = ticket.status, status = ticket.status)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (ticket.status.lowercase() != "closed") {
                            FilledTonalButton(
                                onClick = { onUpdateStatus(ticket.id, "closed") },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Close Ticket")
                            }
                        } else {
                            FilledTonalButton(
                                onClick = { onUpdateStatus(ticket.id, "open") },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reopen Ticket")
                            }
                        }

                        FilledTonalButton(
                            onClick = { refreshReplies() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Refresh")
                        }
                    }
                }
            }

            // Message Bubble History
            if (isLoadingReplies) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Original Ticket Prompt
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "ORIGINAL TICKET TOPIC",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ticket.subject,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Department: ${ticket.department} • Priority: ${ticket.priority.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Created: ${ticket.createdAt}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Replies Thread
                    items(replies, key = { it.id }) { reply ->
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
                                modifier = Modifier.widthIn(max = 320.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = if (isAdmin) (reply.adminName ?: "Staff Admin") else (reply.userName ?: ticket.username),
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
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.align(Alignment.End)
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
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("Write reply to user...", style = MaterialTheme.typography.bodyMedium) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f),
                        maxLines = 3
                    )

                    IconButton(
                        onClick = {
                            if (replyText.isNotBlank() && !isSending) {
                                isSending = true
                                onReplyTicket(ticket.id, replyText.trim()) {
                                    isSending = false
                                    replyText = ""
                                    refreshReplies()
                                }
                            }
                        },
                        enabled = replyText.isNotBlank() && !isSending
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (replyText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
