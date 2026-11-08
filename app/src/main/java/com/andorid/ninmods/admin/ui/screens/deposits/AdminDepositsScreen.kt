package com.andorid.ninmods.admin.ui.screens.deposits

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.andorid.ninmods.admin.data.model.PendingDepositItem
import com.andorid.ninmods.admin.ui.components.*
import com.andorid.ninmods.admin.ui.dialogs.ApproveDepositBottomSheet
import com.andorid.ninmods.admin.ui.dialogs.ManualDepositBottomSheet
import com.andorid.ninmods.admin.ui.dialogs.RejectDepositBottomSheet

@Composable
fun AdminDepositsScreen(
    deposits: List<PendingDepositItem>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onApprove: (depositId: Int, amount: Double?) -> Unit,
    onReject: (depositId: Int, reason: String) -> Unit,
    onAddManualDeposit: (userId: Int, amount: Double, method: String, notes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("PENDING") }
    var rejectDialogDeposit by remember { mutableStateOf<PendingDepositItem?>(null) }
    var approveDialogDeposit by remember { mutableStateOf<PendingDepositItem?>(null) }
    var showManualDepositDialog by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    val filterOptions = listOf("PENDING", "APPROVED", "REJECTED", "ALL")

    val filteredDeposits = remember(deposits, selectedFilter) {
        deposits.filter {
            if (selectedFilter == "ALL") true else it.status.equals(selectedFilter, ignoreCase = true)
        }
    }

    ResponsiveContentBox(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Filter Row & Add Manual Deposit Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AdminFilterChipRow(
                        items = filterOptions,
                        selectedItem = selectedFilter,
                        onSelect = { selectedFilter = it },
                        modifier = Modifier.padding(0.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { showManualDepositDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Deposit", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Manual", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }

            // Transactions List
            if (isLoading && deposits.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (filteredDeposits.isEmpty()) {
                AdminEmptyState(
                    title = "No Deposits in Queue",
                    subtitle = "No transactions found matching the selected filter ($selectedFilter).",
                    icon = Icons.Filled.CheckCircle,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredDeposits, key = { it.id }) { item ->
                        val isPending = item.status.equals("pending", ignoreCase = true)

                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
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
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = item.username,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            StatusBadgePill(text = item.status, status = item.status)
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "\$${String.format("%.2f", item.amount)} USD",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "Method: ${item.paymentMethod.uppercase()}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        if (!item.trxId.isNullOrBlank()) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                Text(
                                                    text = "TxID: ${item.trxId}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = MetricBlue
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Filled.ContentCopy,
                                                    contentDescription = "Copy TxID",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clickable {
                                                            clipboardManager.setText(AnnotatedString(item.trxId))
                                                        }
                                                )
                                            }
                                        }

                                        if (!item.adminNotes.isNullOrBlank()) {
                                            Text(
                                                text = "Notes: ${item.adminNotes}",
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

                                    if (!item.proofImage.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        AsyncImage(
                                            model = item.proofImage,
                                            contentDescription = "Payment Screenshot",
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                        )
                                    }
                                }

                                if (isPending) {
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
                                                containerColor = MetricGreen.copy(alpha = 0.2f),
                                                contentColor = MetricGreen
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
                                                containerColor = MetricRed.copy(alpha = 0.2f),
                                                contentColor = MetricRed
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
        }
    }

    // Manual Deposit Addition Bottom Sheet (slides up from bottom)
    if (showManualDepositDialog) {
        ManualDepositBottomSheet(
            onDismiss = { showManualDepositDialog = false },
            onConfirm = { uid, amt, mth, not ->
                onAddManualDeposit(uid, amt, mth, not)
            }
        )
    }

    // Approve Deposit Confirmation Bottom Sheet
    approveDialogDeposit?.let { dep ->
        ApproveDepositBottomSheet(
            deposit = dep,
            onDismiss = { approveDialogDeposit = null },
            onConfirm = { id, verified ->
                onApprove(id, verified)
            }
        )
    }

    // Reject Deposit Bottom Sheet
    rejectDialogDeposit?.let { dep ->
        RejectDepositBottomSheet(
            deposit = dep,
            onDismiss = { rejectDialogDeposit = null },
            onConfirm = { id, reason ->
                onReject(id, reason)
            }
        )
    }
}
