package com.wifianalyze

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
            WifiAnalyzeTheme {
                AppNavigation(appPreferences = appPreferences)
            }
        }
    }
}
