package com.wifianalyze.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "signal_history")
data class SignalHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val rssi: Int,
    val ssid: String,
    val band: String
)
