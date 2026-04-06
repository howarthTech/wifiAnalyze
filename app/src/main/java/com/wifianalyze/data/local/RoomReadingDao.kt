package com.wifianalyze.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.wifianalyze.data.local.entity.RoomReadingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomReadingDao {

    @Query("SELECT * FROM room_readings ORDER BY rssi DESC")
    fun getAllReadingsRankedBySignal(): Flow<List<RoomReadingEntity>>

    @Insert
    suspend fun insertReading(reading: RoomReadingEntity)

    @Delete
    suspend fun deleteReading(reading: RoomReadingEntity)

    @Query("DELETE FROM room_readings")
    suspend fun deleteAll()
}
