package com.wifianalyze.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wifianalyze.data.preferences.AppPreferences
import com.wifianalyze.ui.advanced.AdvancedDashboardScreen
import com.wifianalyze.ui.advanced.AdvancedViewModel
import com.wifianalyze.ui.permission.PermissionScreen
import com.wifianalyze.ui.settings.SettingsScreen
import com.wifianalyze.ui.simple.RoomListScreen
import com.wifianalyze.ui.simple.RoomTestScreen
import com.wifianalyze.ui.simple.SimpleDashboardScreen
import com.wifianalyze.ui.simple.SimpleViewModel
import kotlinx.coroutines.launch

object Routes {
    const val PERMISSION = "permission"
    const val SIMPLE = "simple"
    const val ADVANCED = "advanced"
    const val ROOM_TEST = "room_test"
    const val ROOM_LIST = "room_list"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavigation(appPreferences: AppPreferences) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val isAdvancedMode by appPreferences.isAdvancedMode.collectAsState(initial = false)

    val hasPermissions = ContextCompat.checkSelfPermission(
        context, Manifest.permission.NEARBY_WIFI_DEVICES
    ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    val dashboardRoute = if (isAdvancedMode) Routes.ADVANCED else Routes.SIMPLE
    val startDestination = if (hasPermissions) dashboardRoute else Routes.PERMISSION

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.PERMISSION) {
            PermissionScreen(
                onPermissionsGranted = {
                    val target = if (isAdvancedMode) Routes.ADVANCED else Routes.SIMPLE
                    navController.navigate(target) {
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

        composable(Routes.ADVANCED) {
            val viewModel: AdvancedViewModel = hiltViewModel()
            AdvancedDashboardScreen(
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
            // Check if Simple mode is on the backstack
            val hasSimpleOnStack = navController.currentBackStack.value
                .any { it.destination.route == Routes.SIMPLE }

            val simpleViewModel: SimpleViewModel? = if (hasSimpleOnStack) {
                val parentEntry = navController.getBackStackEntry(Routes.SIMPLE)
                hiltViewModel(parentEntry)
            } else {
                null
            }

            SettingsScreen(
                isAdvancedMode = isAdvancedMode,
                onNavigateBack = { navController.popBackStack() },
                onClearData = { simpleViewModel?.clearAllReadings() },
                onModeChanged = { advanced ->
                    scope.launch {
                        appPreferences.setAdvancedMode(advanced)
                    }
                    val target = if (advanced) Routes.ADVANCED else Routes.SIMPLE
                    navController.navigate(target) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
