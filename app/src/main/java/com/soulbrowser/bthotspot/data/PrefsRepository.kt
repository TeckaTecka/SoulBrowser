package com.soulbrowser.bthotspot.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bthotspot_prefs")

// Separator between address and name inside a stored device entry (a control char
// that never appears in a MAC address or a Bluetooth device name).
private const val SEP = ""

class PrefsRepository(private val context: Context) {

    companion object {
        // Legacy single-device keys (kept for one-time migration into the set).
        private val KEY_DEVICE_ADDRESS = stringPreferencesKey("selected_device_address")
        private val KEY_DEVICE_NAME = stringPreferencesKey("selected_device_name")
        private val KEY_DEVICES = stringSetPreferencesKey("selected_devices")
        private val KEY_SERVICE_ENABLED = booleanPreferencesKey("service_enabled")
        private val KEY_AUTO_DISABLE = booleanPreferencesKey("auto_disable_on_disconnect")
        private val KEY_WATCHDOG_WARN = booleanPreferencesKey("watchdog_warning_enabled")
        private val KEY_EVENT_NOTIF = booleanPreferencesKey("event_notifications_enabled")
        private val KEY_SOUND_ENABLE_URI = stringPreferencesKey("sound_on_enable_uri")
        private val KEY_SOUND_DISABLE_URI = stringPreferencesKey("sound_on_disable_uri")
        private val KEY_DISCONNECT_DELAY = intPreferencesKey("disconnect_delay_seconds")
        private val KEY_SKIP_ON_WIFI = booleanPreferencesKey("skip_when_on_wifi_internet")

        const val DISCONNECT_DELAY_MAX = 600

        private fun encode(d: DeviceInfo) = d.address + SEP + d.name
        private fun decode(s: String): DeviceInfo {
            val i = s.indexOf(SEP)
            return if (i < 0) DeviceInfo(s, s)
            else DeviceInfo(s.substring(0, i), s.substring(i + 1))
        }

        /** Current set, migrating a legacy single selection if the set isn't set yet. */
        private fun readSet(prefs: Preferences): Set<String> {
            prefs[KEY_DEVICES]?.let { return it }
            val addr = prefs[KEY_DEVICE_ADDRESS] ?: return emptySet()
            return setOf(addr + SEP + (prefs[KEY_DEVICE_NAME] ?: addr))
        }
    }

    /** All watched devices (sorted by name). */
    val selectedDevices: Flow<List<DeviceInfo>> = context.dataStore.data.map { prefs ->
        readSet(prefs).map { decode(it) }.sortedBy { it.name.lowercase() }
    }

    val serviceEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SERVICE_ENABLED] ?: true
    }

    val autoDisableOnDisconnect: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_DISABLE] ?: true
    }

    val watchdogWarningEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_WATCHDOG_WARN] ?: true
    }

    val eventNotificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_EVENT_NOTIF] ?: false
    }

    /** URI of the sound to play when the hotspot is turned on, or null for none. */
    val enableSoundUri: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_SOUND_ENABLE_URI]
    }

    /** URI of the sound to play when the hotspot is turned off, or null for none. */
    val disableSoundUri: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_SOUND_DISABLE_URI]
    }

    /** Grace period (seconds, 0..600) before disabling the hotspot after disconnect. */
    val disconnectDelaySeconds: Flow<Int> = context.dataStore.data.map { prefs ->
        (prefs[KEY_DISCONNECT_DELAY] ?: 0).coerceIn(0, DISCONNECT_DELAY_MAX)
    }

    /** When true, don't enable the hotspot if the phone is already on a WiFi network with internet. */
    val skipWhenOnWifiInternet: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SKIP_ON_WIFI] ?: false
    }

    /** Add the device if not watched, remove it if already watched. */
    suspend fun toggleDevice(device: DeviceInfo) {
        context.dataStore.edit { prefs ->
            val cur = readSet(prefs).toMutableSet()
            val existing = cur.firstOrNull { it.substringBefore(SEP) == device.address }
            if (existing != null) cur.remove(existing) else cur.add(encode(device))
            prefs[KEY_DEVICES] = cur
            // Legacy keys are now folded into the set.
            prefs.remove(KEY_DEVICE_ADDRESS)
            prefs.remove(KEY_DEVICE_NAME)
        }
    }

    suspend fun clearDevices() {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEVICES] = emptySet()
            prefs.remove(KEY_DEVICE_ADDRESS)
            prefs.remove(KEY_DEVICE_NAME)
        }
    }

    suspend fun setServiceEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_SERVICE_ENABLED] = enabled }
    }

    suspend fun setAutoDisableOnDisconnect(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_AUTO_DISABLE] = enabled }
    }

    suspend fun setWatchdogWarningEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_WATCHDOG_WARN] = enabled }
    }

    suspend fun setEventNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_EVENT_NOTIF] = enabled }
    }

    suspend fun setEnableSoundUri(uri: String?) {
        context.dataStore.edit { prefs ->
            if (uri == null) prefs.remove(KEY_SOUND_ENABLE_URI) else prefs[KEY_SOUND_ENABLE_URI] = uri
        }
    }

    suspend fun setDisableSoundUri(uri: String?) {
        context.dataStore.edit { prefs ->
            if (uri == null) prefs.remove(KEY_SOUND_DISABLE_URI) else prefs[KEY_SOUND_DISABLE_URI] = uri
        }
    }

    suspend fun setDisconnectDelaySeconds(seconds: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DISCONNECT_DELAY] = seconds.coerceIn(0, DISCONNECT_DELAY_MAX)
        }
    }

    suspend fun setSkipWhenOnWifiInternet(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_SKIP_ON_WIFI] = enabled }
    }
}
