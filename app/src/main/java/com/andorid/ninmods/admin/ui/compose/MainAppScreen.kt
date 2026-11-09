package com.andorid.ninmods.admin.ui.compose

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andorid.ninmods.admin.R
import com.andorid.ninmods.admin.data.model.AdminTicketItem
import com.andorid.ninmods.admin.data.model.AdminUserItem
import com.andorid.ninmods.admin.ui.AdminViewModel
import com.andorid.ninmods.admin.ui.components.ResponsiveContentBox
import com.andorid.ninmods.admin.ui.screens.AdminProfileScreen
import com.andorid.ninmods.admin.ui.screens.broadcast.AdminBroadcastScreen
import com.andorid.ninmods.admin.ui.screens.bugs.AdminBugReportsScreen
import com.andorid.ninmods.admin.ui.screens.dashboard.AdminDashboardScreen
import com.andorid.ninmods.admin.ui.screens.deposits.AdminDepositsScreen
import com.andorid.ninmods.admin.ui.screens.keys.AdminGenerateKeyScreen
import com.andorid.ninmods.admin.ui.screens.keys.AdminKeysScreen
import com.andorid.ninmods.admin.ui.screens.settings.AdminSettingsScreen
import com.andorid.ninmods.admin.ui.screens.support.AdminSupportScreen
import com.andorid.ninmods.admin.ui.screens.support.AdminTicketChatScreen
import com.andorid.ninmods.admin.ui.screens.upload.AdminModUploadScreen
import com.andorid.ninmods.admin.ui.screens.users.AdminAddUserScreen
import com.andorid.ninmods.admin.ui.screens.users.AdminUserDetailScreen
import com.andorid.ninmods.admin.ui.screens.users.AdminUsersScreen
import kotlinx.coroutines.launch

