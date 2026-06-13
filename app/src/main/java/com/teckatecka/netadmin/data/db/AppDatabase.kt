package com.teckatecka.netadmin.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.teckatecka.netadmin.data.model.Host
import com.teckatecka.netadmin.data.model.MonitoredHostEntity
import com.teckatecka.netadmin.data.model.RdpProfile
import com.teckatecka.netadmin.data.model.SpeedTestEntity

@Database(
    entities = [
        Host::class,
        RdpProfile::class,
        MonitoredHostEntity::class,
        SpeedTestEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun hostDao():          HostDao
    abstract fun rdpProfileDao():    RdpProfileDao
    abstract fun monitoredHostDao(): MonitoredHostDao
    abstract fun speedTestDao():     SpeedTestDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "netadmin.db"
                ).build().also { INSTANCE = it }
            }
    }
}
