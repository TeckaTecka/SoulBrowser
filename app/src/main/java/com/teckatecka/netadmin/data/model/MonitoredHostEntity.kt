package com.teckatecka.netadmin.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monitored_hosts")
data class MonitoredHostEntity(
    @PrimaryKey(autoGenerate = true)
    val id:           Long   = 0,
    val label:        String = "",
    val address:      String = "",
    val intervalMin:  Int    = 5,
    val isUp:         Boolean = true,
    val lastChecked:  Long   = 0L,
    val lastDownAt:   Long   = 0L
)