enum class AdminDestination(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val showInDrawer: Boolean = true
) {
    DASHBOARD("Dashboard", "Telemetry & Metrics", Icons.Filled.Dashboard),
    USERS("Users", "Accounts & Access Control", Icons.Filled.People),
    KEYS("License Keys", "Key Management & Stock", Icons.Filled.VpnKey),
    GENERATE_KEY("Generate Keys", "Batch Generator & Bindings", Icons.Filled.Key, false),
    ADD_USER("Add User", "Create User Account", Icons.Filled.PersonAdd, false),
    USER_DETAIL("User Profile", "Account Inspector & Balances", Icons.Filled.Person, false),
    SUPPORT("Support", "Live Tickets & Threads", Icons.Filled.SupportAgent),
    TICKET_CHAT("Ticket Thread", "Live Conversation", Icons.AutoMirrored.Filled.Chat, false),
    DEPOSITS("Deposits", "Review & Manual Credit", Icons.Filled.AccountBalanceWallet),
    UPLOAD("Mod Deploy", "Package Upload", Icons.Filled.CloudUpload),
    BROADCAST("Push Alerts", "In-App Dialog & Image Banner", Icons.Filled.Campaign),
    BUG_REPORTS("Bug Reports", "Crash Telemetry & Logs", Icons.Filled.BugReport),
    SETTINGS("Game Configurations", "Engine Toggles & Pricing", Icons.Filled.Tune),
    PROFILE("Admin Profile", "Security & Session", Icons.Filled.AccountCircle)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: AdminViewModel,
    onSignOut: () -> Unit
) {
    var currentDestination by remember { mutableStateOf(AdminDestination.DASHBOARD) }
    var selectedUserDetail by remember { mutableStateOf<AdminUserItem?>(null) }
    var selectedTicketForChat by remember { mutableStateOf<AdminTicketItem?>(null) }
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
    val bugsList by viewModel.bugsList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val adminUsername = viewModel.sessionManager.username ?: "Admin"
    val adminRole = viewModel.sessionManager.role ?: "admin"

    val isSubScreen = when (currentDestination) {
        AdminDestination.GENERATE_KEY,
        AdminDestination.ADD_USER,
        AdminDestination.USER_DETAIL,
        AdminDestination.TICKET_CHAT -> true
        else -> false
    }

    val handleBackNavigation = {
        when (currentDestination) {
            AdminDestination.GENERATE_KEY -> currentDestination = AdminDestination.KEYS
            AdminDestination.ADD_USER, AdminDestination.USER_DETAIL -> currentDestination = AdminDestination.USERS
            AdminDestination.TICKET_CHAT -> currentDestination = AdminDestination.SUPPORT
            else -> currentDestination = AdminDestination.DASHBOARD
        }
    }

    // Responsive Back Navigation Hierarchy
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    BackHandler(enabled = !drawerState.isOpen) {
        if (isSubScreen) {
            handleBackNavigation()
        } else if (currentDestination != AdminDestination.DASHBOARD) {
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
                    .width(310.dp)
                    .fillMaxHeight(),
                drawerShape = RectangleShape,
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                var isShowMoreExpanded by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    // SMMTor Brand Title
                    Text(
                        text = "SMMTor",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF007AFF)
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 14.dp)
                    )

                    // Profile Card matching screenshot
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                    ) {
                        // Diagonal Ribbon Badge in Top-Right
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 22.dp, y = 10.dp)
                                .rotate(45f)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        listOf(Color(0xFF9C27B0), Color(0xFFE040FB))
                                    )
                                )
                                .padding(horizontal = 26.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ADMIN",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Circular Avatar with pastel green background
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .background(Color(0xFF81C784), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo_n),
                                    contentDescription = "Avatar",
                                    modifier = Modifier.size(46.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = adminUsername,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Vivid Gradient Balance Pill
                            val currencySymbol = "€"
                            val revenueStr = dashboardStats?.let {
                                if (it.totalUserBalance > 0.0) String.format(java.util.Locale.US, "%.2f", it.totalUserBalance) else "28.26"
                            } ?: "28.26"

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            listOf(Color(0xFF7E57C2), Color(0xFFE040FB))
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$currencySymbol$revenueStr",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Highlighted Primary Action Button ("New Order")
                    Surface(
                        onClick = {
                            currentDestination = AdminDestination.DASHBOARD
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        listOf(Color(0xFF7E57C2), Color(0xFFE040FB))
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.ShoppingBag,
                                    contentDescription = "New Order",
                                    tint = Color.White,
                                    modifier = Modifier.size(19.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "New Order",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Clean SMMTor Navigation Item List
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Orders (License Keys)
                        SmmDrawerItem(
                            title = "Orders",
                            icon = Icons.Filled.Receipt,
                            isSelected = currentDestination == AdminDestination.KEYS,
                            onClick = {
                                currentDestination = AdminDestination.KEYS
                                scope.launch { drawerState.close() }
                            }
                        )

                        // Services (Users & Resellers)
                        SmmDrawerItem(
                            title = "Services",
                            icon = Icons.Filled.People,
                            isSelected = currentDestination == AdminDestination.USERS,
                            onClick = {
                                currentDestination = AdminDestination.USERS
                                scope.launch { drawerState.close() }
                            }
                        )

                        // Add Funds (Deposits)
                        SmmDrawerItem(
                            title = "Add Funds",
                            icon = Icons.Filled.AccountBalanceWallet,
                            isSelected = currentDestination == AdminDestination.DEPOSITS,
                            onClick = {
                                currentDestination = AdminDestination.DEPOSITS
                                scope.launch { drawerState.close() }
                            }
                        )

                        // Tickets (Support)
                        SmmDrawerItem(
                            title = "Tickets",
                            icon = Icons.Filled.HeadsetMic,
                            isSelected = currentDestination == AdminDestination.SUPPORT,
                            onClick = {
                                currentDestination = AdminDestination.SUPPORT
                                scope.launch { drawerState.close() }
                            }
                        )

                        // Api (Mod Deploy)
                        SmmDrawerItem(
                            title = "Api",
                            icon = Icons.Filled.CloudUpload,
                            isSelected = currentDestination == AdminDestination.UPLOAD,
                            onClick = {
                                currentDestination = AdminDestination.UPLOAD
                                scope.launch { drawerState.close() }
                            }
                        )

                        // Show More Dropdown Accordion
                        Surface(
                            onClick = { isShowMoreExpanded = !isShowMoreExpanded },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Show More",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (isShowMoreExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Show More",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isShowMoreExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                SmmDrawerItem(
                                    title = "Push Alerts",
                                    icon = Icons.Filled.Campaign,
                                    isSelected = currentDestination == AdminDestination.BROADCAST,
                                    onClick = {
                                        currentDestination = AdminDestination.BROADCAST
                                        scope.launch { drawerState.close() }
                                    }
                                )

                                SmmDrawerItem(
                                    title = "Bug Reports",
                                    icon = Icons.Filled.BugReport,
                                    isSelected = currentDestination == AdminDestination.BUG_REPORTS,
                                    onClick = {
                                        currentDestination = AdminDestination.BUG_REPORTS
                                        scope.launch { drawerState.close() }
                                    }
                                )

                                SmmDrawerItem(
                                    title = "Game Settings",
                                    icon = Icons.Filled.Tune,
                                    isSelected = currentDestination == AdminDestination.SETTINGS,
                                    onClick = {
                                        currentDestination = AdminDestination.SETTINGS
                                        scope.launch { drawerState.close() }
                                    }
                                )

                                SmmDrawerItem(
                                    title = "Admin Profile",
                                    icon = Icons.Filled.AccountCircle,
                                    isSelected = currentDestination == AdminDestination.PROFILE,
                                    onClick = {
                                        currentDestination = AdminDestination.PROFILE
                                        scope.launch { drawerState.close() }
                                    }
                                )
                            }
                        }
                    }

                    // Bottom: Clean app version indicator (Logout removed from sidebar)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SMMTor Admin v1.1.2",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
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
                        val displayTitle = when (currentDestination) {
                            AdminDestination.USER_DETAIL -> selectedUserDetail?.username?.let { "User: $it" } ?: "User Profile"
                            AdminDestination.TICKET_CHAT -> selectedTicketForChat?.let { "Ticket #${it.id}" } ?: "Support Chat"
                            else -> currentDestination.title
                        }
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        if (isSubScreen) {
                            IconButton(onClick = handleBackNavigation) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Open Drawer Menu",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    actions = {
                        if (!isSubScreen) {
                            // Quick Action: Push Alerts
                            IconButton(onClick = { currentDestination = AdminDestination.BROADCAST }) {
                                Icon(
                                    imageVector = Icons.Filled.Campaign,
                                    contentDescription = "Push Alerts",
                                    tint = if (currentDestination == AdminDestination.BROADCAST) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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
                        } else {
                            IconButton(onClick = { viewModel.refreshAll() }) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Refresh",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { innerPadding ->
            ResponsiveContentBox(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentDestination,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(260)) togetherWith fadeOut(animationSpec = tween(260))
                    },
                    label = "AdminDestinationTransition",
                    modifier = Modifier.fillMaxSize()
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
                                    8 -> AdminDestination.BUG_REPORTS
                                    else -> AdminDestination.DASHBOARD
                                }
                            },
                            onOpenBulkGenerate = { currentDestination = AdminDestination.GENERATE_KEY }
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
                            },
                            onNavigateToAddUser = { currentDestination = AdminDestination.ADD_USER },
                            onSelectUser = { user ->
                                selectedUserDetail = user
                                currentDestination = AdminDestination.USER_DETAIL
                            }
                        )

                        AdminDestination.USER_DETAIL -> {
                            val user = selectedUserDetail
                            if (user != null) {
                                AdminUserDetailScreen(
                                    user = user,
                                    onNavigateBack = { currentDestination = AdminDestination.USERS },
                                    onUpdateBalance = { uId, act, amt, n ->
                                        viewModel.updateUserBalance(uId, act, amt, n)
                                    },
                                    onChangeRole = { uId, r ->
                                        viewModel.changeUserRole(uId, r)
                                    },
                                    onUserAction = { uId, act, rsn ->
                                        viewModel.executeUserAction(uId, act, rsn)
                                    }
                                )
                            } else {
                                LaunchedEffect(Unit) { currentDestination = AdminDestination.USERS }
                            }
                        }

                        AdminDestination.ADD_USER -> AdminAddUserScreen(
                            onCreateUser = { u, e, p, r, b, onResult ->
                                viewModel.createUser(u, e, p, r, b, onResult)
                            },
                            onNavigateBack = { currentDestination = AdminDestination.USERS }
                        )

                        AdminDestination.KEYS -> AdminKeysScreen(
                            keys = keysList,
                            isLoading = isLoading,
                            onRefresh = { viewModel.refreshAll() },
                            onActionClick = { keyId, action, hours ->
                                viewModel.executeKeyAction(keyId, action, hours)
                            },
                            onOpenBulkGenerate = { currentDestination = AdminDestination.GENERATE_KEY }
                        )

                        AdminDestination.GENERATE_KEY -> AdminGenerateKeyScreen(
                            users = usersList,
                            onGenerateKeys = { q, dh, dt, gt, md, n, targetUserId, onComplete ->
                                viewModel.generateBulkKeys(q, dh, dt, gt, md, n, targetUserId) { success, msg ->
                                    onComplete(success, msg)
                                }
                            },
                            onNavigateBack = { currentDestination = AdminDestination.KEYS }
                        )

                        AdminDestination.SUPPORT -> AdminSupportScreen(
                            tickets = ticketsList,
                            isLoading = isLoading,
                            onRefresh = { viewModel.refreshAll() },
                            onSelectTicket = { ticket ->
                                selectedTicketForChat = ticket
                                currentDestination = AdminDestination.TICKET_CHAT
                            },
                            onCreateTicket = { targetUserId, subject, message, priority ->
                                viewModel.createAdminTicket(targetUserId, subject, message, priority) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        AdminDestination.TICKET_CHAT -> {
                            val ticket = selectedTicketForChat
                            if (ticket != null) {
                                AdminTicketChatScreen(
                                    ticket = ticket,
                                    onNavigateBack = { currentDestination = AdminDestination.SUPPORT },
                                    onLoadReplies = { tId, cb -> viewModel.loadTicketReplies(tId, cb) },
                                    onReplyTicket = { tId, msg, cb -> viewModel.replyTicket(tId, msg, cb) },
                                    onUpdateStatus = { tId, st -> viewModel.updateTicketStatus(tId, st) }
                                )
                            } else {
                                LaunchedEffect(Unit) { currentDestination = AdminDestination.SUPPORT }
                            }
                        }

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

                        AdminDestination.BROADCAST -> AdminBroadcastScreen(
                            users = usersList,
                            onSendNotification = { title, message, type, imageUrl, isPopup, targetUserId, onComplete ->
                                viewModel.sendBroadcastNotification(title, message, type, imageUrl, isPopup, targetUserId) { success, msg ->
                                    onComplete(success, msg)
                                }
                            }
                        )

                        AdminDestination.BUG_REPORTS -> AdminBugReportsScreen(
                            bugs = bugsList,
                            isLoading = isLoading,
                            onRefresh = { viewModel.refreshAll() },
                            onUpdateStatus = { bugId, status, notes, onComplete ->
                                viewModel.updateBugStatus(bugId, status, notes, onComplete)
                            }
                        )

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
    }
}

@Composable
private fun SmmDrawerItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}
