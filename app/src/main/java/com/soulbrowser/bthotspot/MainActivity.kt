package com.soulbrowser.bthotspot

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.soulbrowser.bthotspot.data.DeviceInfo
import com.soulbrowser.bthotspot.hotspot.HotspotController
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
    val autoDisable by viewModel.autoDisableOnDisconnect.collectAsStateWithLifecycle()
    val watchdogWarn by viewModel.watchdogWarningEnabled.collectAsStateWithLifecycle()
    val eventNotif by viewModel.eventNotificationsEnabled.collectAsStateWithLifecycle()
    val enableSoundUri by viewModel.enableSoundUri.collectAsStateWithLifecycle()
    val disableSoundUri by viewModel.disableSoundUri.collectAsStateWithLifecycle()
    val disconnectDelay by viewModel.disconnectDelaySeconds.collectAsStateWithLifecycle()
    val automationEnabled by viewModel.automationEnabled.collectAsStateWithLifecycle()
    val skipOnWifi by viewModel.skipWhenOnWifiInternet.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDevicePicker by remember { mutableStateOf(false) }
    var logLines by remember { mutableStateOf<List<String>?>(null) }

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
            // Master automation switch (also available as a Quick Settings tile)
            item {
                SimpleSwitchCard(
                    title = stringResource(R.string.automation_title),
                    desc = stringResource(R.string.automation_desc),
                    checked = automationEnabled,
                    onCheckedChange = { viewModel.setAutomationEnabled(it) }
                )
            }

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

            // Auto-disable on disconnect setting
            item {
                AutoDisableCard(
                    checked = autoDisable,
                    onCheckedChange = { viewModel.setAutoDisableOnDisconnect(it) }
                )
            }

            // Disconnect grace period
            item {
                DisconnectDelayCard(
                    delay = disconnectDelay,
                    onChange = { viewModel.setDisconnectDelaySeconds(it) }
                )
            }

            // Skip when already on WiFi with internet
            item {
                SimpleSwitchCard(
                    title = stringResource(R.string.skip_wifi_title),
                    desc = stringResource(R.string.skip_wifi_desc),
                    checked = skipOnWifi,
                    onCheckedChange = { viewModel.setSkipWhenOnWifiInternet(it) }
                )
            }

            // Event log
            item {
                LogCard(onShow = { viewModel.loadLog { logLines = it } })
            }

            // Notifications & sounds
            item {
                NotificationsSoundsCard(
                    watchdogWarn = watchdogWarn,
                    onWatchdogWarnChange = { viewModel.setWatchdogWarningEnabled(it) },
                    eventNotif = eventNotif,
                    onEventNotifChange = { viewModel.setEventNotificationsEnabled(it) },
                    enableSoundUri = enableSoundUri,
                    onEnableSoundChange = { viewModel.setEnableSoundUri(it) },
                    disableSoundUri = disableSoundUri,
                    onDisableSoundChange = { viewModel.setDisableSoundUri(it) }
                )
            }

            // Diagnostic test card
            item {
                TestHotspotCard(
                    onTest = { onResult ->
                        HotspotController.test(context) { result -> onResult(result) }
                    },
                    onTestCycle = { onResult ->
                        HotspotController.testCycle(context) { result -> onResult(result) }
                    }
                )
            }

            // How it works info
            item {
                HowItWorksCard()
            }

            // Version footer
            item {
                val version = remember {
                    runCatching {
                        context.packageManager.getPackageInfo(context.packageName, 0).versionName
                    }.getOrNull() ?: ""
                }
                Text(
                    text = stringResource(R.string.version_label, version),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    textAlign = TextAlign.Center
                )
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

    logLines?.let { lines ->
        AlertDialog(
            onDismissRequest = { logLines = null },
            title = { Text(stringResource(R.string.log_title)) },
            text = {
                if (lines.isEmpty()) {
                    Text(stringResource(R.string.log_empty))
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        items(lines) { line ->
                            Text(
                                line,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { logLines = null }) { Text(stringResource(R.string.close)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.clearLog()
                    logLines = emptyList()
                }) { Text(stringResource(R.string.log_clear)) }
            }
        )
    }
}

@Composable
fun DisconnectDelayCard(delay: Int, onChange: (Int) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.delay_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                stringResource(R.string.delay_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            var text by remember(delay) { mutableStateOf(if (delay == 0) "" else delay.toString()) }
            OutlinedTextField(
                value = text,
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }.take(3)
                    text = digits
                    onChange((digits.toIntOrNull() ?: 0).coerceIn(0, 600))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text(stringResource(R.string.delay_field_label)) },
                modifier = Modifier.width(180.dp)
            )
        }
    }
}

