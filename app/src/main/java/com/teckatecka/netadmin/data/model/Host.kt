package com.teckatecka.netadmin.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hosts")
data class Host(
    @PrimaryKey(autoGenerate = true)
    val id:         Long   = 0,
    val ip:         String,
    val mac:        String = "",
    val hostname:   String = "",
    val vendor:     String = "",
    val isUp:       Boolean = false,
    val openPorts:  String  = "",   // JSON array uložený jako string
    val lastSeen:   Long    = System.currentTimeMillis(),
    val label:      String  = "",
    val notes:      String  = ""
)
