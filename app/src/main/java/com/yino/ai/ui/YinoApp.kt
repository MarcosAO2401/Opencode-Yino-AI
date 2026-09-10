package com.yino.ai.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yino.ai.ui.theme.YinoColors
import com.yino.ai.ui.theme.YinoTheme

@Composable
fun YinoApp(viewModel: YinoViewModel) {
    YinoTheme {
        val navController = rememberNavController()
        val backStack by navController.currentBackStackEntryAsState()
        val currentRoute = backStack?.destination?.route
        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = YinoColors.surface) {
                    listOf(
                        "chat" to Icons.Filled.Chat,
                        "voice" to Icons.Filled.Mic,
                        "automation" to Icons.Filled.TouchApp,
                        "apps" to Icons.Filled.Apps,
                        "memory" to Icons.Filled.Memory,
                        "settings" to Icons.Filled.Settings
                    ).forEach { (route, icon) ->
                        NavigationBarItem(
                            selected = currentRoute == route,
                            onClick = { navController.navigate(route) { launchSingleTop = true } },
                            icon = { Icon(icon, contentDescription = route, tint = if (currentRoute == route) YinoColors.accentSecondary else YinoColors.textSecondary) },
                            label = { Text(route, color = if (currentRoute == route) YinoColors.accentSecondary else YinoColors.textSecondary) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                NavHost(navController = navController, startDestination = "chat", modifier = Modifier.fillMaxSize()) {
                    composable("chat") { ChatScreen(viewModel) }
                    composable("voice") { VoiceScreen(viewModel) }
                    composable("automation") { AutomationScreen(viewModel) }
                    composable("apps") { AppsScreen(viewModel) }
                    composable("memory") { MemoryScreen(viewModel) }
                    composable("settings") { SettingsScreen(viewModel) }
                }
            }
        }
    }
}