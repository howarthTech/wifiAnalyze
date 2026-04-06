package com.wifianalyze.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "room_readings")
data class RoomReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomName: String,
    val rssi: Int,
    val ssid: String,
    val frequency: Int,
    val channel: Int,
    val band: String,
    val quality: String,
    val competingNetworks: Int,
    val congestionLevel: String,
    val iotReady: Boolean,
    val timestamp: Long
)
