package com.andorid.ninmods.admin.ui.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.andorid.ninmods.admin.R
import com.andorid.ninmods.admin.data.model.DashboardStats

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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // SECTION 1: PRIMARY TELEMETRY (Standardized Uniform Cards - Row 1)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StandardMetricCard(
                title = "Active Keys",
                value = "${s.activeKeys}",
                statusText = "In Production",
                icon = Icons.Filled.CheckCircle,
                iconColor = ColorSuccess,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToPage(2) } // Keys page
            )

            StandardMetricCard(
                title = "Total Users",
                value = "${s.totalUsers}",
                statusText = "Accounts in DB",
                icon = Icons.Filled.People,
                iconColor = ColorInfo,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToPage(1) } // Users page
            )
        }

        // SECTION 2: PRIMARY TELEMETRY (Standardized Uniform Cards - Row 2)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StandardMetricCard(
                title = "User Wallets",
                value = "\$${String.format("%.2f", s.totalUserBalance)}",
                statusText = "Total Balance",
                icon = Icons.Filled.AccountBalanceWallet,
                iconColor = ColorWarning,
                modifier = Modifier.weight(1f)
            )

            StandardMetricCard(
                title = "Deposits Queue",
                value = "${s.pendingDeposits}",
                statusText = if (s.pendingDeposits > 0) "Awaiting Review" else "Queue Clear",
                icon = if (s.pendingDeposits > 0) Icons.Filled.PendingActions else Icons.Filled.Verified,
                iconColor = if (s.pendingDeposits > 0) ColorError else ColorSuccess,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToPage(4) } // Deposits page
            )
        }

        // SECTION 3: SECONDARY METRICS (Standardized Uniform Cards - Row 3)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StandardCompactMetricCard(
                title = "Unused Stock",
                value = "${s.unusedKeys}",
                icon = Icons.Filled.VpnKey,
                iconColor = ColorSuccess,
                modifier = Modifier.weight(1f)
            )

            StandardCompactMetricCard(
                title = "Expired Keys",
                value = "${s.expiredKeys}",
                icon = Icons.Filled.History,
                iconColor = ColorWarning,
                modifier = Modifier.weight(1f)
            )
        }

        // SECTION 4: SECONDARY METRICS (Standardized Uniform Cards - Row 4)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StandardCompactMetricCard(
                title = "Banned Keys",
                value = "${s.bannedKeys}",
                icon = Icons.Filled.Block,
                iconColor = ColorError,
                modifier = Modifier.weight(1f)
            )

            StandardCompactMetricCard(
                title = "Open Tickets",
                value = "${s.openTickets}",
                icon = Icons.Filled.QuestionAnswer,
                iconColor = ColorInfo,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToPage(3) } // Support page
            )
        }

        // SECTION 5: QUICK ACTIONS (Standardized Action Buttons)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onOpenBulkGenerate,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generate Keys",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { onNavigateToPage(4) }, // Deposits
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (s.pendingDeposits > 0) ColorError else MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = if (s.pendingDeposits > 0) Color(0xFF410002) else MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (s.pendingDeposits > 0) "Review (${s.pendingDeposits})" else "Deposits",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

/**
 * Standardized Telemetry Card with strict equal height and symmetrical content alignment.
 */
@Composable
private fun StandardMetricCard(
    title: String,
    value: String,
    statusText: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 14.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = iconColor,
                maxLines = 1
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

/**
 * Standardized Compact Metric Card with strict equal height for secondary metrics.
 */
@Composable
private fun StandardCompactMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = iconColor
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconColor.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
