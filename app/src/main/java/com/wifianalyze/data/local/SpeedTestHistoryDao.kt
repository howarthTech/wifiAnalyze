package com.wifianalyze.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.wifianalyze.data.local.entity.SpeedTestHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeedTestHistoryDao {

    @Insert
    suspend fun insert(entry: SpeedTestHistoryEntity)

    @Query("SELECT * FROM speed_test_history WHERE timestamp > :since ORDER BY timestamp ASC")
    fun getHistory(since: Long): Flow<List<SpeedTestHistoryEntity>>

    @Query("DELETE FROM speed_test_history WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
}
