package com.andorid.ninmods.admin.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andorid.ninmods.admin.data.AdminSessionManager
import com.andorid.ninmods.admin.data.model.*
import com.andorid.ninmods.admin.data.network.AdminApiClient
import com.andorid.ninmods.admin.ui.components.AdminTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    val sessionManager = AdminSessionManager(application)

    private val _isLoggedIn = MutableStateFlow(sessionManager.isLoggedIn)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentTab = MutableStateFlow(AdminTab.DASHBOARD)
    val currentTab: StateFlow<AdminTab> = _currentTab.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // State data
    private val _dashboardStats = MutableStateFlow<DashboardStats?>(null)
    val dashboardStats: StateFlow<DashboardStats?> = _dashboardStats.asStateFlow()

    private val _keysList = MutableStateFlow<List<AdminLicenseKeyItem>>(emptyList())
    val keysList: StateFlow<List<AdminLicenseKeyItem>> = _keysList.asStateFlow()

    private val _usersList = MutableStateFlow<List<AdminUserItem>>(emptyList())
    val usersList: StateFlow<List<AdminUserItem>> = _usersList.asStateFlow()

    private val _pendingDeposits = MutableStateFlow<List<PendingDepositItem>>(emptyList())
    val pendingDeposits: StateFlow<List<PendingDepositItem>> = _pendingDeposits.asStateFlow()

    private val _allDeposits = MutableStateFlow<List<PendingDepositItem>>(emptyList())
    val allDeposits: StateFlow<List<PendingDepositItem>> = _allDeposits.asStateFlow()

    private val _ticketsList = MutableStateFlow<List<AdminTicketItem>>(emptyList())
    val ticketsList: StateFlow<List<AdminTicketItem>> = _ticketsList.asStateFlow()

    private val _gameConfigs = MutableStateFlow<List<GameConfigItem>>(emptyList())
    val gameConfigs: StateFlow<List<GameConfigItem>> = _gameConfigs.asStateFlow()

    private val _plans = MutableStateFlow<List<SubscriptionPlanItem>>(emptyList())
    val plans: StateFlow<List<SubscriptionPlanItem>> = _plans.asStateFlow()

    init {
        if (_isLoggedIn.value) {
            refreshAll()
        }
    }

    fun setTab(tab: AdminTab) {
        _currentTab.value = tab
    }

    fun login(username: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val res = AdminApiClient.login(username, pass)
            if (res.isSuccess) {
                val profile = res.getOrThrow()
                sessionManager.saveSession(
                    key = profile.apiKey,
                    user = profile.username,
                    mail = profile.email,
                    r = profile.role
                )
                _isLoggedIn.value = true
                refreshAll()
            } else {
                _errorMessage.value = res.exceptionOrNull()?.message ?: "Login failed"
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        sessionManager.logout()
        _isLoggedIn.value = false
        _dashboardStats.value = null
        _keysList.value = emptyList()
        _usersList.value = emptyList()
        _pendingDeposits.value = emptyList()
        _allDeposits.value = emptyList()
        _ticketsList.value = emptyList()
        _gameConfigs.value = emptyList()
        _plans.value = emptyList()
    }

    fun refreshAll() {
        val key = sessionManager.apiKey ?: return
        viewModelScope.launch {
            _isLoading.value = true
            // 1. Dashboard
            launch {
                val r = AdminApiClient.getDashboard(key)
                if (r.isSuccess) _dashboardStats.value = r.getOrNull()
            }
            // 2. Keys
            launch {
                val r = AdminApiClient.getKeys(key)
                if (r.isSuccess) _keysList.value = r.getOrNull() ?: emptyList()
            }
            // 3. Users
            launch {
                val r = AdminApiClient.getUsers(key)
                if (r.isSuccess) _usersList.value = r.getOrNull() ?: emptyList()
            }
            // 4. Deposits
            launch {
                val r = AdminApiClient.getAllDeposits(key)
                if (r.isSuccess) {
                    val list = r.getOrNull() ?: emptyList()
                    _allDeposits.value = list
                    _pendingDeposits.value = list.filter { it.status.equals("pending", ignoreCase = true) }
                }
            }
            // 5. Tickets
            launch {
                val r = AdminApiClient.getSupportTickets(key)
                if (r.isSuccess) _ticketsList.value = r.getOrNull() ?: emptyList()
            }
            // 6. Game Configs
            launch {
                val r = AdminApiClient.getGameConfigs(key)
                if (r.isSuccess) _gameConfigs.value = r.getOrNull() ?: emptyList()
            }
            // 7. Plans
            launch {
                val r = AdminApiClient.getAdminPlans(key)
                if (r.isSuccess) _plans.value = r.getOrNull() ?: emptyList()
            }
            _isLoading.value = false
        }
    }

    fun executeUserAction(userId: Int, action: String, reason: String) {
        val key = sessionManager.apiKey ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val r = AdminApiClient.executeUserAction(key, userId, action, reason)
            if (r.isSuccess) {
                val usersRes = AdminApiClient.getUsers(key)
                if (usersRes.isSuccess) _usersList.value = usersRes.getOrNull() ?: emptyList()
                val keysRes = AdminApiClient.getKeys(key)
                if (keysRes.isSuccess) _keysList.value = keysRes.getOrNull() ?: emptyList()
                val dashRes = AdminApiClient.getDashboard(key)
                if (dashRes.isSuccess) _dashboardStats.value = dashRes.getOrNull()
            }
            _isLoading.value = false
        }
    }

    fun addManualDeposit(userId: Int, amount: Double, method: String, notes: String) {
        val key = sessionManager.apiKey ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val r = AdminApiClient.addManualDeposit(key, userId, amount, method, notes)
            if (r.isSuccess) {
                val depRes = AdminApiClient.getAllDeposits(key)
                if (depRes.isSuccess) {
                    val list = depRes.getOrNull() ?: emptyList()
                    _allDeposits.value = list
                    _pendingDeposits.value = list.filter { it.status.equals("pending", ignoreCase = true) }
                }
                val usersRes = AdminApiClient.getUsers(key)
                if (usersRes.isSuccess) _usersList.value = usersRes.getOrNull() ?: emptyList()
                val dashRes = AdminApiClient.getDashboard(key)
                if (dashRes.isSuccess) _dashboardStats.value = dashRes.getOrNull()
            }
            _isLoading.value = false
        }
    }

    fun loadTicketReplies(ticketId: Int, onResult: (List<AdminTicketReplyItem>) -> Unit) {
        val key = sessionManager.apiKey ?: return
        viewModelScope.launch {
            val r = AdminApiClient.getTicketReplies(key, ticketId)
            onResult(r.getOrNull() ?: emptyList())
        }
    }

    fun replyTicket(ticketId: Int, message: String, onComplete: () -> Unit) {
        val key = sessionManager.apiKey ?: return
        viewModelScope.launch {
            val r = AdminApiClient.replyTicket(key, ticketId, message)
            if (r.isSuccess) {
                val ticketsRes = AdminApiClient.getSupportTickets(key)
                if (ticketsRes.isSuccess) _ticketsList.value = ticketsRes.getOrNull() ?: emptyList()
                onComplete()
            }
        }
    }

    fun updateTicketStatus(ticketId: Int, status: String) {
        val key = sessionManager.apiKey ?: return
        viewModelScope.launch {
            val r = AdminApiClient.updateTicketStatus(key, ticketId, status)
            if (r.isSuccess) {
                val ticketsRes = AdminApiClient.getSupportTickets(key)
                if (ticketsRes.isSuccess) _ticketsList.value = ticketsRes.getOrNull() ?: emptyList()
            }
        }
    }

    fun uploadModZip(
        modType: String,
        fileName: String,
        base64Data: String,
        onResult: (Result<ModUploadResult>) -> Unit
    ) {
        val key = sessionManager.apiKey ?: return
        viewModelScope.launch {
            val r = AdminApiClient.uploadModZip(key, modType, fileName, base64Data)
            onResult(r)
        }
    }

    fun generateBulkKeys(quantity: Int, durationHours: Int, durationType: String, gameType: String, maxDevices: Int, notes: String) {
        val key = sessionManager.apiKey ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val r = AdminApiClient.generateBulkKeys(key, quantity, durationHours, durationType, gameType, maxDevices, notes)
            if (r.isSuccess) {
                val keysRes = AdminApiClient.getKeys(key)
                if (keysRes.isSuccess) _keysList.value = keysRes.getOrNull() ?: emptyList()
                val dashRes = AdminApiClient.getDashboard(key)
                if (dashRes.isSuccess) _dashboardStats.value = dashRes.getOrNull()
            }
            _isLoading.value = false
        }
    }

    fun executeKeyAction(keyId: Int, action: String, hours: Int) {
        val key = sessionManager.apiKey ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val r = AdminApiClient.keyAction(key, keyId, action, hours)
            if (r.isSuccess) {
                val keysRes = AdminApiClient.getKeys(key)
                if (keysRes.isSuccess) _keysList.value = keysRes.getOrNull() ?: emptyList()
            }
            _isLoading.value = false
        }
    }

    fun updateUserBalance(userId: Int, action: String, amount: Double, notes: String) {
        val key = sessionManager.apiKey ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val r = AdminApiClient.updateUserBalance(key, userId, action, amount, notes)
            if (r.isSuccess) {
                val usersRes = AdminApiClient.getUsers(key)
                if (usersRes.isSuccess) _usersList.value = usersRes.getOrNull() ?: emptyList()
                val dashRes = AdminApiClient.getDashboard(key)
                if (dashRes.isSuccess) _dashboardStats.value = dashRes.getOrNull()
            }
            _isLoading.value = false
        }
    }

    fun changeUserRole(userId: Int, newRole: String) {
        val key = sessionManager.apiKey ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val r = AdminApiClient.changeUserRole(key, userId, newRole)
            if (r.isSuccess) {
                val usersRes = AdminApiClient.getUsers(key)
                if (usersRes.isSuccess) _usersList.value = usersRes.getOrNull() ?: emptyList()
            }
            _isLoading.value = false
        }
    }

    fun approveDeposit(depositId: Int, amount: Double?) {
        val key = sessionManager.apiKey ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val r = AdminApiClient.approveDeposit(key, depositId, amount)
            if (r.isSuccess) {
                val depRes = AdminApiClient.getPendingDeposits(key)
                if (depRes.isSuccess) _pendingDeposits.value = depRes.getOrNull() ?: emptyList()
                val dashRes = AdminApiClient.getDashboard(key)
                if (dashRes.isSuccess) _dashboardStats.value = dashRes.getOrNull()
            }
            _isLoading.value = false
        }
    }

    fun rejectDeposit(depositId: Int, reason: String) {
        val key = sessionManager.apiKey ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val r = AdminApiClient.rejectDeposit(key, depositId, reason)
            if (r.isSuccess) {
                val depRes = AdminApiClient.getPendingDeposits(key)
                if (depRes.isSuccess) _pendingDeposits.value = depRes.getOrNull() ?: emptyList()
                val dashRes = AdminApiClient.getDashboard(key)
                if (dashRes.isSuccess) _dashboardStats.value = dashRes.getOrNull()
            }
            _isLoading.value = false
        }
    }

    fun updateGameConfig(configId: Int, status: String, version: String, esp: Boolean, aimbot: Boolean, notice: String) {
        val key = sessionManager.apiKey ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val r = AdminApiClient.updateGameConfig(key, configId, status, version, esp, aimbot, notice)
            if (r.isSuccess) {
                val cfgRes = AdminApiClient.getGameConfigs(key)
                if (cfgRes.isSuccess) _gameConfigs.value = cfgRes.getOrNull() ?: emptyList()
            }
            _isLoading.value = false
        }
    }

    fun sendBroadcastNotification(
        title: String,
        message: String,
        type: String,
        imageUrl: String?,
        isPopup: Boolean,
        targetUserId: Int?,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val key = sessionManager.apiKey ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val r = AdminApiClient.sendBroadcastNotification(key, title, message, type, imageUrl, isPopup, targetUserId)
            if (r.isSuccess) {
                onResult(true, r.getOrNull() ?: "Broadcast sent")
            } else {
                onResult(false, r.exceptionOrNull()?.message ?: "Failed to send")
            }
            _isLoading.value = false
        }
    }

    fun createAdminTicket(
        targetUserId: Int,
        subject: String,
        message: String,
        priority: String,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val key = sessionManager.apiKey ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val r = AdminApiClient.createAdminTicket(key, targetUserId, subject, message, priority)
            if (r.isSuccess) {
                val ticketsRes = AdminApiClient.getSupportTickets(key)
                if (ticketsRes.isSuccess) _ticketsList.value = ticketsRes.getOrNull() ?: emptyList()
                onResult(true, r.getOrNull() ?: "Ticket created")
            } else {
                onResult(false, r.exceptionOrNull()?.message ?: "Failed to create ticket")
            }
            _isLoading.value = false
        }
    }
}
