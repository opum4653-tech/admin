package com.andorid.ninmods.admin.ui.screens.deposits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.andorid.ninmods.admin.data.model.PendingDepositItem
import com.andorid.ninmods.admin.ui.components.AdminTopBar
import com.andorid.ninmods.admin.ui.components.CyberCard
import com.andorid.ninmods.admin.ui.components.StatusBadgePill
import com.andorid.ninmods.admin.ui.theme.*

@Composable
fun AdminDepositsScreen(
    deposits: List<PendingDepositItem>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onApprove: (depositId: Int, amount: Double?) -> Unit,
    onReject: (depositId: Int, reason: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var rejectDialogDeposit by remember { mutableStateOf<PendingDepositItem?>(null) }
    var approveDialogDeposit by remember { mutableStateOf<PendingDepositItem?>(null) }
    val clipboardManager = LocalClipboardManager.current

    Column(modifier = modifier.fillMaxSize()) {
        AdminTopBar(
            title = "PENDING DEPOSITS",
            subtitle = "${deposits.size} transactions awaiting review",
            onRefresh = onRefresh
        )

        if (isLoading && deposits.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CyberPurple)
            }
        } else if (deposits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircleOutline,
                        contentDescription = null,
                        tint = CyberGreen,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "All Caught Up!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "No pending manual deposits in the verification queue.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(deposits, key = { it.id }) { item ->
                    CyberCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${item.amount} ${item.currency}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CyberGreen
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusBadgePill(text = item.paymentMethod, status = "pending")
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "User: ${item.username} (${item.email})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Trx ID: ${item.trxId}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = CyberCyan
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = CyberCyan,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable {
                                                clipboardManager.setText(AnnotatedString(item.trxId))
                                            }
                                    )
                                }

                                Text(
                                    text = "Submitted: ${item.createdAt}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary
                                )
                            }

                            if (!item.proofImage.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                AsyncImage(
                                    model = item.proofImage,
                                    contentDescription = "Proof",
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons: Approve vs Reject
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { approveDialogDeposit = item },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("APPROVE", fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Button(
                                onClick = { rejectDialogDeposit = item },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberRed.copy(alpha = 0.25f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Filled.Close, contentDescription = null, tint = CyberRed)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("REJECT", fontWeight = FontWeight.Bold, color = CyberRed)
                            }
                        }
                    }
                }
            }
        }
    }

    // Approve Confirmation Dialog
    approveDialogDeposit?.let { dep ->
        var customAmountText by remember { mutableStateOf(dep.amount.toString()) }

        AlertDialog(
            onDismissRequest = { approveDialogDeposit = null },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Approve Deposit #${dep.id}", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("User: ${dep.username}", color = TextSecondary)
                    Text("Transaction ID: ${dep.trxId}", color = CyberCyan, fontFamily = FontFamily.Monospace)

                    OutlinedTextField(
                        value = customAmountText,
                        onValueChange = { customAmountText = it },
                        label = { Text("Credited Amount (${dep.currency})", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberGreen,
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
                        val finalAmt = customAmountText.toDoubleOrNull()
                        onApprove(dep.id, finalAmt)
                        approveDialogDeposit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen)
                ) {
                    Text("CONFIRM APPROVE", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { approveDialogDeposit = null }) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }

    // Reject Dialog
    rejectDialogDeposit?.let { dep ->
        var reasonText by remember { mutableStateOf("Invalid Transaction ID or unverified payment") }

        AlertDialog(
            onDismissRequest = { rejectDialogDeposit = null },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Reject Deposit #${dep.id}", color = CyberRed, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Provide a rejection note for ${dep.username}:", color = TextSecondary)

                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        label = { Text("Rejection Reason", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberRed,
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
                        onReject(dep.id, reasonText)
                        rejectDialogDeposit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberRed)
                ) {
                    Text("CONFIRM REJECT", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectDialogDeposit = null }) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }
}
