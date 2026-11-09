package com.andorid.ninmods.admin.ui.screens.users

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andorid.ninmods.admin.data.model.AdminUserItem
import com.andorid.ninmods.admin.ui.components.*
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

    ResponsiveContentBox(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Enterprise Full-Width Search Bar
                AdminSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search by username, email...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )

                // Role Filter Pills Row
                AdminFilterChipRow(
                    items = roleFilters,
                    selectedItem = selectedRoleFilter,
                    onSelect = { selectedRoleFilter = it }
                )

                // Users List
                if (isLoading && users.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (filteredUsers.isEmpty()) {
                    AdminEmptyState(
                        title = "No Users Found",
                        subtitle = "No user accounts match the selected role filter ($selectedRoleFilter) or search query.",
                        icon = Icons.Filled.People,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
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

            // Material 3 Floating Action Button at Bottom-Right (Aligned with Support Screen style)
            if (onNavigateToAddUser != null) {
                FloatingActionButton(
                    onClick = onNavigateToAddUser,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add User",
                        modifier = Modifier.size(24.dp)
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.username,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
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
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MetricGreen
                    )
                }

                // 3-Dots Action Button opens Bottom Sheet (from bottom)
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
