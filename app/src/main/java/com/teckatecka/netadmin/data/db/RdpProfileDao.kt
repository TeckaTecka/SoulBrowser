package com.teckatecka.netadmin.data.db

import androidx.room.*
import com.teckatecka.netadmin.data.model.RdpProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface RdpProfileDao {
    @Query("SELECT * FROM rdp_profiles ORDER BY label ASC")
    fun getAll(): Flow<List<RdpProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: RdpProfile): Long

    @Update
    suspend fun update(profile: RdpProfile)

    @Delete
    suspend fun delete(profile: RdpProfile)
}
