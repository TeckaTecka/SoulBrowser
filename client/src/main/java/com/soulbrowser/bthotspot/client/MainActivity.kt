package com.soulbrowser.bthotspot.client

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

private const val REQ_PICK_SOUND = 1

class MainActivity : Activity() {

    private lateinit var soundLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startMonitor()

        val pad = (16 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

        root.addView(title("Hotspot Připojeno"))
        root.addView(body(
            "Tato aplikace hlídá připojení k WiFi hotspotu (z telefonu). " +
                "Jakmile se auto připojí, přehraje zvuk a zobrazí oznámení — poznáš to i přes Waze."
        ))

        soundLabel = body("Zvuk: výchozí")
        root.addView(soundLabel)

        root.addView(button("Vybrat zvuk připojení") { pickSound() })
        root.addView(button("Přehrát zvuk (test)") { playCurrentSound() })
        root.addView(button("Vypnout optimalizaci baterie") { requestIgnoreBattery() })
        root.addView(body(
            "Tip: v nastavení aplikace povol Automatické spouštění, ať se hlídání nastartuje po zapnutí auta."
        ))

        setContentView(ScrollView(this).apply { addView(root) })
        updateSoundLabel()
    }

    private fun startMonitor() {
        val intent = Intent(this, WifiMonitorService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
            else startService(intent)
        } catch (_: Exception) {}
    }

    private fun pickSound() {
        val current = prefs().getString(KEY_SOUND_URI, null)
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Zvuk připojení")
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            if (!current.isNullOrEmpty()) {
                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(current))
            }
        }
        startActivityForResult(intent, REQ_PICK_SOUND)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_PICK_SOUND && resultCode == RESULT_OK) {
            val uri: Uri? = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            prefs().edit().apply {
                if (uri == null) remove(KEY_SOUND_URI) else putString(KEY_SOUND_URI, uri.toString())
            }.apply()
            updateSoundLabel()
        }
    }

    private fun playCurrentSound() {
        try {
            val stored = prefs().getString(KEY_SOUND_URI, null)
            val uri = if (!stored.isNullOrEmpty()) Uri.parse(stored)
            else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            RingtoneManager.getRingtone(this, uri)?.play()
        } catch (_: Exception) {
            Toast.makeText(this, "Zvuk se nepodařilo přehrát", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSoundLabel() {
        val stored = prefs().getString(KEY_SOUND_URI, null)
        val name = try {
            val uri = if (!stored.isNullOrEmpty()) Uri.parse(stored)
            else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            RingtoneManager.getRingtone(this, uri)?.getTitle(this)
        } catch (_: Exception) { null }
        soundLabel.text = "Zvuk: ${name ?: "výchozí"}"
    }

    private fun requestIgnoreBattery() {
        try {
            startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        } catch (_: Exception) {
            Toast.makeText(this, "Nastavení není dostupné", Toast.LENGTH_SHORT).show()
        }
    }

    private fun prefs() = getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // --- tiny view helpers ---
    private fun title(text: String) = TextView(this).apply {
        this.text = text
        textSize = 22f
        setPadding(0, 0, 0, 24)
    }

    private fun body(text: String) = TextView(this).apply {
        this.text = text
        textSize = 15f
        setPadding(0, 8, 0, 8)
    }

    private fun button(text: String, onClick: () -> Unit) = Button(this).apply {
        this.text = text
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = 16 }
    }
}
