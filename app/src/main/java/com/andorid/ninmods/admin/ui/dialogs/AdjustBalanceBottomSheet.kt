package com.andorid.ninmods.admin.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.andorid.ninmods.admin.data.model.AdminUserItem

@Composable
fun AdjustBalanceBottomSheet(
    user: AdminUserItem,
    onDismiss: () -> Unit,
    onConfirm: (userId: Int, action: String, amount: Double, notes: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var actionType by remember { mutableStateOf("add") } // "add" or "deduct"
    var notesText by remember { mutableStateOf("") }

    AdminCustomBottomSheet(
        title = "Adjust Balance",
        subtitle = "Modify wallet funds for ${user.username} (Current: ${String.format("%.2f", user.balance)} ${user.currency})",
        onDismissRequest = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Action Selection Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = actionType == "add",
                    onClick = { actionType = "add" },
                    label = { Text("Credit / Add (+)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                FilterChip(
                    selected = actionType == "deduct",
                    onClick = { actionType = "deduct" },
                    label = { Text("Debit / Deduct (-)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("Amount (${user.currency})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Reason / Administrative Note") },
                placeholder = { Text("e.g. Compensation or manual deposit") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onConfirm(user.id, actionType, amt, notesText.trim())
                        onDismiss()
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Confirm Balance Adjustment", fontWeight = FontWeight.Bold)
            }
        }
    }
}
