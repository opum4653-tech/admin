package com.andorid.ninmods.admin.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

val ApatchGreen = Color(0xFF81C784)
val ApatchRed = Color(0xFFFFB4AB)
val ApatchBlue = Color(0xFF9ECAFF)
val ApatchOrange = Color(0xFFFFB74D)

enum class AdminTab {
    DASHBOARD,
    KEYS,
    USERS,
    DEPOSITS,
    SETTINGS
}

@Composable
fun StatusBadgePill(
    text: String,
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, fgColor) = when (status.uppercase()) {
        "ACTIVE", "APPROVED", "COMPLETED", "ONLINE", "SUCCESS" -> Pair(
            ApatchGreen.copy(alpha = 0.16f),
            ApatchGreen
        )
        "UNUSED", "PENDING", "PROCESSING" -> Pair(
            ApatchBlue.copy(alpha = 0.16f),
            ApatchBlue
        )
        "EXPIRED", "MAINTENANCE" -> Pair(
            ApatchOrange.copy(alpha = 0.16f),
            ApatchOrange
        )
        "BANNED", "REJECTED", "OFFLINE", "ERROR" -> Pair(
            ApatchRed.copy(alpha = 0.16f),
            ApatchRed
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
