package com.wifianalyze.ui.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.wrapContentSize
import com.wifianalyze.MainActivity
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.wifianalyze.data.widget.WidgetUpdater

class WifiWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs   = currentState<Preferences>()
            val ssid    = prefs[WidgetUpdater.KEY_SSID]    ?: "Not connected"
            val rssi    = prefs[WidgetUpdater.KEY_RSSI]    ?: -100
            val quality = prefs[WidgetUpdater.KEY_QUALITY] ?: "No Signal"

            val qualityColor = when {
                rssi >= -50 -> Color(0xFF4CAF50)
                rssi >= -60 -> Color(0xFF8BC34A)
                rssi >= -70 -> Color(0xFFFFC107)
                rssi >= -80 -> Color(0xFFFF9800)
                else        -> Color(0xFFF44336)
            }

            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.widgetBackground)
                        .padding(12.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = GlanceModifier.wrapContentSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = quality,
                            style = TextStyle(
                                color = ColorProvider(qualityColor),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = "$rssi dBm",
                            style = TextStyle(
                                color = ColorProvider(Color.Gray),
                                fontSize = 12.sp
                            )
                        )
                        Text(
                            text = ssid,
                            style = TextStyle(fontSize = 10.sp),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
