package com.wifianalyze

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.wifianalyze.data.preferences.AppPreferences
import com.wifianalyze.ui.navigation.AppNavigation
import com.wifianalyze.ui.theme.WifiAnalyzeTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val forceDark by appPreferences.isDarkMode.collectAsState(initial = false)
            WifiAnalyzeTheme(darkTheme = forceDark || isSystemInDarkTheme()) {
                AppNavigation(appPreferences = appPreferences)
            }
        }
    }
}
