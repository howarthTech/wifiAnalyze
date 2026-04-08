package com.wifianalyze.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "latency_history")
data class LatencyHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val gatewayAvgMs: Float?,
    val internetAvgMs: Float?,
    val ssid: String
)
