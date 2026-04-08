package com.wifianalyze.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wifianalyze.data.local.entity.LatencyHistoryEntity
import com.wifianalyze.data.local.entity.RoomReadingEntity
import com.wifianalyze.data.local.entity.SignalHistoryEntity
import com.wifianalyze.data.local.entity.SpeedTestHistoryEntity

@Database(
    entities = [
        RoomReadingEntity::class,
        SignalHistoryEntity::class,
        SpeedTestHistoryEntity::class,
        LatencyHistoryEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class WifiAnalyzeDatabase : RoomDatabase() {
    abstract fun roomReadingDao(): RoomReadingDao
    abstract fun signalHistoryDao(): SignalHistoryDao
    abstract fun speedTestHistoryDao(): SpeedTestHistoryDao
    abstract fun latencyHistoryDao(): LatencyHistoryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `signal_history` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `rssi` INTEGER NOT NULL,
                        `ssid` TEXT NOT NULL,
                        `band` TEXT NOT NULL
                    )"""
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `speed_test_history` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `downloadMbps` REAL NOT NULL,
                        `uploadMbps` REAL NOT NULL,
                        `ssid` TEXT NOT NULL
                    )"""
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `latency_history` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `gatewayAvgMs` REAL,
                        `internetAvgMs` REAL,
                        `ssid` TEXT NOT NULL
                    )"""
                )
            }
        }
    }
}
