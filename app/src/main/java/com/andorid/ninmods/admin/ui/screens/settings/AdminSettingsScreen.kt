package com.andorid.ninmods.admin.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andorid.ninmods.admin.data.model.GameConfigItem
import com.andorid.ninmods.admin.data.model.SubscriptionPlanItem
import com.andorid.ninmods.admin.ui.components.AdminTopBar
import com.andorid.ninmods.admin.ui.components.CyberCard
import com.andorid.ninmods.admin.ui.components.StatusBadgePill
import com.andorid.ninmods.admin.ui.theme.*

@Composable
fun AdminSettingsScreen(
    adminUsername: String,
    adminRole: String,
    gameConfigs: List<GameConfigItem>,
    plans: List<SubscriptionPlanItem>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onUpdateGameConfig: (configId: Int, status: String, version: String, esp: Boolean, aimbot: Boolean, notice: String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editingConfig by remember { mutableStateOf<GameConfigItem?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        AdminTopBar(
            title = "SYSTEM CONFIG & PLANS",
            subtitle = "Game Engine & Pricing Infrastructure",
            onRefresh = onRefresh
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Admin Profile Card
            CyberCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SIGNED IN AS",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberPurple,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = adminUsername,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Access Role: ${adminRole.uppercase()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberRed.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Logout, contentDescription = null, tint = CyberRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("LOGOUT", color = CyberRed, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Game Configurations
            Text(
                text = "GAME CONFIGURATIONS",
                style = MaterialTheme.typography.labelSmall,
                color = CyberCyan,
                fontWeight = FontWeight.Bold
            )

            if (gameConfigs.isEmpty()) {
                CyberCard {
                    Text(text = "No game configurations loaded", color = TextSecondary)
                }
            } else {
                gameConfigs.forEach { cfg ->
                    CyberCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = cfg.gameType.uppercase(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusBadgePill(text = cfg.status, status = cfg.status)
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "Version: ${cfg.gameVersion} • Package: ${cfg.gamePackage}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )

                                Text(
                                    text = "ESP: ${if (cfg.espEnabled) "ENABLED" else "DISABLED"} • Aimbot: ${if (cfg.aimbotEnabled) "ENABLED" else "DISABLED"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (cfg.espEnabled && cfg.aimbotEnabled) CyberGreen else CyberOrange
                                )

                                if (!cfg.noticeMessage.isNullOrBlank()) {
                                    Text(
                                        text = "Notice: ${cfg.noticeMessage}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyberCyan
                                    )
                                }
                            }

                            IconButton(onClick = { editingConfig = cfg }) {
                                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit", tint = CyberPurple)
                            }
                        }
                    }
                }
            }

            // Subscription Plans
            Text(
                text = "SUBSCRIPTION PLANS (${plans.size})",
                style = MaterialTheme.typography.labelSmall,
                color = CyberPink,
                fontWeight = FontWeight.Bold
            )

            if (plans.isEmpty()) {
                CyberCard {
                    Text(text = "No subscription plans loaded", color = TextSecondary)
                }
            } else {
                plans.forEach { p ->
                    CyberCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = p.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusBadgePill(text = "${p.durationHours}h", status = "active")
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "Code: ${p.planCode} • Devices: ${p.maxDevices} • Retail: $${p.retailPrice}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )

                                Text(
                                    text = "Reseller: $${p.resellerPrice} • VIP Reseller: $${p.vipResellerPrice}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberGreen
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Edit Game Config Dialog
    editingConfig?.let { target ->
        var versionText by remember { mutableStateOf(target.gameVersion) }
        var statusText by remember { mutableStateOf(target.status) }
        var espEnabled by remember { mutableStateOf(target.espEnabled) }
        var aimbotEnabled by remember { mutableStateOf(target.aimbotEnabled) }
        var noticeText by remember { mutableStateOf(target.noticeMessage ?: "") }

        AlertDialog(
            onDismissRequest = { editingConfig = null },
            containerColor = CyberCardBg,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Edit Config: ${target.gameType.uppercase()}", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = versionText,
                        onValueChange = { versionText = it },
                        label = { Text("Game Version (e.g. 1.132.1)", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberPurple,
                            unfocusedBorderColor = CyberBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = statusText,
                        onValueChange = { statusText = it },
                        label = { Text("Status (active / maintenance)", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberPurple,
                            unfocusedBorderColor = CyberBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ESP Engine Enabled", color = TextPrimary)
                        Switch(
                            checked = espEnabled,
                            onCheckedChange = { espEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyberGreen, checkedTrackColor = CyberGreen.copy(alpha = 0.5f))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Aimbot Engine Enabled", color = TextPrimary)
                        Switch(
                            checked = aimbotEnabled,
                            onCheckedChange = { aimbotEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyberPurple, checkedTrackColor = CyberPurple.copy(alpha = 0.5f))
                        )
                    }

                    OutlinedTextField(
                        value = noticeText,
                        onValueChange = { noticeText = it },
                        label = { Text("Maintenance / Announcement Notice", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberPurple,
                            unfocusedBorderColor = CyberBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateGameConfig(target.id, statusText, versionText, espEnabled, aimbotEnabled, noticeText)
                        editingConfig = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPurple)
                ) {
                    Text("SAVE CHANGES", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingConfig = null }) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }
}
