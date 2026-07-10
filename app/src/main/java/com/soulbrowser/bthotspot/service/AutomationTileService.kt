package com.soulbrowser.bthotspot.service

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.soulbrowser.bthotspot.R
import com.soulbrowser.bthotspot.data.PrefsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Quick Settings tile to pause / resume the whole automation without opening the app.
 * Active = automation on, Inactive = paused.
 */
class AutomationTileService : TileService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val prefs by lazy { PrefsRepository(applicationContext) }

    override fun onStartListening() {
        scope.launch { render(prefs.serviceEnabled.first()) }
    }

    override fun onClick() {
        scope.launch {
            val next = !prefs.serviceEnabled.first()
            prefs.setServiceEnabled(next)
            render(next)
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun render(enabled: Boolean) {
        val tile = qsTile ?: return
        tile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.tile_label)
        tile.updateTile()
    }
}
