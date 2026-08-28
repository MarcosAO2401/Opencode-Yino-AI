package com.yino.ai.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yino.ai.ui.theme.AnimatedYinoBackground

sealed class YinoDestination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    data object Chat : YinoDestination("chat", "Chat", Icons.AutoMirrored.Filled.Chat)
    data object Voice : YinoDestination("voice", "Voz", Icons.Filled.Mic)
    data object Automation :
        YinoDestination("automation", "Automatización", Icons.Filled.TouchApp)

    data object Apps : YinoDestination("apps", "Apps", Icons.Filled.Apps)
    data object Memory : YinoDestination("memory", "Memoria", Icons.Filled.Memory)
    data object Settings : YinoDestination("settings", "Ajustes", Icons.Filled.Settings)
    data object Identity :
        YinoDestination("identity", "Identidad", Icons.Filled.Face)

    companion object {
        val entries = listOf(Chat, Voice, Automation, Apps, Memory, Settings, Identity)
    }
}

@Composable
fun YinoApp() {
    val navController = rememberNavController()
    val viewModel: YinoViewModel = viewModel()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                YinoDestination.entries.forEach { destination ->
                    val selected =
                        currentDestination?.hierarchy?.any { it.route == destination.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = { navigateTo(destination, navController) },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            AnimatedYinoBackground(Modifier.fillMaxSize())
            NavHost(
                navController = navController,
                startDestination = YinoDestination.Chat.route,
                modifier = Modifier.fillMaxSize(),
                enterTransition = { slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) },
                exitTransition = { slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(300)) },
                popEnterTransition = { slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300)) },
                popExitTransition = { slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300)) },
            ) {
            composable(YinoDestination.Chat.route) { ChatScreen(viewModel) }
            composable(YinoDestination.Voice.route) { VoiceScreen(viewModel) }
            composable(YinoDestination.Automation.route) { AutomationScreen(viewModel) }
            composable(YinoDestination.Apps.route) { AppsScreen(viewModel) }
            composable(YinoDestination.Memory.route) { MemoryScreen(viewModel) }
            composable(YinoDestination.Settings.route) { SettingsScreen(viewModel) }
            composable(YinoDestination.Identity.route) { IdentityScreen(viewModel) }
        }
        }
    }
}

private fun navigateTo(destination: YinoDestination, navController: NavHostController) {
    navController.navigate(destination.route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
