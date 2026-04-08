package com.wifianalyze.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "speed_test_history")
data class SpeedTestHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val downloadMbps: Float,
    val uploadMbps: Float,
    val ssid: String
)
