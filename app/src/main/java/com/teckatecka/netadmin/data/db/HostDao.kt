package com.teckatecka.netadmin.data.db

import androidx.room.*
import com.teckatecka.netadmin.data.model.Host
import kotlinx.coroutines.flow.Flow

@Dao
interface HostDao {
    @Query("SELECT * FROM hosts ORDER BY ip ASC")
    fun getAll(): Flow<List<Host>>

    @Query("SELECT * FROM hosts WHERE ip = :ip LIMIT 1")
    suspend fun getByIp(ip: String): Host?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(host: Host): Long

    @Update
    suspend fun update(host: Host)

    @Delete
    suspend fun delete(host: Host)

    @Query("DELETE FROM hosts")
    suspend fun deleteAll()
}
