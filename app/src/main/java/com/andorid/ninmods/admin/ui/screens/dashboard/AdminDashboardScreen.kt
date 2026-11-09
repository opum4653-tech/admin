package com.andorid.ninmods.admin.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andorid.ninmods.admin.data.model.DashboardStats
import com.andorid.ninmods.admin.ui.components.UniformMetricCard

private val ColorSuccess = Color(0xFF81C784)
private val ColorWarning = Color(0xFFFFB74D)
private val ColorError = Color(0xFFFFB4AB)
private val ColorInfo = Color(0xFF9ECAFF)

@Composable
fun AdminDashboardScreen(
    stats: DashboardStats?,
    isLoading: Boolean,
    onNavigateToPage: (pageIndex: Int) -> Unit,
    onOpenBulkGenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val s = stats ?: DashboardStats()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // ROW 1: Active Keys & Total Users (Standard Uniform Cards)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UniformMetricCard(
                title = "Active Keys",
                value = "${s.activeKeys}",
                icon = Icons.Filled.CheckCircle,
                iconColor = ColorSuccess,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToPage(2) } // Keys page
            )

            UniformMetricCard(
                title = "Total Users",
                value = "${s.totalUsers}",
                icon = Icons.Filled.People,
                iconColor = ColorInfo,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToPage(1) } // Users page
            )
        }

        // ROW 2: User Wallets & Deposits Queue (Standard Uniform Cards)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UniformMetricCard(
                title = "User Wallets",
                value = "\$${String.format("%.2f", s.totalUserBalance)}",
                icon = Icons.Filled.AccountBalanceWallet,
                iconColor = ColorWarning,
                modifier = Modifier.weight(1f)
            )

            UniformMetricCard(
                title = if (s.pendingDeposits > 0) "Pending Deposits" else "Deposits Clear",
                value = "${s.pendingDeposits}",
                icon = if (s.pendingDeposits > 0) Icons.Filled.PendingActions else Icons.Filled.Verified,
                iconColor = if (s.pendingDeposits > 0) ColorError else ColorSuccess,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToPage(4) } // Deposits page
            )
        }

        // ROW 3: Unused Stock & Expired Keys (Standard Uniform Cards)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UniformMetricCard(
                title = "Unused Stock",
                value = "${s.unusedKeys}",
                icon = Icons.Filled.VpnKey,
                iconColor = ColorSuccess,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToPage(2) }
            )

            UniformMetricCard(
                title = "Expired Keys",
                value = "${s.expiredKeys}",
                icon = Icons.Filled.History,
                iconColor = ColorWarning,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToPage(2) }
            )
        }

        // ROW 4: Open Support Tickets & Bug Reports
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UniformMetricCard(
                title = "Open Tickets",
                value = "${s.openTickets}",
                icon = Icons.Filled.QuestionAnswer,
                iconColor = if (s.openTickets > 0) ColorWarning else ColorInfo,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToPage(3) } // Support page
            )

            UniformMetricCard(
                title = "Bug Reports",
                value = "${s.totalBugReports}",
                icon = Icons.Filled.BugReport,
                iconColor = if (s.totalBugReports > 0) ColorError else ColorSuccess,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToPage(8) } // Bug Reports page
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
