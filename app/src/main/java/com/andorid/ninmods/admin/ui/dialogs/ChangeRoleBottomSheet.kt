package com.andorid.ninmods.admin.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andorid.ninmods.admin.data.model.AdminUserItem

@Composable
fun ChangeRoleBottomSheet(
    user: AdminUserItem,
    onDismiss: () -> Unit,
    onConfirm: (userId: Int, newRole: String) -> Unit
) {
    var selectedRole by remember { mutableStateOf(user.role.lowercase()) }

    AdminCustomBottomSheet(
        title = "Change User Role",
        subtitle = "Assign permissions for ${user.username}",
        onDismissRequest = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(
                Triple("user", "Regular User", "Standard consumer account with standard catalog pricing"),
                Triple("reseller", "Reseller", "Access to discounted reseller wholesale key tariffs"),
                Triple("vip_reseller", "VIP Reseller", "Maximum tier wholesale pricing and high volume access")
            ).forEach { (roleCode, title, desc) ->
                val isSelected = selectedRole == roleCode
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedRole = roleCode },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    onConfirm(user.id, selectedRole)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Apply Role Change", fontWeight = FontWeight.Bold)
            }
        }
    }
}
