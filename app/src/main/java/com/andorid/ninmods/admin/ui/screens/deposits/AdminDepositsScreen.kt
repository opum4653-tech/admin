package com.andorid.ninmods.admin.ui.screens.deposits

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.andorid.ninmods.admin.data.model.PendingDepositItem
import com.andorid.ninmods.admin.ui.components.ApatchGreen
import com.andorid.ninmods.admin.ui.components.ApatchRed
import com.andorid.ninmods.admin.ui.components.StatusBadgePill

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
        if (isLoading && deposits.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = ApatchGreen,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "All Caught Up!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No pending manual deposits in the verification queue.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${item.amount} ${item.currency}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = ApatchGreen
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        StatusBadgePill(text = item.status, status = item.status)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "User: ${item.username} • Gateway: ${item.paymentMethod.uppercase()}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    if (!item.transactionId.isNullOrBlank()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Text(
                                                text = "TrxID: ${item.transactionId}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Filled.ContentCopy,
                                                contentDescription = "Copy TrxID",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clickable {
                                                        clipboardManager.setText(AnnotatedString(item.transactionId))
                                                    }
                                            )
                                        }
                                    }

                                    if (!item.senderNumber.isNullOrBlank()) {
                                        Text(
                                            text = "Sender: ${item.senderNumber}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = "Submitted: ${item.createdAt}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }

                                if (!item.proofUrl.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    AsyncImage(
                                        model = item.proofUrl,
                                        contentDescription = "Payment Screenshot",
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { approveDialogDeposit = item },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ApatchGreen.copy(alpha = 0.2f),
                                        contentColor = ApatchGreen
                                    )
                                ) {
                                    Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Approve", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { rejectDialogDeposit = item },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ApatchRed.copy(alpha = 0.2f),
                                        contentColor = ApatchRed
                                    )
                                ) {
                                    Icon(imageVector = Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Reject", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Approve Deposit Confirmation Dialog
    approveDialogDeposit?.let { dep ->
        var customAmountText by remember { mutableStateOf("${dep.amount}") }

        AlertDialog(
            onDismissRequest = { approveDialogDeposit = null },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Approve Deposit",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Approve and credit balance to account ${dep.username}:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = customAmountText,
                        onValueChange = { customAmountText = it },
                        label = { Text("Confirmed Amount (${dep.currency})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = customAmountText.toDoubleOrNull()
                        onApprove(dep.id, parsed)
                        approveDialogDeposit = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ApatchGreen)
                ) {
                    Text("Confirm Approve", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { approveDialogDeposit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reject Deposit Dialog
    rejectDialogDeposit?.let { dep ->
        var reasonText by remember { mutableStateOf("Invalid Transaction ID / Payment not received") }

        AlertDialog(
            onDismissRequest = { rejectDialogDeposit = null },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Reject Deposit",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Reject deposit #${dep.id} from ${dep.username}. Provide reason:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        label = { Text("Rejection Reason") },
                        shape = RoundedCornerShape(14.dp),
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
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Reject", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectDialogDeposit = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
