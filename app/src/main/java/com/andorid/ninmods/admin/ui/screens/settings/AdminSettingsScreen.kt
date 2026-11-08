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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andorid.ninmods.admin.data.model.GameConfigItem
import com.andorid.ninmods.admin.data.model.SubscriptionPlanItem
import com.andorid.ninmods.admin.ui.components.MetricGreen
import com.andorid.ninmods.admin.ui.components.MetricOrange
import com.andorid.ninmods.admin.ui.components.StatusBadgePill
import com.andorid.ninmods.admin.ui.components.SwitchItem
import com.andorid.ninmods.admin.ui.dialogs.EditGameConfigBottomSheet

@Composable
fun AdminSettingsScreen(
    gameConfigs: List<GameConfigItem>,
    plans: List<SubscriptionPlanItem>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onUpdateGameConfig: (configId: Int, status: String, version: String, esp: Boolean, aimbot: Boolean, notice: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingConfig by remember { mutableStateOf<GameConfigItem?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // 1. Target Game Engine Configurations Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Gamepad,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TARGET GAME CONFIGURATIONS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (gameConfigs.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = "No game engines loaded",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        gameConfigs.forEach { cfg ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = cfg.gameType.uppercase(),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Package: ${cfg.gamePackage} • Ver: ${cfg.gameVersion}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        StatusBadgePill(text = cfg.status, status = cfg.status)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // SwitchItems
                                    SwitchItem(
                                        icon = Icons.Filled.Visibility,
                                        title = "ESP Visuals",
                                        summary = "Inject floating skeletal/box overlays",
                                        checked = cfg.espEnabled,
                                        onCheckedChange = { isChecked ->
                                            onUpdateGameConfig(cfg.id, cfg.status, cfg.gameVersion, isChecked, cfg.aimbotEnabled, cfg.noticeMessage ?: "")
                                        }
                                    )

                                    SwitchItem(
                                        icon = Icons.Filled.GpsFixed,
                                        title = "Aim Engine",
                                        summary = "Enable memory touch injection",
                                        checked = cfg.aimbotEnabled,
                                        onCheckedChange = { isChecked ->
                                            onUpdateGameConfig(cfg.id, cfg.status, cfg.gameVersion, cfg.espEnabled, isChecked, cfg.noticeMessage ?: "")
                                        }
                                    )

                                    SwitchItem(
                                        icon = Icons.Filled.Build,
                                        title = "Maintenance Mode",
                                        summary = "Blocks mod execution during game updates",
                                        checked = cfg.status.equals("maintenance", ignoreCase = true),
                                        onCheckedChange = { isChecked ->
                                            val newStatus = if (isChecked) "maintenance" else "online"
                                            onUpdateGameConfig(cfg.id, newStatus, cfg.gameVersion, cfg.espEnabled, cfg.aimbotEnabled, cfg.noticeMessage ?: "")
                                        }
                                    )

                                    if (!cfg.noticeMessage.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Notice: ${cfg.noticeMessage}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MetricOrange
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedButton(
                                        onClick = { editingConfig = cfg },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(imageVector = Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Edit Version & Broadcast Notice")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Pricing & Subscription Plans Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Loyalty,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SUBSCRIPTION TIERS & PRICING",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (plans.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = "No subscription plans loaded",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        plans.forEach { plan ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = plan.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Duration: ${plan.durationHours} Hours (${plan.durationType})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = "\$${String.format("%.2f", plan.retailPrice)}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MetricGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Edit Game Config Bottom Sheet (slides up from bottom)
    editingConfig?.let { cfg ->
        EditGameConfigBottomSheet(
            config = cfg,
            onDismiss = { editingConfig = null },
            onSave = onUpdateGameConfig
        )
    }
}
