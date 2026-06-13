package com.teckatecka.netadmin.data.db

import androidx.room.*
import com.teckatecka.netadmin.data.model.MonitoredHostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonitoredHostDao {
    @Query("SELECT * FROM monitored_hosts ORDER BY label ASC")
    fun getAll(): Flow<List<MonitoredHostEntity>>

    @Query("SELECT * FROM monitored_hosts")
    suspend fun getAllOnce(): List<MonitoredHostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(host: MonitoredHostEntity): Long

    @Update
    suspend fun update(host: MonitoredHostEntity)

    @Delete
    suspend fun delete(host: MonitoredHostEntity)
}
