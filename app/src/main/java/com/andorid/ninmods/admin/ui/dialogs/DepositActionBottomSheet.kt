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
import com.andorid.ninmods.admin.data.model.PendingDepositItem
import com.andorid.ninmods.admin.ui.components.ApatchGreen
import com.andorid.ninmods.admin.ui.components.ApatchRed

@Composable
fun ApproveDepositBottomSheet(
    deposit: PendingDepositItem,
    onDismiss: () -> Unit,
    onConfirm: (depositId: Int, verifiedAmount: Double?) -> Unit
) {
    var amountText by remember { mutableStateOf(deposit.amount.toString()) }

    AdminCustomBottomSheet(
        title = "Approve Deposit",
        subtitle = "Credit funds for TRX: ${deposit.trxId} (${deposit.username})",
        onDismissRequest = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("Credited Amount (${deposit.currency})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Text(
                text = "Method: ${deposit.paymentMethod} | Requested: ${deposit.amount} ${deposit.currency}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val verified = amountText.toDoubleOrNull() ?: deposit.amount
                    onConfirm(deposit.id, verified)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ApatchGreen)
            ) {
                Text("Confirm & Approve Deposit", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RejectDepositBottomSheet(
    deposit: PendingDepositItem,
    onDismiss: () -> Unit,
    onConfirm: (depositId: Int, reason: String) -> Unit
) {
    var reasonText by remember { mutableStateOf("Invalid Transaction ID") }

    AdminCustomBottomSheet(
        title = "Reject Deposit",
        subtitle = "Disapprove TRX: ${deposit.trxId} (${deposit.username})",
        onDismissRequest = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(
                value = reasonText,
                onValueChange = { reasonText = it },
                label = { Text("Rejection Reason") },
                placeholder = { Text("e.g. Payment not received / invalid TRX") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    onConfirm(deposit.id, reasonText.trim())
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ApatchRed)
            ) {
                Text("Reject Transaction", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ManualDepositBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (userId: Int, amount: Double, method: String, notes: String) -> Unit
) {
    var userIdText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Manual Admin") }
    var notesText by remember { mutableStateOf("") }

    AdminCustomBottomSheet(
        title = "Add Manual Deposit",
        subtitle = "Credit funds directly to a user account ledger",
        onDismissRequest = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(
                value = userIdText,
                onValueChange = { userIdText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Target User ID (#)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("Deposit Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = paymentMethod,
                onValueChange = { paymentMethod = it },
                label = { Text("Payment Channel") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Internal Note / Reference") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val uid = userIdText.toIntOrNull() ?: 0
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (uid > 0 && amt > 0) {
                        onConfirm(uid, amt, paymentMethod.trim(), notesText.trim())
                        onDismiss()
                    }
                },
                enabled = (userIdText.toIntOrNull() ?: 0) > 0 && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Process Manual Deposit", fontWeight = FontWeight.Bold)
            }
        }
    }
}
