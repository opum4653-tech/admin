package com.andorid.ninmods.admin.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTicketBottomSheet(
    onDismiss: () -> Unit,
    onCreateTicket: (targetUserId: Int, subject: String, message: String, priority: String) -> Unit
) {
    var userIdText by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("medium") }

    AdminCustomBottomSheet(
        title = "Compose User Direct Message",
        subtitle = "Initiate support communication thread with a client",
        onDismissRequest = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(
                value = userIdText,
                onValueChange = { userIdText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Recipient User ID (#)") },
                placeholder = { Text("e.g. 1") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            // Priority Selection
            Text(
                text = "Priority Level",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Pair("Low", "low"),
                    Pair("Medium", "medium"),
                    Pair("High", "high")
                ).forEach { (label, code) ->
                    val isSelected = selectedPriority == code
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPriority = code },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject / Topic") },
                placeholder = { Text("e.g. Account Security Alert") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Message Body") },
                placeholder = { Text("Type your direct message to the user...") },
                minLines = 4,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val uid = userIdText.toIntOrNull() ?: 0
                    if (uid > 0 && subject.isNotBlank() && message.isNotBlank()) {
                        onCreateTicket(uid, subject.trim(), message.trim(), selectedPriority)
                        onDismiss()
                    }
                },
                enabled = (userIdText.toIntOrNull() ?: 0) > 0 && subject.isNotBlank() && message.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Dispatch Message",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
