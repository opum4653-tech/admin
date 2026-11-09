package com.andorid.ninmods.admin.data.model

data class AdminProfile(
    val adminId: Int,
    val username: String,
    val email: String,
    val role: String,
    val apiKey: String
)

data class DashboardStats(
    val totalUsers: Int = 0,
    val totalResellers: Int = 0,
    val totalVipResellers: Int = 0,
    val totalUserBalance: Double = 0.0,
    val totalKeys: Int = 0,
    val activeKeys: Int = 0,
    val unusedKeys: Int = 0,
    val expiredKeys: Int = 0,
    val bannedKeys: Int = 0,
    val pendingDeposits: Int = 0,
    val pendingDepositAmount: Double = 0.0,
    val openTickets: Int = 0,
    val totalBugReports: Int = 0
)

data class AdminUserItem(
    val id: Int,
    val brandId: Int,
    val username: String,
    val email: String,
    val telephone: String?,
    val role: String,
    val balance: Double,
    val spent: Double,
    val currency: String,
    val status: String,
    val createdAt: String
)

data class AdminLicenseKeyItem(
    val id: Int,
    val userId: Int? = null,
    val licenseKey: String,
    val gameType: String,
    val durationType: String,
    val durationHours: Int,
    val status: String,
    val hwid: String?,
    val deviceModel: String?,
    val maxDevices: Int,
    val maxResets: Int,
    val resetCount: Int,
    val expiresAt: String?,
    val notes: String?,
    val createdAt: String?
)

data class PendingDepositItem(
    val id: Int,
    val userId: Int,
    val username: String,
    val email: String,
    val amount: Double,
    val currency: String,
    val paymentMethod: String,
    val trxId: String,
    val proofImage: String?,
    val status: String,
    val adminNotes: String?,
    val createdAt: String
)

data class GameConfigItem(
    val id: Int,
    val gameType: String,
    val gamePackage: String,
    val gameVersion: String,
    val status: String,
    val espEnabled: Boolean,
    val aimbotEnabled: Boolean,
    val noticeMessage: String?,
    val updatedAt: String?
)

data class SubscriptionPlanItem(
    val id: Int,
    val planCode: String,
    val durationType: String,
    val title: String,
    val description: String?,
    val durationHours: Int,
    val maxDevices: Int,
    val retailPrice: Double,
    val resellerPrice: Double,
    val vipResellerPrice: Double,
    val status: String,
    val sortOrder: Int
)

data class AdminTicketItem(
    val id: Int,
    val userId: Int,
    val username: String,
    val email: String,
    val subject: String,
    val department: String,
    val status: String,
    val priority: String,
    val lastReplyAt: String?,
    val createdAt: String
)

data class AdminTicketReplyItem(
    val id: Int,
    val ticketId: Int,
    val userId: Int?,
    val adminId: Int?,
    val userName: String?,
    val adminName: String?,
    val message: String,
    val attachmentUrl: String?,
    val createdAt: String
)

data class ModUploadResult(
    val success: Boolean,
    val message: String,
    val modType: String,
    val targetFile: String,
    val fileSizeBytes: Long,
    val verifiedEntriesCount: Int
)

data class AdminBugItem(
    val id: Int,
    val userId: Int?,
    val username: String?,
    val licenseKey: String?,
    val hwid: String?,
    val deviceModel: String?,
    val androidVersion: String?,
    val appVersion: String?,
    val kernelVersion: String?,
    val isRoot: Boolean,
    val title: String,
    val description: String?,
    val logcatTrace: String?,
    val status: String,
    val adminNotes: String?,
    val createdAt: String
)

data class AdminBroadcastItem(
    val id: Int,
    val brandId: Int = 1,
    val title: String,
    val message: String,
    val type: String = "info",
    val imageUrl: String? = null,
    val isPopup: Boolean = true,
    val targetUserId: Int? = null,
    val status: String = "active",
    val createdAt: String? = null
)
