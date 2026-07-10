package com.soulbrowser.bthotspot.client

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

private const val REQ_PICK_CONNECT = 1
private const val REQ_PICK_DISCONNECT = 2

class MainActivity : Activity() {

    private lateinit var connectSoundLabel: TextView
    private lateinit var disconnectSoundLabel: TextView

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
            "Hlídá připojení k WiFi hotspotu (z telefonu). Po připojení přehraje zvuk a zobrazí " +
                "oznámení — poznáš to i přes Waze."
        ))

        // --- Připojení ---
        root.addView(section("Při připojení"))
        root.addView(switchRow("Oznámení", KEY_NOTIFY_ENABLED, true))
        root.addView(switchRow("Zvuk", KEY_SOUND_ENABLED, true))
        root.addView(switchRow("Vibrace", KEY_VIBRATE_ENABLED, false))
        connectSoundLabel = body("")
        root.addView(connectSoundLabel)
        root.addView(button("Vybrat zvuk připojení") {
            pickSound(REQ_PICK_CONNECT, KEY_SOUND_URI, "Zvuk připojení")
        })
        root.addView(button("Přehrát zvuk (test)") { playSound(KEY_SOUND_URI) })

        // --- Opakování ---
        root.addView(section("Opakování zvuku"))
        root.addView(switchRow("Opakovat zvuk, dokud nepotvrdím", KEY_REPEAT_ENABLED, false))
        root.addView(numberRow("Interval opakování (s), 3–120", KEY_REPEAT_INTERVAL, 10, 3, 120))

        // --- Odpojení ---
        root.addView(section("Při odpojení"))
        root.addView(switchRow("Oznámení + zvuk i při odpojení", KEY_NOTIFY_DISCONNECT, false))
        disconnectSoundLabel = body("")
        root.addView(disconnectSoundLabel)
        root.addView(button("Vybrat zvuk odpojení") {
            pickSound(REQ_PICK_DISCONNECT, KEY_SOUND_DISCONNECT_URI, "Zvuk odpojení")
        })
        root.addView(button("Přehrát zvuk odpojení (test)") { playSound(KEY_SOUND_DISCONNECT_URI) })

        // --- Ostatní ---
        root.addView(section("Ostatní"))
        root.addView(button("Vypnout optimalizaci baterie") { requestIgnoreBattery() })
        root.addView(body(
            "Tip: v nastavení aplikace povol Automatické spouštění, ať se hlídání nastartuje po zapnutí auta."
        ))

        setContentView(ScrollView(this).apply { addView(root) })
        updateLabels()
    }

    private fun startMonitor() {
        val intent = Intent(this, WifiMonitorService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
            else startService(intent)
        } catch (_: Exception) {}
    }

    private fun pickSound(requestCode: Int, key: String, title: String) {
        val current = prefs().getString(key, null)
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, title)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            if (!current.isNullOrEmpty()) {
                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(current))
            }
        }
        startActivityForResult(intent, requestCode)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        val key = when (requestCode) {
            REQ_PICK_CONNECT -> KEY_SOUND_URI
            REQ_PICK_DISCONNECT -> KEY_SOUND_DISCONNECT_URI
            else -> return
        }
        val uri: Uri? = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        prefs().edit().apply {
            if (uri == null) remove(key) else putString(key, uri.toString())
        }.apply()
        updateLabels()
    }

    private fun playSound(key: String) {
        try {
            val stored = prefs().getString(key, null)
            val uri = if (!stored.isNullOrEmpty()) Uri.parse(stored)
            else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            RingtoneManager.getRingtone(this, uri)?.play()
        } catch (_: Exception) {
            Toast.makeText(this, "Zvuk se nepodařilo přehrát", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLabels() {
        connectSoundLabel.text = "Zvuk připojení: ${soundName(KEY_SOUND_URI)}"
        disconnectSoundLabel.text = "Zvuk odpojení: ${soundName(KEY_SOUND_DISCONNECT_URI)}"
    }

    private fun soundName(key: String): String {
        val stored = prefs().getString(key, null)
        return try {
            val uri = if (!stored.isNullOrEmpty()) Uri.parse(stored)
            else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            RingtoneManager.getRingtone(this, uri)?.getTitle(this) ?: "výchozí"
        } catch (_: Exception) { "výchozí" }
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

    private fun section(text: String) = TextView(this).apply {
        this.text = text
        textSize = 16f
        setPadding(0, 24, 0, 4)
    }

    private fun body(text: String) = TextView(this).apply {
        this.text = text
        textSize = 15f
        setPadding(0, 8, 0, 8)
    }

    private fun switchRow(label: String, key: String, default: Boolean) = Switch(this).apply {
        text = label
        textSize = 15f
        isChecked = prefs().getBoolean(key, default)
        setPadding(0, 16, 0, 16)
        setOnCheckedChangeListener { _, checked ->
            prefs().edit().putBoolean(key, checked).apply()
        }
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun numberRow(label: String, key: String, default: Int, min: Int, max: Int): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 12, 0, 4)
        }
        row.addView(body(label))
        val edit = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(prefs().getInt(key, default).toString())
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val v = (s?.toString()?.toIntOrNull() ?: default).coerceIn(min, max)
                    prefs().edit().putInt(key, v).apply()
                }
            })
        }
        row.addView(edit)
        return row
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
