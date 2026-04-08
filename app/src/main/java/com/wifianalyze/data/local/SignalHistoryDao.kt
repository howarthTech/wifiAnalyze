package com.wifianalyze.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.wifianalyze.data.local.entity.SignalHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SignalHistoryDao {

    @Insert
    suspend fun insert(entry: SignalHistoryEntity)

    @Query("SELECT * FROM signal_history WHERE timestamp > :since ORDER BY timestamp ASC")
    fun getHistory(since: Long): Flow<List<SignalHistoryEntity>>

    @Query("DELETE FROM signal_history WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)

    @Query("SELECT COUNT(*) FROM signal_history")
    suspend fun count(): Int
}
