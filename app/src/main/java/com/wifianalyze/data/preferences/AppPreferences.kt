package com.wifianalyze.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val advancedModeKey              = booleanPreferencesKey("advanced_mode")
    private val darkModeKey                  = booleanPreferencesKey("dark_mode")
    private val alertsEnabledKey             = booleanPreferencesKey("alerts_enabled")
    private val alertThresholdKey            = intPreferencesKey("alert_threshold_dbm")
    private val lastChannelRecommendationKey = longPreferencesKey("last_channel_notif_ms")
    private val hasSeenOnboardingKey         = booleanPreferencesKey("has_seen_onboarding")

    val isAdvancedMode: Flow<Boolean>           = context.dataStore.data.map { it[advancedModeKey] ?: false }
    val isDarkMode: Flow<Boolean>               = context.dataStore.data.map { it[darkModeKey] ?: false }
    val alertsEnabled: Flow<Boolean>            = context.dataStore.data.map { it[alertsEnabledKey] ?: false }
    val alertThresholdDbm: Flow<Int>            = context.dataStore.data.map { it[alertThresholdKey] ?: -75 }
    val lastChannelRecommendationMs: Flow<Long> = context.dataStore.data.map { it[lastChannelRecommendationKey] ?: 0L }
    val hasSeenOnboarding: Flow<Boolean>        = context.dataStore.data.map { it[hasSeenOnboardingKey] ?: false }

    suspend fun setAdvancedMode(enabled: Boolean)          { context.dataStore.edit { it[advancedModeKey] = enabled } }
    suspend fun setDarkMode(enabled: Boolean)              { context.dataStore.edit { it[darkModeKey] = enabled } }
    suspend fun setAlertsEnabled(enabled: Boolean)         { context.dataStore.edit { it[alertsEnabledKey] = enabled } }
    suspend fun setAlertThresholdDbm(threshold: Int)       { context.dataStore.edit { it[alertThresholdKey] = threshold } }
    suspend fun setLastChannelRecommendationMs(ts: Long)   { context.dataStore.edit { it[lastChannelRecommendationKey] = ts } }
    suspend fun setHasSeenOnboarding(seen: Boolean)        { context.dataStore.edit { it[hasSeenOnboardingKey] = seen } }
}
