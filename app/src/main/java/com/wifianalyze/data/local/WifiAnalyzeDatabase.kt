package com.wifianalyze.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wifianalyze.data.local.entity.RoomReadingEntity

@Database(entities = [RoomReadingEntity::class], version = 1, exportSchema = false)
abstract class WifiAnalyzeDatabase : RoomDatabase() {
    abstract fun roomReadingDao(): RoomReadingDao
}
