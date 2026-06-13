package com.teckatecka.netadmin.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rdp_profiles")
data class RdpProfile(
    @PrimaryKey(autoGenerate = true)
    val id:       Long   = 0,
    val label:    String = "",       // zobrazovaný název
    val host:     String,
    val port:     Int    = 3389,
    val username: String = "",
    val domain:   String = "",
    val notes:    String = ""
)
