package com.teckatecka.netadmin.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "speed_tests")
data class SpeedTestEntity(
    @PrimaryKey(autoGenerate = true)
    val id:           Long   = 0,
    val timestamp:    Long   = System.currentTimeMillis(),
    val downloadMbps: Float  = 0f,
    val uploadMbps:   Float  = 0f,
    val pingMs:       Long   = 0L,
    val serverLabel:  String = ""
)
