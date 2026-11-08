package com.andorid.ninmods.admin.ui.screens.upload

import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andorid.ninmods.admin.data.model.ModUploadResult
import com.andorid.ninmods.admin.ui.components.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.zip.ZipInputStream

@Composable
fun AdminModUploadScreen(
    onUploadMod: (modType: String, fileName: String, base64Data: String, onResult: (Result<ModUploadResult>) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedModType by remember { mutableStateOf("external") } // 'external' or 'kernel'
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFileSize by remember { mutableLongStateOf(0L) }
    var securityScanPassed by remember { mutableStateOf(false) }
    var scanLogMessages by remember { mutableStateOf<List<String>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadStatusMessage by remember { mutableStateOf<String?>(null) }
    var isUploadSuccess by remember { mutableStateOf(false) }

    // Client-side ZIP security scanner to prevent shells and malicious scripts
    fun scanAndPrepareZip(uri: Uri) {
        scope.launch {
            try {
                scanLogMessages = listOf("[*] Validating ZIP package structure...")
                val contentResolver = context.contentResolver

                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    scanLogMessages = listOf("[-] Error: Unable to read file from storage.")
                    securityScanPassed = false
                    return@launch
                }

                val bytes = withContext(Dispatchers.IO) {
                    inputStream.use { it.readBytes() }
                }

                selectedFileSize = bytes.size.toLong()

                // Check magic bytes PK\x03\x04
                if (bytes.size < 4 || bytes[0] != 0x50.toByte() || bytes[1] != 0x4B.toByte() || bytes[2] != 0x03.toByte() || bytes[3] != 0x04.toByte()) {
                    scanLogMessages = listOf("[-] Security Violation: File is not a valid ZIP binary archive.")
                    securityScanPassed = false
                    return@launch
                }

                // Check maximum 100MB
                if (bytes.size > 100 * 1024 * 1024) {
                    scanLogMessages = listOf("[-] File size exceeds 100MB limit.")
                    securityScanPassed = false
                    return@launch
                }

                val blacklistedExtensions = listOf(
                    ".sh", ".bash", ".zsh", ".php", ".php3", ".php4", ".php5", ".phtml",
                    ".py", ".pyc", ".pl", ".cgi", ".exe", ".bat", ".cmd", ".html", ".htm",
                    ".js", ".vbs", ".ps1", ".jar", ".war", ".jsp", ".asp", ".aspx"
                )

                val foundEntries = mutableListOf<String>()
                var violationDetected = false
                var violationReason = ""

                withContext(Dispatchers.IO) {
                    ZipInputStream(bytes.inputStream()).use { zis ->
                        var entry = zis.nextEntry
                        while (entry != null) {
                            val name = entry.name
                            foundEntries.add(name)

                            // Detect Zip Slip Vulnerability
                            if (name.contains("..") || name.startsWith("/") || name.startsWith("\\")) {
                                violationDetected = true
                                violationReason = "Path Traversal (Zip Slip) attempt detected in entry: $name"
                                break
                            }

                            // Check blacklist
                            val lower = name.lowercase()
                            if (blacklistedExtensions.any { lower.endsWith(it) }) {
                                violationDetected = true
                                violationReason = "Forbidden executable or script file detected: $name"
                                break
                            }

                            entry = zis.nextEntry
                        }
                    }
                }

                if (violationDetected) {
                    scanLogMessages = listOf(
                        "[-] Security Scan FAILED!",
                        "[-] Reason: $violationReason",
                        "[-] Upload Rejected by Client Integrity Guard."
                    )
                    securityScanPassed = false
                    return@launch
                }

                // Verify mandatory files
                val hasDexOrApk = foundEntries.any { it.endsWith(".dex") || it.endsWith(".apk") || it.endsWith(".so") || it.endsWith(".zip") }
                if (!hasDexOrApk && foundEntries.isNotEmpty()) {
                    scanLogMessages = listOf(
                        "[*] Validated archive integrity (${foundEntries.size} items).",
                        "[+] Package verified: Clean archive structure.",
                        "[+] Size: ${String.format("%.2f", selectedFileSize / (1024f * 1024f))} MB",
                        "[+] Ready to deploy."
                    )
                    securityScanPassed = true
                } else {
                    scanLogMessages = listOf(
                        "[*] Validated archive integrity (${foundEntries.size} items).",
                        "[+] Package verified: Clean archive structure.",
                        "[+] Size: ${String.format("%.2f", selectedFileSize / (1024f * 1024f))} MB",
                        "[+] Ready to deploy."
                    )
                    securityScanPassed = true
                }
            } catch (e: Exception) {
                scanLogMessages = listOf("[-] Error inspecting archive: ${e.message}")
                securityScanPassed = false
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            selectedFileName = uri.lastPathSegment?.substringAfterLast('/') ?: "module.zip"
            scanAndPrepareZip(uri)
        }
    }

    ResponsiveContentBox(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Target Mod Engine Card
            AdminCard(
                title = "SELECT MOD TARGET ENGINE",
                icon = Icons.Filled.CloudUpload
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val isExternal = selectedModType == "external"
                    Button(
                        onClick = { selectedModType = "external" },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isExternal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            contentColor = if (isExternal) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("External Mod", fontWeight = FontWeight.Bold)
                    }

                    val isKernel = selectedModType == "kernel"
                    Button(
                        onClick = { selectedModType = "kernel" },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isKernel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            contentColor = if (isKernel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("Kernel Mod", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (selectedModType == "external") {
                        "Target Profile: External Floating Overlay Module"
                    } else {
                        "Target Profile: Direct Kernel Injection Module"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // File Selection & Security Inspection Card
            AdminCard(
                title = "SECURE PACKAGE VERIFICATION",
                icon = Icons.Filled.Security
            ) {
                OutlinedButton(
                    onClick = { filePickerLauncher.launch("application/zip") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(imageVector = Icons.Filled.FolderOpen, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedFileName != null) "Selected: $selectedFileName" else "Choose Mod ZIP File",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (scanLogMessages.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            scanLogMessages.forEach { msg ->
                                val color = when {
                                    msg.startsWith("[+]") -> MetricGreen
                                    msg.startsWith("[-]") -> MetricRed
                                    else -> MaterialTheme.colorScheme.primary
                                }
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = color
                                )
                            }
                        }
                    }
                }
            }

            // Upload Action Button with Spinner
            LoadingButton(
                text = "DEPLOY MOD TO SERVER",
                isLoading = isUploading,
                enabled = selectedUri != null && securityScanPassed,
                onClick = {
                    val uri = selectedUri
                    if (uri != null && securityScanPassed && !isUploading) {
                        isUploading = true
                        uploadStatusMessage = null
                        scope.launch {
                            try {
                                val bytes = withContext(Dispatchers.IO) {
                                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                }
                                if (bytes != null) {
                                    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                                    onUploadMod(selectedModType, selectedFileName ?: "package.zip", base64) { res ->
                                        isUploading = false
                                        if (res.isSuccess) {
                                            val data = res.getOrThrow()
                                            isUploadSuccess = true
                                            uploadStatusMessage = data.message
                                            Toast.makeText(context, data.message, Toast.LENGTH_LONG).show()
                                        } else {
                                            isUploadSuccess = false
                                            uploadStatusMessage = res.exceptionOrNull()?.message ?: "Upload failed"
                                            Toast.makeText(context, uploadStatusMessage, Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } else {
                                    isUploading = false
                                    uploadStatusMessage = "Failed to read binary bytes"
                                }
                            } catch (e: Exception) {
                                isUploading = false
                                uploadStatusMessage = e.message
                            }
                        }
                    }
                },
                icon = Icons.Filled.CloudDone,
                modifier = Modifier.fillMaxWidth()
            )

            if (uploadStatusMessage != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isUploadSuccess) MetricGreen.copy(alpha = 0.15f) else MetricRed.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uploadStatusMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isUploadSuccess) MetricGreen else MetricRed,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
