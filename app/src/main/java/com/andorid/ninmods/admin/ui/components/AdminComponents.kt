package com.andorid.ninmods.admin.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andorid.ninmods.admin.R
import com.andorid.ninmods.admin.ui.theme.*

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    borderColor: Color = CyberBorder,
    backgroundColor: Color = CyberCardBg,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Surface(
        modifier = modifier
            .border(BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = CyberCardBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun StatusBadgePill(
    text: String,
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "active", "approved", "online" -> Pair(CyberGreen.copy(alpha = 0.15f), CyberGreen)
        "unused", "pending" -> Pair(CyberOrange.copy(alpha = 0.15f), CyberOrange)
        "banned", "rejected", "expired" -> Pair(CyberRed.copy(alpha = 0.15f), CyberRed)
        else -> Pair(CyberPurple.copy(alpha = 0.15f), CyberPurple)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = text.uppercase(),
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun AdminTopBar(
    title: String,
    subtitle: String? = null,
    onRefresh: (() -> Unit)? = null,
    onActionClick: (() -> Unit)? = null,
    actionIcon: ImageVector? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CyberDarkBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_n),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            if (onRefresh != null) {
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh",
                        tint = CyberPurple
                    )
                }
            }

            if (onActionClick != null && actionIcon != null) {
                IconButton(onClick = onActionClick) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = "Action",
                        tint = CyberPink
                    )
                }
            }
        }
    }
}

enum class AdminTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Overview", Icons.Filled.Dashboard),
    KEYS("Keys", Icons.Filled.VpnKey),
    USERS("Users", Icons.Filled.People),
    DEPOSITS("Deposits", Icons.Filled.AccountBalanceWallet),
    SETTINGS("Settings", Icons.Filled.Tune)
}

@Composable
fun AdminFloatingBottomNav(
    currentTab: AdminTab,
    onTabSelected: (AdminTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .border(BorderStroke(1.dp, CyberBorder), RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = CyberCardBg.copy(alpha = 0.95f),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AdminTab.values().forEach { tab ->
                val selected = currentTab == tab
                val iconTint by animateColorAsState(
                    targetValue = if (selected) CyberPurple else TextSecondary,
                    label = "iconTint"
                )
                val bgTint by animateColorAsState(
                    targetValue = if (selected) CyberPurple.copy(alpha = 0.20f) else Color.Transparent,
                    label = "bgTint"
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = bgTint,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onTabSelected(tab) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                        if (selected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyberPurple
                            )
                        }
                    }
                }
            }
        }
    }
}
