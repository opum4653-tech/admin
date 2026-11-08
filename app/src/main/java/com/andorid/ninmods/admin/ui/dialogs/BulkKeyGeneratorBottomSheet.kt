package com.andorid.ninmods.admin.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkKeyGeneratorBottomSheet(
    onDismiss: () -> Unit,
    onGenerate: (quantity: Int, durationHours: Int, durationType: String, gameType: String, maxDevices: Int, notes: String) -> Unit
) {
    var quantityText by remember { mutableStateOf("1") }
    var durationHoursText by remember { mutableStateOf("24") }
    var durationType by remember { mutableStateOf("1day") }
    var gameType by remember { mutableStateOf("all") }
    var maxDevicesText by remember { mutableStateOf("1") }
    var notesText by remember { mutableStateOf("") }

    AdminCustomBottomSheet(
        title = "Generate License Keys",
        subtitle = "Create bulk authorization keys with hardware-binding rules",
        onDismissRequest = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Quantity & Max Devices in Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                )

                OutlinedTextField(
                    value = maxDevicesText,
                    onValueChange = { maxDevicesText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Max Devices") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                )
            }

            // Duration Selection Pills
            Text(
                text = "Duration Preset",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("1 Day", "1day", 24),
                    Triple("7 Days", "7days", 168),
                    Triple("30 Days", "30days", 720),
                    Triple("Custom", "custom", -1)
                ).forEach { (label, type, hours) ->
                    val isSelected = durationType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            durationType = type
                            if (hours > 0) durationHoursText = hours.toString()
                        },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            if (durationType == "custom") {
                OutlinedTextField(
                    value = durationHoursText,
                    onValueChange = { durationHoursText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Duration in Hours") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                )
            }

            // Game Type Pills
            Text(
                text = "Target Game",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Pair("All Games", "all"),
                    Pair("Free Fire", "freefire"),
                    Pair("PUBG / BGMI", "pubg")
                ).forEach { (label, code) ->
                    val isSelected = gameType == code
                    FilterChip(
                        selected = isSelected,
                        onClick = { gameType = code },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Admin Notes
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Admin Reference Note") },
                placeholder = { Text("e.g. VIP tournament batch") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Button
            Button(
                onClick = {
                    val qty = quantityText.toIntOrNull()?.coerceAtLeast(1) ?: 1
                    val hrs = durationHoursText.toIntOrNull()?.coerceAtLeast(1) ?: 24
                    val maxDev = maxDevicesText.toIntOrNull()?.coerceAtLeast(1) ?: 1
                    onGenerate(qty, hrs, durationType, gameType, maxDev, notesText.trim())
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generate Keys Now",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
