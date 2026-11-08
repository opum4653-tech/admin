package com.andorid.ninmods.admin.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// Standard Metric Palette
val MetricGreen = Color(0xFF81C784)
val MetricRed = Color(0xFFFFB4AB)
val MetricBlue = Color(0xFF9ECAFF)
val MetricOrange = Color(0xFFFFB74D)

// Backward compatibility aliases
val ApatchGreen = MetricGreen
val ApatchRed = MetricRed
val ApatchBlue = MetricBlue
val ApatchOrange = MetricOrange

enum class AdminTab {
    DASHBOARD,
    USERS,
    KEYS,
    SUPPORT,
    DEPOSITS,
    UPLOAD,
    SETTINGS,
    PROFILE
}

@Composable
fun StatusBadgePill(
    text: String,
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, fgColor) = when (status.uppercase()) {
        "ACTIVE", "APPROVED", "COMPLETED", "ONLINE", "SUCCESS" -> Pair(
            MetricGreen.copy(alpha = 0.16f),
            MetricGreen
        )
        "UNUSED", "PENDING", "PROCESSING" -> Pair(
            MetricBlue.copy(alpha = 0.16f),
            MetricBlue
        )
        "EXPIRED", "MAINTENANCE" -> Pair(
            MetricOrange.copy(alpha = 0.16f),
            MetricOrange
        )
        "BANNED", "REJECTED", "OFFLINE", "ERROR" -> Pair(
            MetricRed.copy(alpha = 0.16f),
            MetricRed
        )
        else -> Pair(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fgColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/**
 * Standard Responsive Content Box.
 * Automatically constrains maximum content width on wide screens (tablets, foldables, landscape)
 * and centers it symmetrically, while expanding to full width on phones.
 */
@Composable
fun ResponsiveContentBox(
    modifier: Modifier = Modifier,
    maxWidth: androidx.compose.ui.unit.Dp = 760.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = maxWidth),
            content = content
        )
    }
}

/**
 * Standard Uniform Metric Card for Dashboard.
 * All 8 cards across the dashboard use this identical design, eliminating height mismatches and font overflow.
 */
@Composable
fun UniformMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val cardModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .height(112.dp)
            .clickable(onClick = onClick)
    } else {
        modifier
            .fillMaxWidth()
            .height(112.dp)
    }

    ElevatedCard(
        modifier = cardModifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(26.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = iconColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Official Loading Button with integrated spinner.
 * Replaces button contents with an official CircularProgressIndicator when active.
 */
@Composable
fun LoadingButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors()
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = colors
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
