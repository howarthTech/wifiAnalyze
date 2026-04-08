package com.wifianalyze.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.wifianalyze.data.local.entity.LatencyHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LatencyHistoryDao {

    @Insert
    suspend fun insert(entry: LatencyHistoryEntity)

    @Query("SELECT * FROM latency_history WHERE timestamp > :since ORDER BY timestamp ASC")
    fun getHistory(since: Long): Flow<List<LatencyHistoryEntity>>

    @Query("DELETE FROM latency_history WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
}
