package com.soulbrowser.bthotspot.data

import android.content.Context
import java.io.File

/**
 * Tiny append-only event log persisted to a file, kept for 90 days (pruned on write).
 * Low volume (a few events per day), so rewriting the whole file on each append is fine.
 */
object EventLog {

    private const val FILE_NAME = "events.log"
    private const val RETENTION_MS = 90L * 24 * 60 * 60 * 1000 // 3 months

    data class Entry(val time: Long, val message: String)

    @Synchronized
    fun log(context: Context, message: String) {
        val now = System.currentTimeMillis()
        val kept = read(context).filter { now - it.time <= RETENTION_MS }.toMutableList()
        kept.add(Entry(now, message.replace("\n", " ")))
        write(context, kept)
    }

    @Synchronized
    fun read(context: Context): List<Entry> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return emptyList()
        return try {
            file.readLines().mapNotNull { line ->
                val i = line.indexOf('\t')
                if (i <= 0) return@mapNotNull null
                val t = line.substring(0, i).toLongOrNull() ?: return@mapNotNull null
                Entry(t, line.substring(i + 1))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Synchronized
    fun clear(context: Context) {
        runCatching { File(context.filesDir, FILE_NAME).delete() }
    }

    private fun write(context: Context, entries: List<Entry>) {
        try {
            File(context.filesDir, FILE_NAME).writeText(
                entries.joinToString("\n") { "${it.time}\t${it.message}" }
            )
        } catch (e: Exception) {
            // ignore — logging must never crash the app
        }
    }
}
