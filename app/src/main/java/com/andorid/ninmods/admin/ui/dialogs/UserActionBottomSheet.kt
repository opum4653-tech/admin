package com.andorid.ninmods.admin.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andorid.ninmods.admin.data.model.AdminUserItem
import com.andorid.ninmods.admin.ui.components.ApatchGreen
import com.andorid.ninmods.admin.ui.components.ApatchOrange
import com.andorid.ninmods.admin.ui.components.ApatchRed

@Composable
fun UserActionBottomSheet(
    user: AdminUserItem,
    onDismiss: () -> Unit,
    onViewInfo: () -> Unit,
    onAdjustBalance: () -> Unit,
    onChangeRole: () -> Unit,
    onActionClick: (userId: Int, action: String, reason: String) -> Unit
) {
    var reasonText by remember { mutableStateOf("") }
    var showBanReasonInput by remember { mutableStateOf(false) }

    AdminCustomBottomSheet(
        title = "User Operations",
        subtitle = "Account: ${user.username} (#${user.id})",
        onDismissRequest = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // View Profile
            UserActionCard(
                title = "View Full Profile",
                subtitle = "Inspect balance, contact details and timestamps",
                icon = Icons.Filled.Info,
                iconColor = MaterialTheme.colorScheme.primary,
                onClick = {
                    onDismiss()
                    onViewInfo()
                }
            )

            // Adjust Balance
            UserActionCard(
                title = "Adjust Wallet Balance",
                subtitle = "Credit or debit funds directly to wallet",
                icon = Icons.Filled.AccountBalanceWallet,
                iconColor = ApatchGreen,
                onClick = {
                    onDismiss()
                    onAdjustBalance()
                }
            )

            // Change Role
            UserActionCard(
                title = "Change User Role",
                subtitle = "Switch between User, Reseller or VIP",
                icon = Icons.Filled.Security,
                iconColor = ApatchOrange,
                onClick = {
                    onDismiss()
                    onChangeRole()
                }
            )

            // Reset User HWID
            UserActionCard(
                title = "Reset Device HWIDs",
                subtitle = "Clear hardware signature across all associated keys",
                icon = Icons.Filled.Refresh,
                iconColor = MaterialTheme.colorScheme.tertiary,
                onClick = {
                    onActionClick(user.id, "reset_hwid", "Admin initiated reset")
                    onDismiss()
                }
            )

            // Ban or Unban
            val isBanned = user.status.equals("banned", ignoreCase = true) || user.status.equals("suspended", ignoreCase = true)
            if (isBanned) {
                UserActionCard(
                    title = "Unban Account",
                    subtitle = "Restore login access and re-enable active keys",
                    icon = Icons.Filled.CheckCircle,
                    iconColor = ApatchGreen,
                    onClick = {
                        onActionClick(user.id, "unban", "Admin restored access")
                        onDismiss()
                    }
                )
            } else {
                UserActionCard(
                    title = "Ban Account",
                    subtitle = "Block login and suspend all active keys",
                    icon = Icons.Filled.Block,
                    iconColor = ApatchRed,
                    onClick = { showBanReasonInput = !showBanReasonInput }
                )

                if (showBanReasonInput) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            OutlinedTextField(
                                value = reasonText,
                                onValueChange = { reasonText = it },
                                label = { Text("Reason for Ban") },
                                placeholder = { Text("e.g. Terms violation or chargeback") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    onActionClick(user.id, "ban", reasonText.trim().ifEmpty { "Violation of policies" })
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ApatchRed, contentColor = Color.Black),
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Confirm Ban", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun UserActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.16f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
