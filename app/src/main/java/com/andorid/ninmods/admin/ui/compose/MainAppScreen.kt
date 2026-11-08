package com.andorid.ninmods.admin.ui.compose

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andorid.ninmods.admin.R
import com.andorid.ninmods.admin.ui.AdminViewModel
import com.andorid.ninmods.admin.ui.dialogs.BulkKeyGeneratorBottomSheet
import com.andorid.ninmods.admin.ui.dialogs.SendNotificationBottomSheet
import com.andorid.ninmods.admin.ui.screens.AdminProfileScreen
import com.andorid.ninmods.admin.ui.screens.dashboard.AdminDashboardScreen
import com.andorid.ninmods.admin.ui.screens.deposits.AdminDepositsScreen
import com.andorid.ninmods.admin.ui.screens.keys.AdminKeysScreen
import com.andorid.ninmods.admin.ui.screens.settings.AdminSettingsScreen
import com.andorid.ninmods.admin.ui.screens.support.AdminSupportScreen
import com.andorid.ninmods.admin.ui.screens.upload.AdminModUploadScreen
import com.andorid.ninmods.admin.ui.screens.users.AdminUsersScreen
import kotlinx.coroutines.launch

enum class AdminDestination(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
) {
    DASHBOARD("Dashboard", "Telemetry & Metrics", Icons.Filled.Dashboard),
    USERS("Users Management", "Accounts & Access Control", Icons.Filled.People),
    KEYS("License Keys", "Key Generator & Resets", Icons.Filled.VpnKey),
    SUPPORT("Support Messaging", "Live Tickets & Threads", Icons.Filled.SupportAgent),
    DEPOSITS("Deposits & Wallets", "Review & Manual Credit", Icons.Filled.AccountBalanceWallet),
    UPLOAD("Mod Deployment", "Anti-Shell ZIP Upload", Icons.Filled.CloudUpload),
    BROADCAST("Push Popups & Alerts", "In-App Dialog & Image Banner", Icons.Filled.Campaign),
    SETTINGS("Game Configurations", "FF Versions & Features", Icons.Filled.Tune),
    PROFILE("Admin Profile", "Security & Session", Icons.Filled.AccountCircle)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: AdminViewModel,
    onSignOut: () -> Unit
) {
    var currentDestination by remember { mutableStateOf(AdminDestination.DASHBOARD) }
    var showBulkKeySheet by remember { mutableStateOf(false) }
    var showBroadcastSheet by remember { mutableStateOf(false) }
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val dashboardStats by viewModel.dashboardStats.collectAsState()
    val keysList by viewModel.keysList.collectAsState()
    val usersList by viewModel.usersList.collectAsState()
    val allDeposits by viewModel.allDeposits.collectAsState()
    val ticketsList by viewModel.ticketsList.collectAsState()
    val gameConfigs by viewModel.gameConfigs.collectAsState()
    val plans by viewModel.plans.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val adminUsername = viewModel.sessionManager.username ?: "Admin"
    val adminRole = viewModel.sessionManager.role ?: "admin"

    // APatch Drawer and BackHandler Hierarchy
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    BackHandler(enabled = !drawerState.isOpen) {
        if (currentDestination != AdminDestination.DASHBOARD) {
            currentDestination = AdminDestination.DASHBOARD
        } else {
            val now = System.currentTimeMillis()
            if (now - lastBackPressTime < 2000L) {
                (context as? Activity)?.finish()
            } else {
                lastBackPressTime = now
                Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight(),
                drawerShape = RectangleShape, // Straight non-rounded edges strictly matching APatch official UI
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    // Header Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Image(
                                        painter = painterResource(id = R.drawable.logo_n),
                                        contentDescription = "Logo",
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = "NINMODS ADMIN",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = adminUsername.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Destination List
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AdminDestination.values().forEach { destination ->
                            val isSelected = currentDestination == destination
                            NavigationDrawerItem(
                                icon = {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.title,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                label = {
                                    Column {
                                        Text(
                                            text = destination.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = destination.subtitle,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                            fontSize = 11.sp
                                        )
                                    }
                                },
                                selected = isSelected,
                                onClick = {
                                    if (destination == AdminDestination.BROADCAST) {
                                        showBroadcastSheet = true
                                        scope.launch { drawerState.close() }
                                    } else {
                                        currentDestination = destination
                                        scope.launch { drawerState.close() }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    unselectedContainerColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Footer / Sign Out
                    Surface(
                        onClick = {
                            scope.launch { drawerState.close() }
                            onSignOut()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Sign Out",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Sign Out Session",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "APatch Admin Engine v1.0.2",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = currentDestination.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = currentDestination.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Open Drawer Menu",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        // Quick Action: Push Popups / Notification
                        IconButton(onClick = { showBroadcastSheet = true }) {
                            Icon(
                                imageVector = Icons.Filled.Campaign,
                                contentDescription = "Push Popup Broadcast",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(onClick = { viewModel.refreshAll() }) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh Data",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { currentDestination = AdminDestination.PROFILE }) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "Admin Profile",
                                tint = if (currentDestination == AdminDestination.PROFILE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = {
                    fadeIn(animationSpec = tween(280)) togetherWith fadeOut(animationSpec = tween(280))
                },
                label = "AdminDestinationTransition",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { destination ->
                when (destination) {
                    AdminDestination.DASHBOARD -> AdminDashboardScreen(
                        stats = dashboardStats,
                        isLoading = isLoading,
                        onNavigateToPage = { pageIndex ->
                            currentDestination = when (pageIndex) {
                                1 -> AdminDestination.USERS
                                2 -> AdminDestination.KEYS
                                3 -> AdminDestination.SUPPORT
                                4 -> AdminDestination.DEPOSITS
                                5 -> AdminDestination.UPLOAD
                                6 -> AdminDestination.SETTINGS
                                7 -> AdminDestination.PROFILE
                                else -> AdminDestination.DASHBOARD
                            }
                        },
                        onOpenBulkGenerate = { showBulkKeySheet = true }
                    )

                    AdminDestination.USERS -> AdminUsersScreen(
                        users = usersList,
                        isLoading = isLoading,
                        onRefresh = { viewModel.refreshAll() },
                        onUserAction = { userId, action, reason ->
                            viewModel.executeUserAction(userId, action, reason)
                        },
                        onUpdateBalance = { userId, action, amount, notes ->
                            viewModel.updateUserBalance(userId, action, amount, notes)
                        },
                        onChangeRole = { userId, newRole ->
                            viewModel.changeUserRole(userId, newRole)
                        }
                    )

                    AdminDestination.KEYS -> AdminKeysScreen(
                        keys = keysList,
                        isLoading = isLoading,
                        onRefresh = { viewModel.refreshAll() },
                        onActionClick = { keyId, action, hours ->
                            viewModel.executeKeyAction(keyId, action, hours)
                        },
                        onOpenBulkGenerate = { showBulkKeySheet = true }
                    )

                    AdminDestination.SUPPORT -> AdminSupportScreen(
                        tickets = ticketsList,
                        isLoading = isLoading,
                        onRefresh = { viewModel.refreshAll() },
                        onLoadTicketReplies = { ticketId, onResult ->
                            viewModel.loadTicketReplies(ticketId, onResult)
                        },
                        onReplyTicket = { ticketId, message, onComplete ->
                            viewModel.replyTicket(ticketId, message, onComplete)
                        },
                        onUpdateTicketStatus = { ticketId, status ->
                            viewModel.updateTicketStatus(ticketId, status)
                        },
                        onCreateTicket = { targetUserId, subject, message, priority ->
                            viewModel.createAdminTicket(targetUserId, subject, message, priority) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    AdminDestination.DEPOSITS -> AdminDepositsScreen(
                        deposits = allDeposits,
                        isLoading = isLoading,
                        onRefresh = { viewModel.refreshAll() },
                        onApprove = { depositId, amount ->
                            viewModel.approveDeposit(depositId, amount)
                        },
                        onReject = { depositId, reason ->
                            viewModel.rejectDeposit(depositId, reason)
                        },
                        onAddManualDeposit = { userId, amount, method, notes ->
                            viewModel.addManualDeposit(userId, amount, method, notes)
                        }
                    )

                    AdminDestination.UPLOAD -> AdminModUploadScreen(
                        onUploadMod = { modType, fileName, base64Data, onResult ->
                            viewModel.uploadModZip(modType, fileName, base64Data, onResult)
                        }
                    )

                    AdminDestination.BROADCAST -> {
                        LaunchedEffect(Unit) {
                            showBroadcastSheet = true
                            currentDestination = AdminDestination.DASHBOARD
                        }
                    }

                    AdminDestination.SETTINGS -> AdminSettingsScreen(
                        gameConfigs = gameConfigs,
                        plans = plans,
                        isLoading = isLoading,
                        onRefresh = { viewModel.refreshAll() },
                        onUpdateGameConfig = { configId, status, version, esp, aimbot, notice ->
                            viewModel.updateGameConfig(configId, status, version, esp, aimbot, notice)
                        }
                    )

                    AdminDestination.PROFILE -> AdminProfileScreen(
                        username = adminUsername,
                        role = adminRole,
                        onSignOut = onSignOut
                    )
                }
            }
        }
    }

    // Bulk Key Generator Bottom Sheet (slides up from bottom)
    if (showBulkKeySheet) {
        BulkKeyGeneratorBottomSheet(
            onDismiss = { showBulkKeySheet = false },
            onGenerate = { q, dh, dt, gt, md, n ->
                viewModel.generateBulkKeys(q, dh, dt, gt, md, n)
            }
        )
    }

    // In-App Popup / Push Broadcast Bottom Sheet (slides up from bottom)
    if (showBroadcastSheet) {
        SendNotificationBottomSheet(
            onDismiss = { showBroadcastSheet = false },
            onSendNotification = { title, message, type, imageUrl, isPopup, targetUserId ->
                viewModel.sendBroadcastNotification(title, message, type, imageUrl, isPopup, targetUserId) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}
