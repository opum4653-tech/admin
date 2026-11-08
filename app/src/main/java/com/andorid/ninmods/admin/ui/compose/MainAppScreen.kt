package com.andorid.ninmods.admin.ui.compose

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andorid.ninmods.admin.R
import com.andorid.ninmods.admin.ui.AdminViewModel
import com.andorid.ninmods.admin.ui.screens.AdminManagementScreen
import com.andorid.ninmods.admin.ui.screens.AdminProfileScreen
import com.andorid.ninmods.admin.ui.screens.dashboard.AdminDashboardScreen
import com.andorid.ninmods.admin.ui.screens.keys.BulkKeyGeneratorDialog
import com.andorid.ninmods.admin.ui.screens.settings.AdminSettingsScreen

enum class AdminDestination(
    @StringRes val labelRes: Int,
    @DrawableRes val selectedDrawable: Int,
    @DrawableRes val unselectedDrawable: Int
) {
    DASHBOARD(
        labelRes = R.string.home,
        selectedDrawable = R.drawable.ic_nav_home_fill,
        unselectedDrawable = R.drawable.ic_nav_home
    ),
    MANAGEMENT(
        labelRes = R.string.nav_drivers,
        selectedDrawable = R.drawable.ic_nav_wrench,
        unselectedDrawable = R.drawable.ic_nav_wrench
    ),
    SETTINGS(
        labelRes = R.string.settings,
        selectedDrawable = R.drawable.ic_nav_gear,
        unselectedDrawable = R.drawable.ic_nav_gear
    ),
    PROFILE(
        labelRes = R.string.about,
        selectedDrawable = R.drawable.ic_user_profile,
        unselectedDrawable = R.drawable.ic_user_profile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: AdminViewModel,
    onSignOut: () -> Unit
) {
    var currentDestination by remember { mutableStateOf(AdminDestination.DASHBOARD) }
    var previousDestination by remember { mutableStateOf(AdminDestination.DASHBOARD) }
    var managementSubTab by remember { mutableIntStateOf(0) }
    var showBulkKeyDialog by remember { mutableStateOf(false) }
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    val context = LocalContext.current

    val dashboardStats by viewModel.dashboardStats.collectAsState()
    val keysList by viewModel.keysList.collectAsState()
    val usersList by viewModel.usersList.collectAsState()
    val pendingDeposits by viewModel.pendingDeposits.collectAsState()
    val gameConfigs by viewModel.gameConfigs.collectAsState()
    val plans by viewModel.plans.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val adminUsername = viewModel.sessionManager.username ?: "Admin"
    val adminRole = viewModel.sessionManager.role ?: "admin"

    // APatch Official BackHandler
    BackHandler(enabled = true) {
        when {
            currentDestination == AdminDestination.PROFILE -> {
                currentDestination = previousDestination
            }
            currentDestination != AdminDestination.DASHBOARD -> {
                currentDestination = AdminDestination.DASHBOARD
            }
            else -> {
                val now = System.currentTimeMillis()
                if (now - lastBackPressTime < 2000L) {
                    (context as? Activity)?.finish()
                } else {
                    lastBackPressTime = now
                    Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val bottomBarDestinations = listOf(
        AdminDestination.DASHBOARD,
        AdminDestination.MANAGEMENT,
        AdminDestination.SETTINGS
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = currentDestination.labelRes),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (currentDestination == AdminDestination.PROFILE) {
                        IconButton(onClick = { currentDestination = previousDestination }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshAll() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh Telemetry",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (currentDestination != AdminDestination.PROFILE) {
                        IconButton(onClick = {
                            previousDestination = currentDestination
                            currentDestination = AdminDestination.PROFILE
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_user_profile),
                                contentDescription = "Profile",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (currentDestination in bottomBarDestinations) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    tonalElevation = 8.dp
                ) {
                    NavigationBar(
                        modifier = Modifier.clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                        tonalElevation = 0.dp,
                        containerColor = Color.Transparent
                    ) {
                        bottomBarDestinations.forEach { dest ->
                            val isSelected = currentDestination == dest
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentDestination = dest },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = if (isSelected) dest.selectedDrawable else dest.unselectedDrawable),
                                        contentDescription = stringResource(id = dest.labelRes),
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(id = dest.labelRes),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                },
                                alwaysShowLabel = false
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        // APatch Official Screen Transitions (tween 340ms, horizontal slide + fade for Profile, fade for tabs)
        AnimatedContent(
            targetState = currentDestination,
            transitionSpec = {
                if (targetState == AdminDestination.PROFILE) {
                    (slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(340)
                    ) + fadeIn(animationSpec = tween(340))) togetherWith
                    (slideOutHorizontally(
                        targetOffsetX = { -it / 4 },
                        animationSpec = tween(340)
                    ) + fadeOut(animationSpec = tween(340)))
                } else if (initialState == AdminDestination.PROFILE) {
                    (slideInHorizontally(
                        initialOffsetX = { -it / 4 },
                        animationSpec = tween(340)
                    ) + fadeIn(animationSpec = tween(340))) togetherWith
                    (scaleOut(
                        targetScale = 0.9f,
                        animationSpec = tween(340)
                    ) + fadeOut(animationSpec = tween(340)))
                } else {
                    fadeIn(animationSpec = tween(340)) togetherWith fadeOut(animationSpec = tween(340))
                }
            },
            label = "APatchAdminDestinationTransition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { destination ->
            when (destination) {
                AdminDestination.DASHBOARD -> AdminDashboardScreen(
                    stats = dashboardStats,
                    isLoading = isLoading,
                    onNavigateToManagement = { subTab ->
                        managementSubTab = subTab
                        currentDestination = AdminDestination.MANAGEMENT
                    },
                    onOpenBulkGenerate = { showBulkKeyDialog = true }
                )
                AdminDestination.MANAGEMENT -> AdminManagementScreen(
                    currentSubTab = managementSubTab,
                    onSubTabChanged = { managementSubTab = it },
                    keys = keysList,
                    users = usersList,
                    deposits = pendingDeposits,
                    isLoading = isLoading,
                    onRefresh = { viewModel.refreshAll() },
                    onActionClick = { id, act, hrs -> viewModel.executeKeyAction(id, act, hrs) },
                    onOpenBulkGenerate = { showBulkKeyDialog = true },
                    onUpdateBalance = { id, act, amt, notes -> viewModel.updateUserBalance(id, act, amt, notes) },
                    onChangeRole = { id, r -> viewModel.changeUserRole(id, r) },
                    onApproveDeposit = { id, amt -> viewModel.approveDeposit(id, amt) },
                    onRejectDeposit = { id, rsn -> viewModel.rejectDeposit(id, rsn) }
                )
                AdminDestination.SETTINGS -> AdminSettingsScreen(
                    gameConfigs = gameConfigs,
                    plans = plans,
                    isLoading = isLoading,
                    onRefresh = { viewModel.refreshAll() },
                    onUpdateGameConfig = { id, st, ver, esp, aim, not -> viewModel.updateGameConfig(id, st, ver, esp, aim, not) }
                )
                AdminDestination.PROFILE -> AdminProfileScreen(
                    username = adminUsername,
                    role = adminRole,
                    onSignOut = onSignOut
                )
            }
        }
    }

    if (showBulkKeyDialog) {
        BulkKeyGeneratorDialog(
            onDismiss = { showBulkKeyDialog = false },
            onGenerate = { q, dh, dt, gt, md, n ->
                viewModel.generateBulkKeys(q, dh, dt, gt, md, n)
            }
        )
    }
}
