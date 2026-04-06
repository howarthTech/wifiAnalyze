package com.wifianalyze.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wifianalyze.ui.permission.PermissionScreen
import com.wifianalyze.ui.settings.SettingsScreen
import com.wifianalyze.ui.simple.RoomListScreen
import com.wifianalyze.ui.simple.RoomTestScreen
import com.wifianalyze.ui.simple.SimpleDashboardScreen
import com.wifianalyze.ui.simple.SimpleViewModel

object Routes {
    const val PERMISSION = "permission"
    const val SIMPLE = "simple"
    const val ROOM_TEST = "room_test"
    const val ROOM_LIST = "room_list"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()

    val hasPermissions = ContextCompat.checkSelfPermission(
        context, Manifest.permission.NEARBY_WIFI_DEVICES
    ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    val startDestination = if (hasPermissions) Routes.SIMPLE else Routes.PERMISSION

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.PERMISSION) {
            PermissionScreen(
                onPermissionsGranted = {
                    navController.navigate(Routes.SIMPLE) {
                        popUpTo(Routes.PERMISSION) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SIMPLE) {
            val viewModel: SimpleViewModel = hiltViewModel()
            SimpleDashboardScreen(
                onNavigateToRoomTest = { navController.navigate(Routes.ROOM_TEST) },
                onNavigateToRoomList = { navController.navigate(Routes.ROOM_LIST) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                viewModel = viewModel
            )
        }

        composable(Routes.ROOM_TEST) {
            val parentEntry = navController.getBackStackEntry(Routes.SIMPLE)
            val viewModel: SimpleViewModel = hiltViewModel(parentEntry)
            RoomTestScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        composable(Routes.ROOM_LIST) {
            val parentEntry = navController.getBackStackEntry(Routes.SIMPLE)
            val viewModel: SimpleViewModel = hiltViewModel(parentEntry)
            RoomListScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        composable(Routes.SETTINGS) {
            val parentEntry = navController.getBackStackEntry(Routes.SIMPLE)
            val viewModel: SimpleViewModel = hiltViewModel(parentEntry)
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onClearData = { viewModel.clearAllReadings() }
            )
        }
    }
}
