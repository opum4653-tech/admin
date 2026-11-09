package com.andorid.ninmods.admin.ui.screens.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andorid.ninmods.admin.data.model.DashboardStats
import com.andorid.ninmods.admin.ui.components.UniformMetricCard
import java.util.Locale

private val ColorSuccess = Color(0xFF81C784)
private val ColorWarning = Color(0xFFFFB74D)
private val ColorError = Color(0xFFFFB4AB)
private val ColorInfo = Color(0xFF9ECAFF)
private val ColorBanned = Color(0xFFEF5350)
private val ColorVip = Color(0xFFCE93D8)

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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        // ══════════════════════════════════════════════════════════
        // 1. TOP METRIC TILES (Authentic High-Level Telemetry)
        // ══════════════════════════════════════════════════════════
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UniformMetricCard(
                title = "User Wallets",
                value = "\$${String.format(Locale.US, "%.2f", s.totalUserBalance)}",
                icon = Icons.Filled.AccountBalanceWallet,
                iconColor = ColorWarning,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToPage(4) } // Deposits page
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

        // ══════════════════════════════════════════════════════════
        // 2. REAL REPORT GRAPH: LICENSE INVENTORY DISTRIBUTION
        // ══════════════════════════════════════════════════════════
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "License Inventory Health",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val totalInventory = s.totalKeys.coerceAtLeast(s.activeKeys + s.unusedKeys + s.expiredKeys + s.bannedKeys)
                        Text(
                            text = "Real-time stock ratio across $totalInventory total keys",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = onOpenBulkGenerate,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Stock", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }

                // Proportional Multi-Segment Bar Chart
                val total = (s.activeKeys + s.unusedKeys + s.expiredKeys + s.bannedKeys).coerceAtLeast(1)
                val activeWeight = (s.activeKeys.toFloat() / total).coerceAtLeast(0.01f)
                val unusedWeight = (s.unusedKeys.toFloat() / total).coerceAtLeast(0.01f)
                val expiredWeight = (s.expiredKeys.toFloat() / total).coerceAtLeast(0.01f)
                val bannedWeight = (s.bannedKeys.toFloat() / total).coerceAtLeast(0.01f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    if (s.activeKeys > 0) {
                        Box(
                            modifier = Modifier
                                .weight(activeWeight)
                                .fillMaxHeight()
                                .background(ColorSuccess)
                        )
                    }
                    if (s.unusedKeys > 0) {
                        Box(
                            modifier = Modifier
                                .weight(unusedWeight)
                                .fillMaxHeight()
                                .background(ColorInfo)
                        )
                    }
                    if (s.expiredKeys > 0) {
                        Box(
                            modifier = Modifier
                                .weight(expiredWeight)
                                .fillMaxHeight()
                                .background(ColorWarning)
                        )
                    }
                    if (s.bannedKeys > 0) {
                        Box(
                            modifier = Modifier
                                .weight(bannedWeight)
                                .fillMaxHeight()
                                .background(ColorBanned)
                        )
                    }
                    if (s.activeKeys == 0 && s.unusedKeys == 0 && s.expiredKeys == 0 && s.bannedKeys == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        )
                    }
                }

                // Detailed Key Telemetry Legend Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LegendMetricPill(
                        label = "Active",
                        count = s.activeKeys,
                        percentage = if (total > 0 && (s.activeKeys + s.unusedKeys + s.expiredKeys + s.bannedKeys) > 0) ((s.activeKeys.toFloat() / total) * 100).toInt() else 0,
                        color = ColorSuccess
                    )
                    LegendMetricPill(
                        label = "Unused",
                        count = s.unusedKeys,
                        percentage = if (total > 0 && (s.activeKeys + s.unusedKeys + s.expiredKeys + s.bannedKeys) > 0) ((s.unusedKeys.toFloat() / total) * 100).toInt() else 0,
                        color = ColorInfo
                    )
                    LegendMetricPill(
                        label = "Expired",
                        count = s.expiredKeys,
                        percentage = if (total > 0 && (s.activeKeys + s.unusedKeys + s.expiredKeys + s.bannedKeys) > 0) ((s.expiredKeys.toFloat() / total) * 100).toInt() else 0,
                        color = ColorWarning
                    )
                    LegendMetricPill(
                        label = "Banned",
                        count = s.bannedKeys,
                        percentage = if (total > 0 && (s.activeKeys + s.unusedKeys + s.expiredKeys + s.bannedKeys) > 0) ((s.bannedKeys.toFloat() / total) * 100).toInt() else 0,
                        color = ColorBanned
                    )
                }
            }
        }

        // ══════════════════════════════════════════════════════════
        // 3. REAL REPORT GRAPH: FINANCIAL LIQUIDITY & DEPOSITS
        // ══════════════════════════════════════════════════════════
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Financial & Settlement Ledger",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Total active balances vs pending deposit transactions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Filled.AccountBalance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Balance Comparison Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "User Wallets (Settled)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "\$${String.format(Locale.US, "%.2f", s.totalUserBalance)}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Pending Deposits",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "\$${String.format(Locale.US, "%.2f", s.pendingDepositAmount)} (${s.pendingDeposits} req)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (s.pendingDeposits > 0) ColorWarning else ColorSuccess
                            )
                        )
                    }
                }

                // Visual Settlement Bar Chart
                val totalMoney = (s.totalUserBalance + s.pendingDepositAmount).coerceAtLeast(1.0)
                val settledWeight = (s.totalUserBalance / totalMoney).toFloat().coerceIn(0.05f, 0.95f)
                val pendingWeight = (1f - settledWeight).coerceIn(0.05f, 0.95f)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(if (s.totalUserBalance > 0) settledWeight else 0.5f)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        if (s.pendingDeposits > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(pendingWeight)
                                    .fillMaxHeight()
                                    .background(ColorWarning)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Circulating Balance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (s.pendingDeposits > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(ColorWarning, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Awaiting Approval", style = MaterialTheme.typography.labelSmall, color = ColorWarning)
                            }
                        }
                    }
                }

                // Interactive Status Action Strip
                Surface(
                    onClick = { onNavigateToPage(4) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (s.pendingDeposits > 0) ColorWarning.copy(alpha = 0.15f) else ColorSuccess.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (s.pendingDeposits > 0) Icons.Filled.WarningAmber else Icons.Filled.CheckCircleOutline,
                                contentDescription = null,
                                tint = if (s.pendingDeposits > 0) ColorWarning else ColorSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (s.pendingDeposits > 0) "${s.pendingDeposits} manual deposit(s) need review" else "All deposit queues 100% verified",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (s.pendingDeposits > 0) ColorWarning else ColorSuccess
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        // ══════════════════════════════════════════════════════════
        // 4. USER TIER BREAKDOWN GRAPH
        // ══════════════════════════════════════════════════════════
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "User Accounts & Role Distribution",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Client tiers across regular users, resellers, and VIPs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { onNavigateToPage(1) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Open Users",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val standardUsers = (s.totalUsers - s.totalResellers - s.totalVipResellers).coerceAtLeast(0)
                val totalUserSum = s.totalUsers.coerceAtLeast(1)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    if (standardUsers > 0) {
                        Box(
                            modifier = Modifier
                                .weight((standardUsers.toFloat() / totalUserSum).coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(ColorInfo)
                        )
                    }
                    if (s.totalResellers > 0) {
                        Box(
                            modifier = Modifier
                                .weight((s.totalResellers.toFloat() / totalUserSum).coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(ColorWarning)
                        )
                    }
                    if (s.totalVipResellers > 0) {
                        Box(
                            modifier = Modifier
                                .weight((s.totalVipResellers.toFloat() / totalUserSum).coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(ColorVip)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LegendMetricPill(
                        label = "Clients",
                        count = standardUsers,
                        percentage = if (s.totalUsers > 0) ((standardUsers.toFloat() / s.totalUsers) * 100).toInt() else 0,
                        color = ColorInfo
                    )
                    LegendMetricPill(
                        label = "Resellers",
                        count = s.totalResellers,
                        percentage = if (s.totalUsers > 0) ((s.totalResellers.toFloat() / s.totalUsers) * 100).toInt() else 0,
                        color = ColorWarning
                    )
                    LegendMetricPill(
                        label = "VIP",
                        count = s.totalVipResellers,
                        percentage = if (s.totalUsers > 0) ((s.totalVipResellers.toFloat() / s.totalUsers) * 100).toInt() else 0,
                        color = ColorVip
                    )
                }
            }
        }

        // ══════════════════════════════════════════════════════════
        // 5. LOWER METRIC TILES: SUPPORT & BUGS
        // ══════════════════════════════════════════════════════════
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

@Composable
private fun LegendMetricPill(
    label: String,
    count: Int,
    percentage: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "$count ($percentage%)",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
