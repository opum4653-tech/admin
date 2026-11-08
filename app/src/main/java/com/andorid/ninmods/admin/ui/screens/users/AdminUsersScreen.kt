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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.andorid.ninmods.admin.data.model.AdminUserItem
import com.andorid.ninmods.admin.ui.components.AdminTopBar
import com.andorid.ninmods.admin.ui.components.CyberCard
import com.andorid.ninmods.admin.ui.components.StatusBadgePill
import com.andorid.ninmods.admin.ui.theme.*

@Composable
fun AdminUsersScreen(
    users: List<AdminUserItem>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onUpdateBalance: (userId: Int, action: String, amount: Double, notes: String) -> Unit,
    onChangeRole: (userId: Int, newRole: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf("ALL") }
    var balanceDialogUser by remember { mutableStateOf<AdminUserItem?>(null) }
    var roleDialogUser by remember { mutableStateOf<AdminUserItem?>(null) }

    val roleFilters = listOf("ALL", "USER", "RESELLER", "VIP_RESELLER")

    val filteredUsers = remember(users, searchQuery, selectedRoleFilter) {
        users.filter { item ->
            val matchesRole = when (selectedRoleFilter) {
                "ALL" -> true
                else -> item.role.equals(selectedRoleFilter, ignoreCase = true)
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                item.username.contains(searchQuery, ignoreCase = true) ||
                item.email.contains(searchQuery, ignoreCase = true)
            }
            matchesRole && matchesSearch
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        AdminTopBar(
            title = "USER ACCOUNTS",
            subtitle = "${users.size} registered accounts",
            onRefresh = onRefresh
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by username or email...", color = TextSecondary) },
            leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = CyberPurple) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Clear", tint = TextSecondary)
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberPurple,
                unfocusedBorderColor = CyberBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Role Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            roleFilters.forEach { role ->
                val isSelected = selectedRoleFilter == role
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) CyberPurple else CyberSurface,
                    modifier = Modifier.clickable { selectedRoleFilter = role }
                ) {
                    Text(
                        text = role,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else TextSecondary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Users List
        if (isLoading && users.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CyberPurple)
            }
        } else if (filteredUsers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No users found", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredUsers, key = { it.id }) { u ->
                    CyberCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = u.username,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusBadgePill(text = u.role, status = if (u.role.contains("reseller")) "active" else "default")
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = u.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text(
                                        text = "Balance: $${String.format("%.2f", u.balance)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberGreen
                                    )
                                    Text(
                                        text = "Spent: $${String.format("%.2f", u.spent)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Row {
                                IconButton(onClick = { balanceDialogUser = u }) {
                                    Icon(imageVector = Icons.Filled.AccountBalanceWallet, contentDescription = "Balance", tint = CyberGreen)
                                }
                                IconButton(onClick = { roleDialogUser = u }) {
                                    Icon(imageVector = Icons.Filled.ManageAccounts, contentDescription = "Role", tint = CyberPurple)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Balance Adjustment Dialog
    balanceDialogUser?.let { targetUser ->
        var action by remember { mutableStateOf("add") }
        var amountText by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("Admin adjustment") }

        AlertDialog(
            onDismissRequest = { balanceDialogUser = null },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Adjust Balance: ${targetUser.username}", color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Current Balance: $${String.format("%.2f", targetUser.balance)} USD", color = CyberGreen, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { action = "add" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (action == "add") CyberGreen else CyberSurface),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ADD (+)", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Button(
                            onClick = { action = "deduct" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (action == "deduct") CyberRed else CyberSurface),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("DEDUCT (-)", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount ($)", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                        label = { Text("Notes", color = TextSecondary) },
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
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            onUpdateBalance(targetUser.id, action, amt, notes)
                            balanceDialogUser = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (action == "add") CyberGreen else CyberRed)
                ) {
                    Text("SUBMIT", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { balanceDialogUser = null }) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }

    // Role Switcher Dialog
    roleDialogUser?.let { targetUser ->
        val availableRoles = listOf("user", "reseller", "vip_reseller")

        AlertDialog(
            onDismissRequest = { roleDialogUser = null },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Change Role: ${targetUser.username}", color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Current Role: ${targetUser.role.uppercase()}", color = CyberPurple, fontWeight = FontWeight.Bold)

                    availableRoles.forEach { r ->
                        Button(
                            onClick = {
                                onChangeRole(targetUser.id, r)
                                roleDialogUser = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (targetUser.role == r) CyberPurple else CyberSurface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(r.uppercase(), fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { roleDialogUser = null }) {
                    Text("CLOSE", color = TextSecondary)
                }
            }
        )
    }
}
