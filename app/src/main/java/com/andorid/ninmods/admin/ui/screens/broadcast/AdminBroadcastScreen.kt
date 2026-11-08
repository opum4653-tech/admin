package com.andorid.ninmods.admin.ui.screens.broadcast

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andorid.ninmods.admin.data.model.AdminUserItem
import com.andorid.ninmods.admin.ui.components.LoadingButton
import com.andorid.ninmods.admin.ui.components.ResponsiveContentBox

@Composable
fun AdminBroadcastScreen(
    users: List<AdminUserItem>,
    onSendNotification: (title: String, message: String, type: String, imageUrl: String?, isPopup: Boolean, targetUserId: Int?, onComplete: (Boolean, String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("info") } // info, warning, urgent, update
    var isPopup by remember { mutableStateOf(true) }
    var imageUrl by remember { mutableStateOf("") }
    var targetAllUsers by remember { mutableStateOf(true) }
    var targetUserIdText by remember { mutableStateOf("") }

    var isSending by remember { mutableStateOf(false) }

    ResponsiveContentBox(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // CARD 1: MESSAGE CONTENT
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Campaign,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BROADCAST ALERT CONTENT",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Broadcast Title") },
                        placeholder = { Text("e.g. Critical Game Engine Update") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message Body") },
                        placeholder = { Text("Write full message or announcement text...") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6
                    )

                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Banner Image URL (Optional)") },
                        placeholder = { Text("https://example.com/banner.png") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // CARD 2: DELIVERY MODE & TYPE
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.DisplaySettings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DELIVERY & PRIORITY SETTINGS",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(text = "Priority Level:", style = MaterialTheme.typography.bodyMedium)

                    val types = listOf(
                        Pair("info", "Information"),
                        Pair("warning", "Warning"),
                        Pair("urgent", "Urgent Alert"),
                        Pair("update", "App Update")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        types.forEach { (typeKey, label) ->
                            val isSelected = selectedType == typeKey
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedType = typeKey },
                                label = { Text(label) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // In-app Popup vs Normal Notification Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "In-App Modal Dialog",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Displays a centered modal dialog when user opens the client app",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = isPopup, onCheckedChange = { isPopup = it })
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Target Audience
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Broadcast to All Users",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (targetAllUsers) "All connected user accounts will receive this alert" else "Targeting single user account",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = targetAllUsers, onCheckedChange = { targetAllUsers = it })
                    }

                    if (!targetAllUsers) {
                        OutlinedTextField(
                            value = targetUserIdText,
                            onValueChange = { targetUserIdText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Target User ID") },
                            placeholder = { Text("e.g. 15") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // SUBMIT BUTTON WITH SPINNER
            LoadingButton(
                text = "SEND BROADCAST ALERT",
                isLoading = isSending,
                onClick = {
                    if (title.isBlank() || message.isBlank()) {
                        Toast.makeText(context, "Title and message are required", Toast.LENGTH_SHORT).show()
                        return@LoadingButton
                    }

                    val targetId = if (targetAllUsers) null else targetUserIdText.toIntOrNull()
                    isSending = true
                    onSendNotification(
                        title.trim(),
                        message.trim(),
                        selectedType,
                        imageUrl.ifBlank { null },
                        isPopup,
                        targetId
                    ) { _, msg ->
                        isSending = false
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        title = ""
                        message = ""
                        imageUrl = ""
                    }
                },
                icon = Icons.AutoMirrored.Filled.Send,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
