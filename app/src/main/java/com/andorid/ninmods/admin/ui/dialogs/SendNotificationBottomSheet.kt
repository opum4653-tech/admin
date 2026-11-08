package com.andorid.ninmods.admin.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendNotificationBottomSheet(
    onDismiss: () -> Unit,
    onSendNotification: (title: String, message: String, type: String, imageUrl: String?, isPopup: Boolean, targetUserId: Int?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("info") }
    var isPopup by remember { mutableStateOf(true) }
    var targetMode by remember { mutableStateOf("all") } // "all" or "user"
    var targetUserIdText by remember { mutableStateOf("") }

    AdminCustomBottomSheet(
        title = "Push User Popup / Notification",
        subtitle = "Broadcast real-time popups with image banners to DriverLoader clients",
        onDismissRequest = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Target Selection Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = targetMode == "all",
                    onClick = { targetMode = "all" },
                    label = { Text("Broadcast to All") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                FilterChip(
                    selected = targetMode == "user",
                    onClick = { targetMode = "user" },
                    label = { Text("Specific User ID") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (targetMode == "user") {
                OutlinedTextField(
                    value = targetUserIdText,
                    onValueChange = { targetUserIdText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Target User ID (#)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
            }

            // Notification Type Selection
            Text(
                text = "Popup Style / Priority",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Pair("Info", "info"),
                    Pair("Alert", "warning"),
                    Pair("Update", "update"),
                    Pair("Maintenance", "maintenance")
                ).forEach { (label, code) ->
                    val isSelected = selectedType == code
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedType = code },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Notification Title") },
                placeholder = { Text("e.g. Mandatory Game Update V90") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            // Message Body
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Message Body") },
                placeholder = { Text("Detailed notice text visible inside client app popup...") },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            // Image URL Banner
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Banner Image URL (Optional)") },
                placeholder = { Text("https://example.com/banner.png") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            // Popup Toggle Switch
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Launch as Screen Popup Dialog",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Forces modal pop-up on user app startup",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = isPopup, onCheckedChange = { isPopup = it })
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Button
            Button(
                onClick = {
                    val uid = if (targetMode == "user") targetUserIdText.toIntOrNull() else null
                    onSendNotification(
                        title.trim(),
                        message.trim(),
                        selectedType,
                        imageUrl.trim().takeIf { it.isNotEmpty() },
                        isPopup,
                        uid
                    )
                    onDismiss()
                },
                enabled = title.isNotBlank() && message.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Campaign,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Broadcast Popup Now",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
