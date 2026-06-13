package com.teckatecka.netadmin.data.db

import androidx.room.*
import com.teckatecka.netadmin.data.model.SpeedTestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeedTestDao {
    @Query("SELECT * FROM speed_tests ORDER BY timestamp DESC")
    fun getAll(): Flow<List<SpeedTestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: SpeedTestEntity): Long

    @Delete
    suspend fun delete(record: SpeedTestEntity)

    @Query("DELETE FROM speed_tests")
    suspend fun deleteAll()
}
