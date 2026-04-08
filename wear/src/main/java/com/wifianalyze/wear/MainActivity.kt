package com.wifianalyze.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.wear.compose.material.MaterialTheme
import com.wifianalyze.wear.presentation.WearSignalScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val data by WearSignalState.data.collectAsState()
                WearSignalScreen(data = data)
            }
        }
    }
}
