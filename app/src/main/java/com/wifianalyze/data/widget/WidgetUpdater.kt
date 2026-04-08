package com.wifianalyze.data.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.wifianalyze.ui.widget.WifiWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    companion object {
        val KEY_SSID    = stringPreferencesKey("ssid")
        val KEY_RSSI    = intPreferencesKey("rssi")
        val KEY_QUALITY = stringPreferencesKey("quality")
    }

    fun update(ssid: String, rssi: Int, quality: String) {
        scope.launch {
            try {
                val manager = GlanceAppWidgetManager(context)
                val ids = manager.getGlanceIds(WifiWidget::class.java)
                ids.forEach { id ->
                    updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                        prefs.toMutablePreferences().also {
                            it[KEY_SSID] = ssid
                            it[KEY_RSSI] = rssi
                            it[KEY_QUALITY] = quality
                        }
                    }
                    WifiWidget().update(context, id)
                }
            } catch (_: Exception) { /* no widget instances placed */ }
        }
    }
}
