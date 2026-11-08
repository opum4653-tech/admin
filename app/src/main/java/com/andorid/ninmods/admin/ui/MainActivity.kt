package com.andorid.ninmods.admin.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.andorid.ninmods.admin.ui.components.AdminFloatingBottomNav
import com.andorid.ninmods.admin.ui.components.AdminTab
import com.andorid.ninmods.admin.ui.screens.dashboard.AdminDashboardScreen
import com.andorid.ninmods.admin.ui.screens.deposits.AdminDepositsScreen
import com.andorid.ninmods.admin.ui.screens.keys.AdminKeysScreen
import com.andorid.ninmods.admin.ui.screens.login.AdminLoginScreen
import com.andorid.ninmods.admin.ui.screens.settings.AdminSettingsScreen
import com.andorid.ninmods.admin.ui.screens.users.AdminUsersScreen
import com.andorid.ninmods.admin.ui.theme.CyberDarkBg
import com.andorid.ninmods.admin.ui.theme.NINMODSAdminTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NINMODSAdminTheme {
                val isLoggedIn by viewModel.isLoggedIn.collectAsState()

                if (!isLoggedIn) {
                    val isLoading by viewModel.isLoading.collectAsState()
                    val errorMessage by viewModel.errorMessage.collectAsState()
                    AdminLoginScreen(
                        onLoginClick = { u, p -> viewModel.login(u, p) },
                        isLoading = isLoading,
                        errorMessage = errorMessage
                    )
                } else {
                    AdminMainConsole(viewModel)
                }
            }
        }
    }
}

@Composable
fun AdminMainConsole(viewModel: AdminViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val dashboardStats by viewModel.dashboardStats.collectAsState()
    val keysList by viewModel.keysList.collectAsState()
    val usersList by viewModel.usersList.collectAsState()
    val pendingDeposits by viewModel.pendingDeposits.collectAsState()
    val gameConfigs by viewModel.gameConfigs.collectAsState()
    val plans by viewModel.plans.collectAsState()

    val username = viewModel.sessionManager.username ?: "Admin"
    val role = viewModel.sessionManager.role ?: "admin"

    Scaffold(
        containerColor = CyberDarkBg,
        bottomBar = {
            AdminFloatingBottomNav(
                currentTab = currentTab,
                onTabSelected = { viewModel.setTab(it) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CyberDarkBg)
        ) {
            when (currentTab) {
                AdminTab.DASHBOARD -> {
                    AdminDashboardScreen(
                        stats = dashboardStats,
                        isLoading = isLoading,
                        onRefresh = { viewModel.refreshAll() }
                    )
                }
                AdminTab.KEYS -> {
                    AdminKeysScreen(
                        keys = keysList,
                        isLoading = isLoading,
                        onRefresh = { viewModel.refreshAll() },
                        onActionClick = { id, act, hrs -> viewModel.executeKeyAction(id, act, hrs) },
                        onGenerateBulk = { q, dh, dt, gt, md, n -> viewModel.generateBulkKeys(q, dh, dt, gt, md, n) }
                    )
                }
                AdminTab.USERS -> {
                    AdminUsersScreen(
                        users = usersList,
                        isLoading = isLoading,
                        onRefresh = { viewModel.refreshAll() },
                        onUpdateBalance = { id, act, amt, notes -> viewModel.updateUserBalance(id, act, amt, notes) },
                        onChangeRole = { id, r -> viewModel.changeUserRole(id, r) }
                    )
                }
                AdminTab.DEPOSITS -> {
                    AdminDepositsScreen(
                        deposits = pendingDeposits,
                        isLoading = isLoading,
                        onRefresh = { viewModel.refreshAll() },
                        onApprove = { id, amt -> viewModel.approveDeposit(id, amt) },
                        onReject = { id, rsn -> viewModel.rejectDeposit(id, rsn) }
                    )
                }
                AdminTab.SETTINGS -> {
                    AdminSettingsScreen(
                        adminUsername = username,
                        adminRole = role,
                        gameConfigs = gameConfigs,
                        plans = plans,
                        isLoading = isLoading,
                        onRefresh = { viewModel.refreshAll() },
                        onUpdateGameConfig = { id, st, ver, esp, aim, not -> viewModel.updateGameConfig(id, st, ver, esp, aim, not) },
                        onLogout = { viewModel.logout() }
                    )
                }
            }
        }
    }
}
