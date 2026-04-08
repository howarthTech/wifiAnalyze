package com.wifianalyze.data.wear

import android.content.Context
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearDataSender @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun send(ssid: String, rssi: Int, quality: String, band: String) {
        try {
            val request = PutDataMapRequest.create("/wifi-signal").apply {
                dataMap.putString("ssid", ssid)
                dataMap.putInt("rssi", rssi)
                dataMap.putString("quality", quality)
                dataMap.putString("band", band)
                dataMap.putLong("ts", System.currentTimeMillis())
            }
            Wearable.getDataClient(context).putDataItem(request.asPutDataRequest().setUrgent()).await()
        } catch (_: Exception) {
            // No watch paired — silently ignore
        }
    }

    suspend fun sendDisconnected() {
        try {
            val request = PutDataMapRequest.create("/wifi-signal").apply {
                dataMap.putString("ssid", "")
                dataMap.putInt("rssi", 0)
                dataMap.putString("quality", "No Signal")
                dataMap.putString("band", "")
                dataMap.putLong("ts", System.currentTimeMillis())
            }
            Wearable.getDataClient(context).putDataItem(request.asPutDataRequest().setUrgent()).await()
        } catch (_: Exception) {
            // No watch paired — silently ignore
        }
    }
}
