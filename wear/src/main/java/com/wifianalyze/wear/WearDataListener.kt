package com.wifianalyze.wear

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class WearDataListener : WearableListenerService() {

    override fun onDataChanged(events: DataEventBuffer) {
        events.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED &&
                event.dataItem.uri.path == "/wifi-signal"
            ) {
                val map = DataMapItem.fromDataItem(event.dataItem).dataMap
                WearSignalState.update(
                    ssid = map.getString("ssid") ?: "",
                    rssi = map.getInt("rssi"),
                    quality = map.getString("quality") ?: "—",
                    band = map.getString("band") ?: ""
                )
            }
        }
    }
}
