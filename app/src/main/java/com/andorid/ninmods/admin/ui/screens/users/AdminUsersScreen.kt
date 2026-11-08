package com.andorid.ninmods.admin.ui.screens.users

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.andorid.ninmods.admin.data.model.AdminUserItem
import com.andorid.ninmods.admin.ui.components.ApatchGreen
import com.andorid.ninmods.admin.ui.components.ApatchOrange
import com.andorid.ninmods.admin.ui.components.ApatchRed
import com.andorid.ninmods.admin.ui.components.StatusBadgePill

@Composable
fun AdminUsersScreen(
    users: List<AdminUserItem>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onUserAction: (userId: Int, action: String, reason: String) -> Unit,
    onUpdateBalance: (userId: Int, action: String, amount: Double, notes: String) -> Unit,
    onChangeRole: (userId: Int, newRole: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf("ALL") }

    // Dialog States
    var infoDialogUser by remember { mutableStateOf<AdminUserItem?>(null) }
    var balanceDialogUser by remember { mutableStateOf<AdminUserItem?>(null) }
    var roleDialogUser by remember { mutableStateOf<AdminUserItem?>(null) }

    val roleFilters = listOf("ALL", "USER", "RESELLER", "VIP_RESELLER", "BANNED")

    val filteredUsers = remember(users, searchQuery, selectedRoleFilter) {
        users.filter { item ->
            val matchesRole = when (selectedRoleFilter) {
                "ALL" -> true
                "BANNED" -> item.status.equals("banned", ignoreCase = true)
                else -> item.role.equals(selectedRoleFilter, ignoreCase = true)
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                item.username.contains(searchQuery, ignoreCase = true) ||
                item.email.contains(searchQuery, ignoreCase = true) ||
                (item.telephone?.contains(searchQuery, ignoreCase = true) ?: false)
            }
            matchesRole && matchesSearch
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by username, email, phone...", style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // Role & Status Filter Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            roleFilters.forEach { role ->
                val isSelected = selectedRoleFilter == role
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { selectedRoleFilter = role }
                ) {
                    Text(
                        text = role,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Users List
        if (isLoading && users.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (filteredUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No user accounts match the criteria",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredUsers, key = { it.id }) { item ->
                    UserCardItem(
                        user = item,
                        onViewInfo = { infoDialogUser = item },
                        onToggleBan = {
                            val newAction = if (item.status.equals("banned", ignoreCase = true)) "unban" else "ban"
                            onUserAction(item.id, newAction, "Admin dashboard toggle")
                        },
                        onResetHwid = {
                            onUserAction(item.id, "reset_hwid", "Admin HWID reset")
                        },
                        onEditBalance = { balanceDialogUser = item },
                        onChangeRole = { roleDialogUser = item }
                    )
                }
            }
        }
    }

    // User Info Details Dialog
    infoDialogUser?.let { u ->
        AlertDialog(
            onDismissRequest = { infoDialogUser = null },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "User Profile: ${u.username}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoDetailRow("User ID", "#${u.id}")
                    InfoDetailRow("Username", u.username)
                    InfoDetailRow("Email", u.email)
                    InfoDetailRow("Telephone", u.telephone ?: "Not provided")
                    InfoDetailRow("Role", u.role.uppercase())
                    InfoDetailRow("Account Status", u.status.uppercase())
                    InfoDetailRow("Wallet Balance", "\$${String.format("%.2f", u.balance)} ${u.currency}")
                    InfoDetailRow("Total Spent", "\$${String.format("%.2f", u.spent)} ${u.currency}")
                    InfoDetailRow("Registration Date", u.createdAt)
                }
            },
            confirmButton = {
                TextButton(onClick = { infoDialogUser = null }) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Balance Adjustment Dialog
    balanceDialogUser?.let { u ->
        var amountText by remember { mutableStateOf("") }
        var isDeduct by remember { mutableStateOf(false) }
        var notes by remember { mutableStateOf("Admin adjustment") }

        AlertDialog(
            onDismissRequest = { balanceDialogUser = null },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Adjust Balance: ${u.username}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Current Balance: \$${String.format("%.2f", u.balance)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isDeduct = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isDeduct) ApatchGreen else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (!isDeduct) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("CREDIT (+)")
                        }

                        Button(
                            onClick = { isDeduct = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDeduct) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isDeduct) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("DEDUCT (-)")
                        }
                    }

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount in USD ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Transaction Notes") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            onUpdateBalance(u.id, if (isDeduct) "deduct" else "add", amt, notes)
                        }
                        balanceDialogUser = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Apply", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { balanceDialogUser = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Role Promotion Dialog
    roleDialogUser?.let { u ->
        val availableRoles = listOf("user", "reseller", "vip_reseller")

        AlertDialog(
            onDismissRequest = { roleDialogUser = null },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Change Role: ${u.username}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Current role: ${u.role.uppercase()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    availableRoles.forEach { r ->
                        val isCurrent = u.role.equals(r, ignoreCase = true)
                        Button(
                            onClick = {
                                onChangeRole(u.id, r)
                                roleDialogUser = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(r.uppercase(), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { roleDialogUser = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun UserCardItem(
    user: AdminUserItem,
    onViewInfo: () -> Unit,
    onToggleBan: () -> Unit,
    onResetHwid: () -> Unit,
    onEditBalance: () -> Unit,
    onChangeRole: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isBanned = user.status.equals("banned", ignoreCase = true)

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
                            text = user.username,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusBadgePill(text = user.role, status = user.role)
                        if (isBanned) {
                            Spacer(modifier = Modifier.width(6.dp))
                            StatusBadgePill(text = "BANNED", status = "BANNED")
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Balance: \$${String.format("%.2f", user.balance)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ApatchGreen
                    )
                }

                // 3-Dots / Hamburger Dropdown Menu Button on the right of each card
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "User Actions",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("View User Info") },
                            leadingIcon = {
                                Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            onClick = {
                                menuExpanded = false
                                onViewInfo()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(if (isBanned) "Unban Account" else "Ban Account") },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isBanned) Icons.Filled.LockOpen else Icons.Filled.Block,
                                    contentDescription = null,
                                    tint = if (isBanned) ApatchGreen else ApatchRed
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onToggleBan()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Reset User HWID") },
                            leadingIcon = {
                                Icon(Icons.Filled.RestartAlt, contentDescription = null, tint = ApatchOrange)
                            },
                            onClick = {
                                menuExpanded = false
                                onResetHwid()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Adjust Wallet Balance") },
                            leadingIcon = {
                                Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = ApatchGreen)
                            },
                            onClick = {
                                menuExpanded = false
                                onEditBalance()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Change Role") },
                            leadingIcon = {
                                Icon(Icons.Filled.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            onClick = {
                                menuExpanded = false
                                onChangeRole()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
