package com.andorid.ninmods.admin.ui.screens.keys

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.andorid.ninmods.admin.ui.theme.*

@Composable
fun BulkKeyGeneratorDialog(
    onDismiss: () -> Unit,
    onGenerate: (quantity: Int, durationHours: Int, durationType: String, gameType: String, maxDevices: Int, notes: String) -> Unit
) {
    var quantityText by remember { mutableStateOf("1") }
    var durationHoursText by remember { mutableStateOf("24") }
    var durationType by remember { mutableStateOf("1day") }
    var gameType by remember { mutableStateOf("all") }
    var maxDevicesText by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("Admin generated batch") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CyberCardBg,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "BULK KEY GENERATOR",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CyberPurple
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Quantity (1 - 100)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberPurple,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = durationHoursText,
                    onValueChange = { durationHoursText = it },
                    label = { Text("Duration (Hours)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberPurple,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = maxDevicesText,
                    onValueChange = { maxDevicesText = it },
                    label = { Text("Max Devices Limit", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberPurple,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Internal Batch Notes", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberPurple,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val q = quantityText.toIntOrNull() ?: 1
                    val dh = durationHoursText.toIntOrNull() ?: 24
                    val md = maxDevicesText.toIntOrNull() ?: 1
                    onGenerate(q, dh, durationType, gameType, md, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberPurple)
            ) {
                Text("GENERATE", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = TextSecondary)
            }
        }
    )
}
