package com.andorid.ninmods.admin.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.andorid.ninmods.admin.data.model.AdminLicenseKeyItem
import com.andorid.ninmods.admin.ui.components.ApatchGreen
import com.andorid.ninmods.admin.ui.components.ApatchOrange
import com.andorid.ninmods.admin.ui.components.ApatchRed

@Composable
fun KeyActionBottomSheet(
    keyItem: AdminLicenseKeyItem,
    onDismiss: () -> Unit,
    onActionClick: (keyId: Int, action: String, hours: Int) -> Unit
) {
    var extendHoursText by remember { mutableStateOf("24") }
    var showExtendInput by remember { mutableStateOf(false) }

    AdminCustomBottomSheet(
        title = "Key Management",
        subtitle = "License: ${keyItem.licenseKey}",
        onDismissRequest = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Action 1: Reset HWID
            ActionItemCard(
                title = "Reset HWID",
                subtitle = "Unbind registered device (${keyItem.deviceModel ?: "None"})",
                icon = Icons.Filled.Refresh,
                iconColor = MaterialTheme.colorScheme.primary,
                onClick = {
                    onActionClick(keyItem.id, "reset_hwid", 0)
                    onDismiss()
                }
            )

            // Action 2: Extend Validity
            ActionItemCard(
                title = "Extend Duration",
                subtitle = "Add additional hours to key validity",
                icon = Icons.Filled.AccessTime,
                iconColor = ApatchOrange,
                onClick = { showExtendInput = !showExtendInput }
            )

            if (showExtendInput) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        OutlinedTextField(
                            value = extendHoursText,
                            onValueChange = { extendHoursText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Hours to Extend") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                val hrs = extendHoursText.toIntOrNull()?.coerceAtLeast(1) ?: 24
                                onActionClick(keyItem.id, "extend", hrs)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Confirm Extension", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Action 3: Ban or Unban
            if (keyItem.status.equals("banned", ignoreCase = true)) {
                ActionItemCard(
                    title = "Unban License Key",
                    subtitle = "Restore authorization privileges",
                    icon = Icons.Filled.CheckCircle,
                    iconColor = ApatchGreen,
                    onClick = {
                        onActionClick(keyItem.id, "unban", 0)
                        onDismiss()
                    }
                )
            } else {
                ActionItemCard(
                    title = "Ban License Key",
                    subtitle = "Revoke instant access across devices",
                    icon = Icons.Filled.Block,
                    iconColor = ApatchRed,
                    onClick = {
                        onActionClick(keyItem.id, "ban", 0)
                        onDismiss()
                    }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun ActionItemCard(
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
