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
import com.andorid.ninmods.admin.ui.dialogs.UserActionBottomSheet

@Composable
fun AdminUsersScreen(
    users: List<AdminUserItem>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onUserAction: (userId: Int, action: String, reason: String) -> Unit,
    onUpdateBalance: (userId: Int, action: String, amount: Double, notes: String) -> Unit,
    onChangeRole: (userId: Int, newRole: String) -> Unit,
    onNavigateToAddUser: (() -> Unit)? = null,
    onSelectUser: ((AdminUserItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf("ALL") }

    // Contextual Action Sheet State (Only for quick 3-dots actions)
    var actionSheetUser by remember { mutableStateOf<AdminUserItem?>(null) }

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
        // Search & Add User Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by username, email...", style = MaterialTheme.typography.bodyMedium) },
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
                modifier = Modifier.weight(1f)
            )

            if (onNavigateToAddUser != null) {
                Button(
                    onClick = onNavigateToAddUser,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PersonAdd,
                        contentDescription = "Add User",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

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
                        onClick = { onSelectUser?.invoke(item) },
                        onActionClick = { actionSheetUser = item }
                    )
                }
            }
        }
    }

    // User Operations Bottom Sheet (slides up from bottom for quick actions)
    actionSheetUser?.let { u ->
        UserActionBottomSheet(
            user = u,
            onDismiss = { actionSheetUser = null },
            onViewInfo = {
                actionSheetUser = null
                onSelectUser?.invoke(u)
            },
            onAdjustBalance = {
                actionSheetUser = null
                onSelectUser?.invoke(u)
            },
            onChangeRole = {
                actionSheetUser = null
                onSelectUser?.invoke(u)
            },
            onActionClick = onUserAction
        )
    }
}

@Composable
private fun UserCardItem(
    user: AdminUserItem,
    onClick: (() -> Unit)? = null,
    onActionClick: () -> Unit
) {
    val isBanned = user.status.equals("banned", ignoreCase = true)

    ElevatedCard(
        modifier = if (onClick != null) {
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
        } else {
            Modifier.fillMaxWidth()
        },
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

                // 3-Dots Action Button opens APatch Bottom Sheet (from bottom)
                IconButton(onClick = onActionClick) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "User Actions",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


