package com.soulbrowser.bthotspot.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bthotspot_prefs")

class PrefsRepository(private val context: Context) {

    companion object {
        private val KEY_DEVICE_ADDRESS = stringPreferencesKey("selected_device_address")
        private val KEY_DEVICE_NAME = stringPreferencesKey("selected_device_name")
        private val KEY_SERVICE_ENABLED = booleanPreferencesKey("service_enabled")
        private val KEY_AUTO_DISABLE = booleanPreferencesKey("auto_disable_on_disconnect")
        private val KEY_WATCHDOG_WARN = booleanPreferencesKey("watchdog_warning_enabled")
        private val KEY_EVENT_NOTIF = booleanPreferencesKey("event_notifications_enabled")
        private val KEY_SOUND_ENABLE_URI = stringPreferencesKey("sound_on_enable_uri")
        private val KEY_SOUND_DISABLE_URI = stringPreferencesKey("sound_on_disable_uri")
        private val KEY_DISCONNECT_DELAY = intPreferencesKey("disconnect_delay_seconds")

        const val DISCONNECT_DELAY_MAX = 600
    }

    val selectedDevice: Flow<DeviceInfo?> = context.dataStore.data.map { prefs ->
        val address = prefs[KEY_DEVICE_ADDRESS] ?: return@map null
        val name = prefs[KEY_DEVICE_NAME] ?: address
        DeviceInfo(address = address, name = name)
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

    suspend fun saveSelectedDevice(device: DeviceInfo?) {
        context.dataStore.edit { prefs ->
            if (device == null) {
                prefs.remove(KEY_DEVICE_ADDRESS)
                prefs.remove(KEY_DEVICE_NAME)
            } else {
                prefs[KEY_DEVICE_ADDRESS] = device.address
                prefs[KEY_DEVICE_NAME] = device.name
            }
        }
    }

    suspend fun setServiceEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SERVICE_ENABLED] = enabled
        }
    }

    suspend fun setAutoDisableOnDisconnect(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTO_DISABLE] = enabled
        }
    }

    suspend fun setWatchdogWarningEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WATCHDOG_WARN] = enabled
        }
    }

    suspend fun setEventNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_EVENT_NOTIF] = enabled
        }
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
}
