package com.andorid.ninmods.admin.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andorid.ninmods.admin.data.model.AdminLicenseKeyItem
import com.andorid.ninmods.admin.data.model.AdminUserItem
import com.andorid.ninmods.admin.data.model.PendingDepositItem
import com.andorid.ninmods.admin.ui.components.ApatchRed
import com.andorid.ninmods.admin.ui.screens.deposits.AdminDepositsScreen
import com.andorid.ninmods.admin.ui.screens.keys.AdminKeysScreen
import com.andorid.ninmods.admin.ui.screens.users.AdminUsersScreen

enum class ManagementSubTab(val title: String, val icon: ImageVector) {
    KEYS("Keys", Icons.Filled.VpnKey),
    USERS("Users", Icons.Filled.People),
    DEPOSITS("Deposits", Icons.Filled.AccountBalanceWallet)
}

@Composable
fun AdminManagementScreen(
    currentSubTab: Int,
    onSubTabChanged: (Int) -> Unit,
    keys: List<AdminLicenseKeyItem>,
    users: List<AdminUserItem>,
    deposits: List<PendingDepositItem>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onActionClick: (keyId: Int, action: String, hours: Int) -> Unit,
    onOpenBulkGenerate: () -> Unit,
    onUpdateBalance: (userId: Int, action: String, amount: Double, notes: String) -> Unit,
    onChangeRole: (userId: Int, newRole: String) -> Unit,
    onApproveDeposit: (depositId: Int, amount: Double?) -> Unit,
    onRejectDeposit: (depositId: Int, reason: String) -> Unit,
    onUserAction: (userId: Int, action: String, reason: String) -> Unit = { _, _, _ -> },
    onAddManualDeposit: (userId: Int, amount: Double, method: String, notes: String) -> Unit = { _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Top APatch Sub-tab Selector Pill
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ManagementSubTab.values().forEachIndexed { index, tab ->
                    val isSelected = currentSubTab == index
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSubTabChanged(index) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(18.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (tab == ManagementSubTab.DEPOSITS && deposits.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ApatchRed
                                ) {
                                    Text(
                                        text = "${deposits.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onError,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sub-tab Content with smooth transition
        AnimatedContent(
            targetState = currentSubTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
            },
            label = "SubTabTransition",
            modifier = Modifier.fillMaxSize()
        ) { tabIndex ->
            when (tabIndex) {
                0 -> AdminKeysScreen(
                    keys = keys,
                    isLoading = isLoading,
                    onRefresh = onRefresh,
                    onActionClick = onActionClick,
                    onOpenBulkGenerate = onOpenBulkGenerate
                )
                1 -> AdminUsersScreen(
                    users = users,
                    isLoading = isLoading,
                    onRefresh = onRefresh,
                    onUserAction = onUserAction,
                    onUpdateBalance = onUpdateBalance,
                    onChangeRole = onChangeRole
                )
                2 -> AdminDepositsScreen(
                    deposits = deposits,
                    isLoading = isLoading,
                    onRefresh = onRefresh,
                    onApprove = onApproveDeposit,
                    onReject = onRejectDeposit,
                    onAddManualDeposit = onAddManualDeposit
                )
            }
        }
    }
}