@Composable
fun LogCard(onShow: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.log_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                stringResource(R.string.log_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onShow) { Text(stringResource(R.string.log_show)) }
        }
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
fun NotificationsSoundsCard(
    watchdogWarn: Boolean,
    onWatchdogWarnChange: (Boolean) -> Unit,
    eventNotif: Boolean,
    onEventNotifChange: (Boolean) -> Unit,
    enableSoundUri: String?,
    onEnableSoundChange: (String?) -> Unit,
    disableSoundUri: String?,
    onDisableSoundChange: (String?) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Notifications, null)
                Text(
                    stringResource(R.string.notifications_section),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            SwitchRow(
                title = stringResource(R.string.watchdog_warn_setting_title),
                desc = stringResource(R.string.watchdog_warn_setting_desc),
                checked = watchdogWarn,
                onCheckedChange = onWatchdogWarnChange
            )
            SwitchRow(
                title = stringResource(R.string.event_notif_title),
                desc = stringResource(R.string.event_notif_desc),
                checked = eventNotif,
                onCheckedChange = onEventNotifChange
            )
            SoundRow(
                title = stringResource(R.string.sound_enable_title),
                currentUri = enableSoundUri,
                onPicked = onEnableSoundChange
            )
            SoundRow(
                title = stringResource(R.string.sound_disable_title),
                currentUri = disableSoundUri,
                onPicked = onDisableSoundChange
            )
        }
    }
}

@Composable
fun SwitchRow(title: String, desc: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SoundRow(title: String, currentUri: String?, onPicked: (String?) -> Unit) {
    val context = LocalContext.current
    val soundName = remember(currentUri) {
        if (currentUri.isNullOrEmpty()) null
        else runCatching {
            RingtoneManager.getRingtone(context, Uri.parse(currentUri))?.getTitle(context)
        }.getOrNull()
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            @Suppress("DEPRECATION")
            val uri: Uri? = result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            onPicked(uri?.toString())
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                soundName ?: stringResource(R.string.sound_none),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (!currentUri.isNullOrEmpty()) {
            TextButton(onClick = { onPicked(null) }) { Text(stringResource(R.string.sound_clear)) }
        }
        TextButton(onClick = {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, title)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                if (!currentUri.isNullOrEmpty()) {
                    putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(currentUri))
                }
            }
            launcher.launch(intent)
        }) { Text(stringResource(R.string.sound_pick)) }
    }
}

@Composable
fun SimpleSwitchCard(title: String, desc: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun AutoDisableCard(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Wifi, null)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.auto_disable_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(R.string.auto_disable_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun TestHotspotCard(
    onTest: ((String) -> Unit) -> Unit,
    onTestCycle: ((String) -> Unit) -> Unit,
) {
    var resultText by remember { mutableStateOf<String?>(null) }
    var running by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.test_hotspot_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(stringResource(R.string.test_hotspot_desc), style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        running = true
                        onTest { result ->
                            resultText = result
                            running = false
                        }
                    },
                    enabled = !running
                ) {
                    Text(stringResource(R.string.test_hotspot_button))
                }
                OutlinedButton(
                    onClick = {
                        running = true
                        onTestCycle { result ->
                            resultText = result
                            running = false
                        }
                    },
                    enabled = !running
                ) {
                    Text(stringResource(R.string.test_cycle_button))
                }
            }
            if (running) {
                Text(stringResource(R.string.test_running), style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    if (resultText != null) {
        AlertDialog(
            onDismissRequest = { resultText = null },
            title = { Text(stringResource(R.string.test_result_title)) },
            text = { Text(resultText ?: "", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { resultText = null }) { Text(stringResource(R.string.close)) }
            }
        )
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
