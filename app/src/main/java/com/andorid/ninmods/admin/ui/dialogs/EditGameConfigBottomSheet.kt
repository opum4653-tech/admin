package com.andorid.ninmods.admin.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andorid.ninmods.admin.data.model.GameConfigItem

@Composable
fun EditGameConfigBottomSheet(
    config: GameConfigItem,
    onDismiss: () -> Unit,
    onSave: (configId: Int, status: String, version: String, esp: Boolean, aimbot: Boolean, notice: String) -> Unit
) {
    var status by remember { mutableStateOf(config.status) }
    var version by remember { mutableStateOf(config.gameVersion) }
    var espEnabled by remember { mutableStateOf(config.espEnabled) }
    var aimbotEnabled by remember { mutableStateOf(config.aimbotEnabled) }
    var notice by remember { mutableStateOf(config.noticeMessage ?: "") }

    AdminCustomBottomSheet(
        title = "Edit Configuration",
        subtitle = "Game Target: ${config.gameType.uppercase()} (${config.gamePackage})",
        onDismissRequest = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Status Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = status == "active",
                    onClick = { status = "active" },
                    label = { Text("Active / Online") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                FilterChip(
                    selected = status == "maintenance",
                    onClick = { status = "maintenance" },
                    label = { Text("Maintenance") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            OutlinedTextField(
                value = version,
                onValueChange = { version = it },
                label = { Text("Supported Game Version") },
                placeholder = { Text("e.g. 1.109.1") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            // ESP Feature Switch
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ESP / Visuals Global Master", fontWeight = FontWeight.SemiBold)
                    Switch(checked = espEnabled, onCheckedChange = { espEnabled = it })
                }
            }

            // Aimbot Feature Switch
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Aimbot / Memory Global Master", fontWeight = FontWeight.SemiBold)
                    Switch(checked = aimbotEnabled, onCheckedChange = { aimbotEnabled = it })
                }
            }

            OutlinedTextField(
                value = notice,
                onValueChange = { notice = it },
                label = { Text("Update Notice / In-Game Message") },
                placeholder = { Text("e.g. Safe for Ranked matches") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    onSave(config.id, status, version.trim(), espEnabled, aimbotEnabled, notice.trim())
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save Configuration", fontWeight = FontWeight.Bold)
            }
        }
    }
}
