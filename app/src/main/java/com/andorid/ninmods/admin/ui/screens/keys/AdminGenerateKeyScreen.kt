package com.andorid.ninmods.admin.ui.screens.keys

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.andorid.ninmods.admin.data.model.AdminUserItem
import com.andorid.ninmods.admin.ui.components.LoadingButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminGenerateKeyScreen(
    users: List<AdminUserItem>,
    onGenerateKeys: (quantity: Int, durationHours: Int, durationType: String, gameType: String, maxDevices: Int, notes: String, targetUserId: Int?, onComplete: (Boolean, String) -> Unit) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var quantity by remember { mutableIntStateOf(1) }
    var selectedPresetHours by remember { mutableIntStateOf(24) }
    var customHoursText by remember { mutableStateOf("24") }
    var selectedDurationType by remember { mutableStateOf("standard") }
    var selectedGameType by remember { mutableStateOf("all") }
    var maxDevices by remember { mutableIntStateOf(1) }
    var notes by remember { mutableStateOf("") }

    // User Assignment Selector
    var selectedUser by remember { mutableStateOf<AdminUserItem?>(null) }
    var userSearchQuery by remember { mutableStateOf("") }
    var isUserDropdownExpanded by remember { mutableStateOf(false) }

    var isSubmitting by remember { mutableStateOf(false) }

    val filteredUsers = remember(users, userSearchQuery) {
        if (userSearchQuery.isBlank()) users.take(15)
        else users.filter {
            it.username.contains(userSearchQuery, ignoreCase = true) ||
            it.email.contains(userSearchQuery, ignoreCase = true) ||
            it.id.toString() == userSearchQuery.trim()
        }.take(15)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // CARD 1: USER ASSIGNMENT (TARGET USER)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ASSIGN TO USER ACCOUNT",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // User Selector Button
                Surface(
                    onClick = { isUserDropdownExpanded = !isUserDropdownExpanded },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
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
                                text = if (selectedUser != null) selectedUser!!.username else "Public / Unassigned Stock",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (selectedUser != null) "User ID: #${selectedUser!!.id} • Role: ${selectedUser!!.role}" else "Key will be unassigned in inventory",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = if (isUserDropdownExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Expandable User Search and Selection List
                if (isUserDropdownExpanded) {
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = userSearchQuery,
                        onValueChange = { userSearchQuery = it },
                        placeholder = { Text("Search users by name, email, ID...", style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (userSearchQuery.isNotBlank()) {
                                IconButton(onClick = { userSearchQuery = "" }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Unassigned Option
                        Surface(
                            onClick = {
                                selectedUser = null
                                isUserDropdownExpanded = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedUser == null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "None (Public Unassigned Stock)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selectedUser == null) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedUser == null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }

                        // Filtered Users List
                        filteredUsers.forEach { u ->
                            val isChosen = selectedUser?.id == u.id
                            Surface(
                                onClick = {
                                    selectedUser = u
                                    isUserDropdownExpanded = false
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isChosen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = u.username,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (isChosen) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "#${u.id} • ${u.email}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = u.role.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // CARD 2: QUANTITY & DURATION
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Numbers,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "QUANTITY & EXPIRATION",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quantity Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Keys Count (1 - 100):",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalIconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = "Decrease")
                        }

                        Text(
                            text = "$quantity",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        FilledTonalIconButton(
                            onClick = { if (quantity < 100) quantity++ },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Increase")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(14.dp))

                // Duration Presets
                Text(
                    text = "Duration Preset:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                val presets = listOf(
                    Pair("2 Hours", 2),
                    Pair("24 Hours", 24),
                    Pair("7 Days", 168),
                    Pair("30 Days", 720),
                    Pair("365 Days", 8760),
                    Pair("Custom", -1)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { (label, hrs) ->
                        val isSelected = selectedPresetHours == hrs
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedPresetHours = hrs
                                if (hrs != -1) customHoursText = hrs.toString()
                            },
                            label = { Text(label) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                if (selectedPresetHours == -1) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customHoursText,
                        onValueChange = { customHoursText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Custom Duration in Hours") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // CARD 3: TARGET ENGINE & RESTRICTIONS
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GAME ENGINE & DEVICE LIMITS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Game Type
                Text(text = "Target Game Engine:", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val gameOptions = listOf(
                        Pair("all", "All Games"),
                        Pair("freefire", "Free Fire"),
                        Pair("freefiremax", "FF MAX")
                    )
                    gameOptions.forEach { (type, label) ->
                        val isSelected = selectedGameType == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedGameType = type },
                            label = { Text(label, maxLines = 1) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Max Devices
                Text(text = "Max Devices Per Key:", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 2, 3).forEach { count ->
                        val isSelected = maxDevices == count
                        FilterChip(
                            selected = isSelected,
                            onClick = { maxDevices = count },
                            label = { Text("$count Device${if (count > 1) "s" else ""}", maxLines = 1) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Reference Notes (Optional)") },
                    placeholder = { Text("e.g. VIP Tournament / Custom Batch") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // SUBMIT BUTTON WITH SPINNER
        LoadingButton(
            text = "GENERATE $quantity KEY${if (quantity > 1) "S" else ""}",
            isLoading = isSubmitting,
            onClick = {
                val finalHours = if (selectedPresetHours == -1) {
                    customHoursText.toIntOrNull() ?: 24
                } else {
                    selectedPresetHours
                }

                isSubmitting = true
                onGenerateKeys(
                    quantity,
                    finalHours,
                    selectedDurationType,
                    selectedGameType,
                    maxDevices,
                    notes.ifBlank { "Batch generated" },
                    selectedUser?.id
                ) { success, msg ->
                    isSubmitting = false
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    if (success) {
                        onNavigateBack()
                    }
                }
            },
            icon = Icons.Filled.Key,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}
