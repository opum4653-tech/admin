package com.andorid.ninmods.admin.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andorid.ninmods.admin.data.model.DashboardStats
import com.andorid.ninmods.admin.ui.components.AdminStatCard
import com.andorid.ninmods.admin.ui.components.AdminTopBar
import com.andorid.ninmods.admin.ui.components.CyberCard
import com.andorid.ninmods.admin.ui.theme.*

@Composable
fun AdminDashboardScreen(
    stats: DashboardStats?,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        AdminTopBar(
            title = "NINMODS OVERVIEW",
            subtitle = "Live Cloud Operations & Telemetry",
            onRefresh = onRefresh
        )

        if (isLoading && stats == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator(color = CyberPurple)
            }
        } else {
            val s = stats ?: DashboardStats()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Key Management Metrics
                Text(
                    text = "LICENSE KEYS TELEMETRY",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberPurple,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminStatCard(
                        title = "Active Keys",
                        value = "${s.activeKeys}",
                        subtitle = "${s.totalKeys} total in DB",
                        icon = Icons.Filled.CheckCircle,
                        accentColor = CyberGreen,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        title = "Unused Stock",
                        value = "${s.unusedKeys}",
                        subtitle = "Ready to assign",
                        icon = Icons.Filled.VpnKey,
                        accentColor = CyberCyan,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminStatCard(
                        title = "Expired Keys",
                        value = "${s.expiredKeys}",
                        subtitle = "Completed duration",
                        icon = Icons.Filled.History,
                        accentColor = CyberOrange,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        title = "Banned Keys",
                        value = "${s.bannedKeys}",
                        subtitle = "HWID / Security Lock",
                        icon = Icons.Filled.Block,
                        accentColor = CyberRed,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Finance & Pending Approvals
                Text(
                    text = "FINANCIAL & DEPOSIT QUEUE",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberPink,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminStatCard(
                        title = "Pending Deposits",
                        value = "${s.pendingDeposits}",
                        subtitle = "$${String.format("%.2f", s.pendingDepositAmount)} awaiting check",
                        icon = Icons.Filled.PendingActions,
                        accentColor = if (s.pendingDeposits > 0) CyberOrange else CyberGreen,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        title = "User Wallets",
                        value = "$${String.format("%.2f", s.totalUserBalance)}",
                        subtitle = "Combined USD balance",
                        icon = Icons.Filled.AccountBalanceWallet,
                        accentColor = CyberPurple,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // User Registry
                Text(
                    text = "COMMUNITY & ACCOUNTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold
                )

                CyberCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total Users", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(text = "${s.totalUsers}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column {
                            Text(text = "Resellers", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(text = "${s.totalResellers}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = CyberCyan)
                        }
                        Column {
                            Text(text = "VIP Resellers", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(text = "${s.totalVipResellers}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = CyberPink)
                        }
                    }
                }

                // Support & Diagnostics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminStatCard(
                        title = "Open Tickets",
                        value = "${s.openTickets}",
                        icon = Icons.Filled.SupportAgent,
                        accentColor = CyberPurple,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        title = "Bug Reports",
                        value = "${s.totalBugReports}",
                        icon = Icons.Filled.BugReport,
                        accentColor = CyberCyan,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
