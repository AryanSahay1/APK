package com.nexos.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nexos.ai.data.local.entity.Alarm
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: Alarm): Long

    @Update
    suspend fun update(alarm: Alarm)

    @Delete
    suspend fun delete(alarm: Alarm)

    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM alarms ORDER BY triggerAt ASC")
    fun observeAll(): Flow<List<Alarm>>

    @Query("SELECT * FROM alarms WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Alarm?

    @Query("SELECT * FROM alarms WHERE isEnabled = 1 AND isFired = 0 AND triggerAt > :now ORDER BY triggerAt ASC")
    suspend fun getPending(now: Long = System.currentTimeMillis()): List<Alarm>

    @Query("UPDATE alarms SET isFired = 1 WHERE id = :id")
    suspend fun markFired(id: Long)
}
