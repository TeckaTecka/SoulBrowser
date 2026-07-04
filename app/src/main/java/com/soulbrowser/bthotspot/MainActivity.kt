package com.soulbrowser.bthotspot

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.soulbrowser.bthotspot.data.DeviceInfo
import com.soulbrowser.bthotspot.service.HotspotAccessibilityService
import com.soulbrowser.bthotspot.ui.MainViewModel
import com.soulbrowser.bthotspot.ui.theme.BTHotspotTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { onPermissionsResult() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { moveTaskToBack(true) }
        })
        requestMissingPermissions()

        setContent {
            BTHotspotTheme {
                MainScreen(viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val a11yEnabled = HotspotAccessibilityService.isConnected()
        viewModel.refresh(a11yEnabled)
        viewModel.startService(this)
    }

    private fun requestMissingPermissions() {
        val needed = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT))
                    add(Manifest.permission.BLUETOOTH_CONNECT)
                if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN))
                    add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!hasPermission(Manifest.permission.POST_NOTIFICATIONS))
                    add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (needed.isNotEmpty()) permissionLauncher.launch(needed.toTypedArray())
    }

    private fun onPermissionsResult() {
        viewModel.refresh(HotspotAccessibilityService.isConnected())
    }

    private fun hasPermission(p: String) =
        ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED
}

// ---------- Composable UI ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedDevice by viewModel.selectedDevice.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDevicePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Write settings — primary hotspot method
            item {
                StatusCard(
                    title = stringResource(R.string.write_settings_title),
                    ok = uiState.writeSettingsGranted,
                    okText = stringResource(R.string.write_settings_granted),
                    failText = stringResource(R.string.write_settings_missing),
                    icon = { Icon(Icons.Default.Settings, null) },
                    action = if (!uiState.writeSettingsGranted) {
                        {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                    Uri.parse("package:${context.packageName}")
                                )
                            )
                        }
                    } else null,
                    actionLabel = stringResource(R.string.write_settings_grant)
                )
            }

            // Accessibility Service status card (fallback method)
            item {
                StatusCard(
                    title = stringResource(R.string.accessibility_service_label),
                    ok = uiState.accessibilityEnabled,
                    okText = stringResource(R.string.status_active),
                    failText = stringResource(R.string.accessibility_not_enabled),
                    icon = { Icon(Icons.Default.Settings, null) },
                    action = if (!uiState.accessibilityEnabled) {
                        { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
                    } else null,
                    actionLabel = stringResource(R.string.open_accessibility_settings)
                )
            }

            // Bluetooth status card
            item {
                StatusCard(
                    title = stringResource(R.string.bluetooth_status),
                    ok = uiState.btEnabled,
                    okText = stringResource(R.string.bt_on),
                    failText = stringResource(R.string.bt_off),
                    icon = { Icon(Icons.Default.Bluetooth, null) },
                    action = if (!uiState.btEnabled) {
                        { context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) }
                    } else null,
                    actionLabel = stringResource(R.string.open_bt_settings)
                )
            }

            // Selected device card
            item {
                SelectedDeviceCard(
                    device = selectedDevice,
                    onClick = { showDevicePicker = true }
                )
            }

            // How it works info
            item {
                HowItWorksCard()
            }
        }
    }

    if (showDevicePicker) {
        DevicePickerDialog(
            devices = uiState.pairedDevices,
            selectedAddress = selectedDevice?.address,
            onSelect = { device ->
                viewModel.selectDevice(device)
                showDevicePicker = false
            },
            onDismiss = { showDevicePicker = false }
        )
    }
}

@Composable
fun StatusCard(
    title: String,
    ok: Boolean,
    okText: String,
    failText: String,
    icon: @Composable () -> Unit,
    action: (() -> Unit)?,
    actionLabel: String,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            icon()
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    if (ok) okText else failText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (ok) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                )
            }
            if (ok) {
                Icon(Icons.Default.Check, null, tint = Color(0xFF2E7D32))
            } else if (action != null) {
                TextButton(onClick = action) { Text(actionLabel) }
            }
        }
    }
}

@Composable
fun SelectedDeviceCard(device: DeviceInfo?, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (device != null)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Bluetooth, null)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.selected_device),
                    style = MaterialTheme.typography.labelMedium
                )
                if (device != null) {
                    Text(device.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(device.address, style = MaterialTheme.typography.bodySmall)
                } else {
                    Text(
                        stringResource(R.string.tap_to_select),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(Icons.Default.Wifi, null)
        }
    }
}

@Composable
fun HowItWorksCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                stringResource(R.string.how_it_works_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(stringResource(R.string.how_it_works_body), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun DevicePickerDialog(
    devices: List<DeviceInfo>,
    selectedAddress: String?,
    onSelect: (DeviceInfo?) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.paired_devices)) },
        text = {
            if (devices.isEmpty()) {
                Text(stringResource(R.string.no_paired_devices))
            } else {
                LazyColumn {
                    items(devices) { device ->
                        ListItem(
                            headlineContent = { Text(device.name) },
                            supportingContent = { Text(device.address) },
                            trailingContent = {
                                if (device.address == selectedAddress) {
                                    Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            modifier = Modifier.clickable { onSelect(device) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
        dismissButton = if (selectedAddress != null) {
            { TextButton(onClick = { onSelect(null) }) { Text(stringResource(R.string.clear_selection)) } }
        } else null
    )
}
