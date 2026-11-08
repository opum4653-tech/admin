package com.andorid.ninmods.admin.data.network

import com.andorid.ninmods.admin.config.AdminConfig
import com.andorid.ninmods.admin.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object AdminApiClient {

    private fun openConnection(endpoint: String, method: String, apiKey: String? = null): HttpURLConnection {
        val fullUrl = if (endpoint.startsWith("http")) endpoint else "${AdminConfig.BASE_URL}${AdminConfig.API_PREFIX}$endpoint"
        val url = URL(fullUrl)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = method
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.setRequestProperty("Accept", "application/json")
        conn.setRequestProperty("User-Agent", "NINMODS-AdminApp/1.0")
        if (!apiKey.isNullOrBlank()) {
            conn.setRequestProperty("Authorization", "Bearer $apiKey")
            conn.setRequestProperty("x-admin-key", apiKey)
        }
        if (method in listOf("POST", "PUT", "DELETE")) {
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.doOutput = true
        }
        return conn
    }

    private fun sendJson(conn: HttpURLConnection, jsonBody: String) {
        OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
            writer.write(jsonBody)
            writer.flush()
        }
    }

    private fun readResponse(conn: HttpURLConnection): String {
        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream ?: conn.inputStream
        return BufferedReader(InputStreamReader(stream, "UTF-8")).use { it.readText() }
    }

    suspend fun login(username: String, password: String): Result<AdminProfile> = withContext(Dispatchers.IO) {
        try {
            val conn = openConnection("/auth/login", "POST")
            val payload = JSONObject().apply {
                put("username", username.trim())
                put("password", password.trim())
            }
            sendJson(conn, payload.toString())
            val resText = readResponse(conn)
            val json = JSONObject(resText)
            if (conn.responseCode == 200 && json.optBoolean("success", true)) {
                Result.success(
                    AdminProfile(
                        adminId = json.optInt("admin_id"),
                        username = json.optString("username"),
                        email = json.optString("email"),
                        role = json.optString("role", "admin"),
                        apiKey = json.optString("api_key")
                    )
                )
            } else {
                Result.failure(Exception(json.optString("error", "Admin login failed")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDashboard(apiKey: String): Result<DashboardStats> = withContext(Dispatchers.IO) {
        try {
            val conn = openConnection("/dashboard", "GET", apiKey)
            val resText = readResponse(conn)
            val json = JSONObject(resText)
            if (conn.responseCode == 200 && json.optBoolean("success", true)) {
                val stats = json.optJSONObject("stats") ?: JSONObject()
                val users = stats.optJSONObject("users") ?: JSONObject()
                val keys = stats.optJSONObject("keys") ?: JSONObject()
                val finance = stats.optJSONObject("finance") ?: JSONObject()
                val support = stats.optJSONObject("support") ?: JSONObject()
                val bugs = stats.optJSONObject("bugs") ?: JSONObject()

                Result.success(
                    DashboardStats(
                        totalUsers = users.optInt("total_users"),
                        totalResellers = users.optInt("total_resellers"),
                        totalVipResellers = users.optInt("total_vip_resellers"),
                        totalUserBalance = users.optDouble("total_user_balance", 0.0),
                        totalKeys = keys.optInt("total_keys"),
                        activeKeys = keys.optInt("active_keys"),
                        unusedKeys = keys.optInt("unused_keys"),
                        expiredKeys = keys.optInt("expired_keys"),
                        bannedKeys = keys.optInt("banned_keys"),
                        pendingDeposits = finance.optInt("pending_deposits"),
                        pendingDepositAmount = finance.optDouble("pending_deposit_amount", 0.0),
                        openTickets = support.optInt("open_tickets"),
                        totalBugReports = bugs.optInt("total_bug_reports")
                    )
                )
            } else {
                Result.failure(Exception(json.optString("error", "Failed to load dashboard")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getKeys(apiKey: String, status: String? = null, search: String? = null): Result<List<AdminLicenseKeyItem>> = withContext(Dispatchers.IO) {
        try {
            val queryParams = mutableListOf<String>()
            if (!status.isNullOrBlank() && status != "ALL") queryParams.add("status=${URLEncoder.encode(status.lowercase(), "UTF-8")}")
            if (!search.isNullOrBlank()) queryParams.add("search=${URLEncoder.encode(search, "UTF-8")}")
            val q = if (queryParams.isNotEmpty()) "?" + queryParams.joinToString("&") else ""

            val conn = openConnection("/keys$q", "GET", apiKey)
            val resText = readResponse(conn)
            val json = JSONObject(resText)
            if (conn.responseCode == 200 && json.optBoolean("success", true)) {
                val array = json.optJSONArray("keys") ?: JSONArray()
                val list = mutableListOf<AdminLicenseKeyItem>()
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    list.add(
                        AdminLicenseKeyItem(
                            id = item.optInt("id"),
                            licenseKey = item.optString("license_key"),
                            gameType = item.optString("game_type", "all"),
                            durationType = item.optString("duration_type", "custom"),
                            durationHours = item.optInt("duration_hours", 24),
                            status = item.optString("status", "unused"),
                            hwid = item.optString("hwid").takeIf { it.isNotBlank() && it != "null" },
                            deviceModel = item.optString("device_model").takeIf { it.isNotBlank() && it != "null" },
                            maxDevices = item.optInt("max_devices", 1),
                            maxResets = item.optInt("max_resets", 5),
                            resetCount = item.optInt("reset_count", 0),
                            expiresAt = item.optString("expires_at").takeIf { it.isNotBlank() && it != "null" },
                            notes = item.optString("notes").takeIf { it.isNotBlank() && it != "null" },
                            createdAt = item.optString("created_at")
                        )
                    )
                }
                Result.success(list)
            } else {
                Result.failure(Exception(json.optString("error", "Failed to load license keys")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateBulkKeys(
        apiKey: String,
        quantity: Int,
        durationHours: Int,
        durationType: String,
        gameType: String,
        maxDevices: Int,
        notes: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val conn = openConnection("/keys/generate", "POST", apiKey)
            val payload = JSONObject().apply {
                put("quantity", quantity)
                put("duration_hours", durationHours)
                put("duration_type", durationType)
                put("game_type", gameType)
                put("max_devices", maxDevices)
                put("notes", notes)
            }
            sendJson(conn, payload.toString())
            val resText = readResponse(conn)
            val json = JSONObject(resText)
            if (conn.responseCode in 200..201 && json.optBoolean("success", true)) {
                Result.success(json.optString("message", "Generated $quantity keys successfully"))
            } else {
                Result.failure(Exception(json.optString("error", "Key generation failed")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun keyAction(apiKey: String, keyId: Int, action: String, hours: Int = 24): Result<String> = withContext(Dispatchers.IO) {
        try {
            val conn = openConnection("/keys/$keyId/action", "POST", apiKey)
            val payload = JSONObject().apply {
                put("action", action)
                if (action == "extend") put("hours", hours)
            }
            sendJson(conn, payload.toString())
            val resText = readResponse(conn)
            val json = JSONObject(resText)
            if (conn.responseCode == 200 && json.optBoolean("success", true)) {
                Result.success(json.optString("message", "Action applied successfully"))
            } else {
                Result.failure(Exception(json.optString("error", "Action failed")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsers(apiKey: String, search: String? = null, role: String? = null): Result<List<AdminUserItem>> = withContext(Dispatchers.IO) {
        try {
            val queryParams = mutableListOf<String>()
            if (!search.isNullOrBlank()) queryParams.add("search=${URLEncoder.encode(search, "UTF-8")}")
            if (!role.isNullOrBlank() && role != "ALL") queryParams.add("role=${URLEncoder.encode(role.lowercase(), "UTF-8")}")
            val q = if (queryParams.isNotEmpty()) "?" + queryParams.joinToString("&") else ""

            val conn = openConnection("/users$q", "GET", apiKey)
            val resText = readResponse(conn)
            val json = JSONObject(resText)
            if (conn.responseCode == 200 && json.optBoolean("success", true)) {
                val array = json.optJSONArray("users") ?: JSONArray()
                val list = mutableListOf<AdminUserItem>()
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    list.add(
                        AdminUserItem(
                            id = item.optInt("id"),
                            brandId = item.optInt("brand_id", 1),
                            username = item.optString("username"),
                            email = item.optString("email"),
                            telephone = item.optString("telephone").takeIf { it.isNotBlank() && it != "null" },
                            role = item.optString("role", "user"),
                            balance = item.optDouble("balance", 0.0),
                            spent = item.optDouble("spent", 0.0),
                            currency = item.optString("currency", "USD"),
                            status = item.optString("status", "active"),
                            createdAt = item.optString("created_at")
                        )
                    )
                }
                Result.success(list)
            } else {
                Result.failure(Exception(json.optString("error", "Failed to load users")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserBalance(apiKey: String, userId: Int, action: String, amount: Double, notes: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val conn = openConnection("/users/$userId/balance", "POST", apiKey)
            val payload = JSONObject().apply {
                put("action", action)
                put("amount", amount)
                put("notes", notes)
            }
            sendJson(conn, payload.toString())
            val resText = readResponse(conn)
            val json = JSONObject(resText)
            if (conn.responseCode == 200 && json.optBoolean("success", true)) {
                Result.success(json.optString("message", "Balance updated successfully"))
            } else {
                Result.failure(Exception(json.optString("error", "Balance adjustment failed")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changeUserRole(apiKey: String, userId: Int, newRole: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val conn = openConnection("/users/$userId/role", "POST", apiKey)
            val payload = JSONObject().apply {
                put("role", newRole)
            }
            sendJson(conn, payload.toString())
            val resText = readResponse(conn)
            val json = JSONObject(resText)
            if (conn.responseCode == 200 && json.optBoolean("success", true)) {
                Result.success(json.optString("message", "User role updated successfully"))
            } else {
                Result.failure(Exception(json.optString("error", "Role change failed")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPendingDeposits(apiKey: String): Result<List<PendingDepositItem>> = withContext(Dispatchers.IO) {
        try {
            val conn = openConnection("/deposits/pending", "GET", apiKey)
            val resText = readResponse(conn)
            val json = JSONObject(resText)
            if (conn.responseCode == 200 && json.optBoolean("success", true)) {
                val array = json.optJSONArray("pending_deposits") ?: JSONArray()
                val list = mutableListOf<PendingDepositItem>()
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    list.add(
                        PendingDepositItem(
                            id = item.optInt("id"),
                            userId = item.optInt("user_id"),
                            username = item.optString("username"),
                            email = item.optString("email"),
                            amount = item.optDouble("amount", 0.0),
                            currency = item.optString("currency", "BDT"),
                            paymentMethod = item.optString("payment_method", "manual"),
                            trxId = item.optString("trx_id"),
                            proofImage = item.optString("proof_image").takeIf { it.isNotBlank() && it != "null" },
                            status = item.optString("status", "pending"),
                            adminNotes = item.optString("admin_notes").takeIf { it.isNotBlank() && it != "null" },
                            createdAt = item.optString("created_at")
                        )
                    )
                }
                Result.success(list)
            } else {
                Result.failure(Exception(json.optString("error", "Failed to load pending deposits")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun approveDeposit(apiKey: String, depositId: Int, amount: Double? = null): Result<String> = withContext(Dispatchers.IO) {
        try {
            val conn = openConnection("/deposits/$depositId/approve", "POST", apiKey)
            val payload = JSONObject().apply {
                if (amount != null && amount > 0) put("amount", amount)
            }
            sendJson(conn, payload.toString())
            val resText = readResponse(conn)
            val json = JSONObject(resText)
            if (conn.responseCode == 200 && json.optBoolean("success", true)) {
                Result.success(json.optString("message", "Deposit approved successfully"))
            } else {
                Result.failure(Exception(json.optString("error", "Approval failed")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectDeposit(apiKey: String, depositId: Int, reason: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val conn = openConnection("/deposits/$depositId/reject", "POST", apiKey)
            val payload = JSONObject().apply {
                put("reason", reason)
            }
            sendJson(conn, payload.toString())
            val resText = readResponse(conn)
            val json = JSONObject(resText)
            if (conn.responseCode == 200 && json.optBoolean("success", true)) {
                Result.success(json.optString("message", "Deposit rejected"))
            } else {
                Result.failure(Exception(json.optString("error", "Rejection failed")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGameConfigs(apiKey: String): Result<List<GameConfigItem>> = withContext(Dispatchers.IO) {
        try {
            val conn = openConnection("/game-configs", "GET", apiKey)
            val resText = readResponse(conn)
            val json = JSONObject(resText)
            if (conn.responseCode == 200 && json.optBoolean("success", true)) {
                val array = json.optJSONArray("game_configs") ?: JSONArray()
                val list = mutableListOf<GameConfigItem>()
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    list.add(
                        GameConfigItem(
                            id = item.optInt("id"),
                            gameType = item.optString("game_type"),
                            gamePackage = item.optString("game_package"),
                            gameVersion = item.optString("game_version"),
                            status = item.optString("status", "active"),
                            espEnabled = item.optInt("esp_enabled", 1) == 1,
                            aimbotEnabled = item.optInt("aimbot_enabled", 1) == 1,
                            noticeMessage = item.optString("notice_message").takeIf { it.isNotBlank() && it != "null" },
                            updatedAt = item.optString("updated_at")
                        )
                    )
                }
                Result.success(list)
            } else {
                Result.failure(Exception(json.optString("error", "Failed to load game configs")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateGameConfig(
        apiKey: String,
        configId: Int,
        status: String,
        gameVersion: String,
        espEnabled: Boolean,
        aimbotEnabled: Boolean,
        noticeMessage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val conn = openConnection("/game-configs/$configId", "POST", apiKey)
            val payload = JSONObject().apply {
                put("status", status)
                put("game_version", gameVersion)
                put("esp_enabled", if (espEnabled) 1 else 0)
                put("aimbot_enabled", if (aimbotEnabled) 1 else 0)
                put("notice_message", noticeMessage)
            }
            sendJson(conn, payload.toString())
            val resText = readResponse(conn)
            val json = JSONObject(resText)
            if (conn.responseCode == 200 && json.optBoolean("success", true)) {
                Result.success(json.optString("message", "Game configuration updated"))
            } else {
                Result.failure(Exception(json.optString("error", "Update failed")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAdminPlans(apiKey: String): Result<List<SubscriptionPlanItem>> = withContext(Dispatchers.IO) {
        try {
            val conn = openConnection("/plans", "GET", apiKey)
            val resText = readResponse(conn)
            val json = JSONObject(resText)
            if (conn.responseCode == 200 && json.optBoolean("success", true)) {
                val array = json.optJSONArray("plans") ?: JSONArray()
                val list = mutableListOf<SubscriptionPlanItem>()
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    list.add(
                        SubscriptionPlanItem(
                            id = item.optInt("id"),
                            planCode = item.optString("plan_code"),
                            durationType = item.optString("duration_type"),
                            title = item.optString("title"),
                            description = item.optString("description").takeIf { it.isNotBlank() && it != "null" },
                            durationHours = item.optInt("duration_hours", 24),
                            maxDevices = item.optInt("max_devices", 1),
                            retailPrice = item.optDouble("retail_price", 0.0),
                            resellerPrice = item.optDouble("reseller_price", 0.0),
                            vipResellerPrice = item.optDouble("vip_reseller_price", 0.0),
                            status = item.optString("status", "active"),
                            sortOrder = item.optInt("sort_order", 0)
                        )
                    )
                }
                Result.success(list)
            } else {
                Result.failure(Exception(json.optString("error", "Failed to load plans")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun executeUserAction(apiKey: String, userId: Int, action: String, reason: String = ""): Result<String> = withContext(Dispatchers.IO) {
        try {
            val conn = openConnection("/users/$userId/action", "POST", apiKey)
            val payload = JSONObject().apply {
                put("action", action)
                put("reason", reason)
            }
            sendJson(conn, payload.toString())
            val resText = readResponse(conn)
            val json = JSONObject(resText)
            if (conn.responseCode == 200 && json.optBoolean("success", true)) {
                Result.success(json.optString("message", "User action completed"))
            } else {
                Result.failure(Exception(json.optString("error", "User action failed")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllDeposits(apiKey: String, status: String? = null): Result<List<PendingDepositItem>> = withContext(Dispatchers.IO) {
        try {
            val q = if (!status.isNullOrBlank() && status != "ALL") "?status=${URLEncoder.encode(status.lowercase(), "UTF-8")}" else ""
            val conn = openConnection("/deposits/all$q", "GET", apiKey)
            val resText = readResponse(conn)
            val json = JSONObject(resText)
            if (conn.responseCode == 200 && json.optBoolean("success", true)) {
                val array = json.optJSONArray("deposits") ?: JSONArray()
                val list = mutableListOf<PendingDepositItem>()
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    list.add(
                        PendingDepositItem(
                            id = item.optInt("id"),
                            userId = item.optInt("user_id"),
                            username = item.optString("username"),
                            email = item.optString("email"),
                            amount = item.optDouble("amount", 0.0),
                            currency = item.optString("currency", "BDT"),
                            paymentMethod = item.optString("payment_method", "manual"),
                            trxId = item.optString("trx_id"),
                            proofImage = item.optString("proof_image").takeIf { it.isNotBlank() && it != "null" },
                            status = item.optString("status", "pending"),
                            adminNotes = item.optString("admin_notes").takeIf { it.isNotBlank() && it != "null" },
                            createdAt = item.optString("created_at")
                        )
                    )
                }
                Result.success(list)
            } else {
                Result.failure(Exception(json.optString("error", "Failed to load deposits")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addManualDeposit(
        apiKey: String,
        userId: Int,
        amount: Double,
        paymentMethod: String = "manual_admin",
        notes: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val conn = openConnection("/deposits/manual", "POST", apiKey)
            val payload = JSONObject().apply {
                put("user_id", userId)
                put("amount", amount)
                put("payment_method", paymentMethod)
                put("notes", notes)
            }
            sendJson(conn, payload.toString())
            val resText = readResponse(conn)
            val json = JSONObject(resText)
            if (conn.responseCode in 200..201 && json.optBoolean("success", true)) {
                Result.success(json.optString("message", "Manual deposit credited successfully"))
            } else {
                Result.failure(Exception(json.optString("error", "Manual deposit failed")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSupportTickets(apiKey: String, status: String? = null): Result<List<AdminTicketItem>> = withContext(Dispatchers.IO) {
        try {
            val q = if (!status.isNullOrBlank() && status != "ALL") "?status=${URLEncoder.encode(status.lowercase(), "UTF-8")}" else ""
            val conn = openConnection("/support/tickets$q", "GET", apiKey)
            val resText = readResponse(conn)
            val json = JSONObject(resText)
            if (conn.responseCode == 200 && json.optBoolean("success", true)) {
                val array = json.optJSONArray("tickets") ?: JSONArray()
                val list = mutableListOf<AdminTicketItem>()
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    list.add(
                        AdminTicketItem(
                            id = item.optInt("id"),
                            userId = item.optInt("user_id"),
                            username = item.optString("username"),
                            email = item.optString("email"),
                            subject = item.optString("subject"),
                            department = item.optString("category", item.optString("department", "general")),
                            status = item.optString("status", "open"),
                            priority = item.optString("priority", "medium"),
                            lastReplyAt = item.optString("last_updated").takeIf { it.isNotBlank() && it != "null" } ?: item.optString("last_reply_at").takeIf { it.isNotBlank() && it != "null" },
                            createdAt = item.optString("created_at")
                        )
                    )
                }
                Result.success(list)
            } else {
                Result.failure(Exception(json.optString("error", "Failed to load tickets")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTicketReplies(apiKey: String, ticketId: Int): Result<List<AdminTicketReplyItem>> = withContext(Dispatchers.IO) {
        try {
            val conn = openConnection("/support/tickets/$ticketId", "GET", apiKey)
            val resText = readResponse(conn)
            val json = JSONObject(resText)
            if (conn.responseCode == 200 && json.optBoolean("success", true)) {
                val array = json.optJSONArray("replies") ?: JSONArray()
                val list = mutableListOf<AdminTicketReplyItem>()
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    list.add(
                        AdminTicketReplyItem(
                            id = item.optInt("id"),
                            ticketId = item.optInt("ticket_id"),
                            userId = item.optInt("user_id").takeIf { it > 0 },
                            adminId = item.optInt("admin_id").takeIf { it > 0 },
                            userName = item.optString("sender_name", item.optString("user_name")),
                            adminName = item.optString("admin_name").takeIf { it.isNotBlank() && it != "null" },
                            message = item.optString("message"),
                            attachmentUrl = item.optString("attachment_url").takeIf { it.isNotBlank() && it != "null" },
                            createdAt = item.optString("created_at")
                        )
                    )
                }
                Result.success(list)
            } else {
                Result.failure(Exception(json.optString("error", "Failed to load replies")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun replyTicket(apiKey: String, ticketId: Int, message: String, status: String? = null): Result<String> = withContext(Dispatchers.IO) {
        try {
            val conn = openConnection("/support/tickets/$ticketId/reply", "POST", apiKey)
            val payload = JSONObject().apply {
                put("message", message)
                if (!status.isNullOrBlank()) put("status", status)
            }
            sendJson(conn, payload.toString())
            val resText = readResponse(conn)
            val json = JSONObject(resText)
            if (conn.responseCode in 200..201 && json.optBoolean("success", true)) {
                Result.success(json.optString("message", "Reply submitted"))
            } else {
                Result.failure(Exception(json.optString("error", "Reply failed")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTicketStatus(apiKey: String, ticketId: Int, status: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val conn = openConnection("/support/tickets/$ticketId/status", "POST", apiKey)
            val payload = JSONObject().apply {
                put("status", status)
            }
            sendJson(conn, payload.toString())
            val resText = readResponse(conn)
            val json = JSONObject(resText)
            if (conn.responseCode == 200 && json.optBoolean("success", true)) {
                Result.success(json.optString("message", "Status updated"))
            } else {
                Result.failure(Exception(json.optString("error", "Failed to update status")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadModZip(
        apiKey: String,
        modType: String,
        fileName: String,
        base64Data: String
    ): Result<ModUploadResult> = withContext(Dispatchers.IO) {
        try {
            val conn = openConnection("/mods/upload", "POST", apiKey)
            val payload = JSONObject().apply {
                put("mod_type", modType)
                put("file_name", fileName)
                put("file_base64", base64Data)
            }
            sendJson(conn, payload.toString())
            val resText = readResponse(conn)
            val json = JSONObject(resText)
            if (conn.responseCode == 200 && json.optBoolean("success", true)) {
                Result.success(
                    ModUploadResult(
                        success = true,
                        message = json.optString("message", "Mod uploaded successfully"),
                        modType = json.optString("mod_type", modType),
                        targetFile = json.optString("target_file", fileName),
                        fileSizeBytes = json.optLong("file_size", 0L),
                        verifiedEntriesCount = json.optInt("verified_entries_count", 1)
                    )
                )
            } else {
                Result.failure(Exception(json.optString("error", "Mod upload failed")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendBroadcastNotification(
        apiKey: String,
        title: String,
        message: String,
        type: String = "info",
        imageUrl: String? = null,
        isPopup: Boolean = true,
        targetUserId: Int? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val conn = openConnection("/broadcasts/send", "POST", apiKey)
            val payload = JSONObject().apply {
                put("title", title)
                put("message", message)
                put("type", type)
                if (!imageUrl.isNullOrBlank()) put("image_url", imageUrl)
                put("is_popup", if (isPopup) 1 else 0)
                if (targetUserId != null && targetUserId > 0) put("target_user_id", targetUserId)
            }
            sendJson(conn, payload.toString())
            val resText = readResponse(conn)
            val json = JSONObject(resText)
            if (conn.responseCode in 200..201 && json.optBoolean("success", true)) {
                Result.success(json.optString("message", "Popup notification broadcasted successfully"))
            } else {
                Result.failure(Exception(json.optString("error", "Failed to send broadcast")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAdminTicket(
        apiKey: String,
        targetUserId: Int,
        subject: String,
        message: String,
        priority: String = "medium"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val conn = openConnection("/support/tickets/create", "POST", apiKey)
            val payload = JSONObject().apply {
                put("target_user_id", targetUserId)
                put("subject", subject)
                put("message", message)
                put("priority", priority)
            }
            sendJson(conn, payload.toString())
            val resText = readResponse(conn)
            val json = JSONObject(resText)
            if (conn.responseCode in 200..201 && json.optBoolean("success", true)) {
                Result.success(json.optString("message", "Direct message thread dispatched"))
            } else {
                Result.failure(Exception(json.optString("error", "Failed to create ticket")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
