package com.yino.ai.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yino.ai.ui.components.YinoAvatar
import com.yino.ai.ui.components.YinoIconButton
import com.yino.ai.ui.theme.AnimatedYinoBackground
import com.yino.ai.ui.theme.YinoColors
import com.yino.ai.ui.theme.YinoMotion
import com.yino.ai.ui.theme.YinoSpacing
import kotlinx.coroutines.launch

sealed class YinoDestination(
    val route: String,
    val label: String,
    private val _icon: androidx.compose.ui.graphics.vector.ImageVector?,
) {
    val icon: androidx.compose.ui.graphics.vector.ImageVector
        get() = _icon ?: Icons.Filled.Face // Fallback seguro

    data object Chat : YinoDestination("chat", "Chat", Icons.AutoMirrored.Filled.Chat)
    data object Voice : YinoDestination("voice", "Voz", Icons.Filled.Mic)
    data object Automation :
        YinoDestination("automation", "Automatizar", Icons.Filled.TouchApp)

    data object Apps : YinoDestination("apps", "Apps", Icons.Filled.Apps)
    data object Memory : YinoDestination("memory", "Memoria", Icons.Filled.Memory)
    data object Settings : YinoDestination("settings", "Ajustes", Icons.Filled.Settings)
    data object Identity :
        YinoDestination("identity", "Identidad", Icons.Filled.Face)

    companion object {
        val entries = listOf(Chat, Voice, Automation, Apps, Memory, Settings, Identity)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YinoApp() {
    val navController = rememberNavController()
    val viewModel: YinoViewModel = viewModel()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentRoute = currentDestination?.route
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showMore by remember { mutableStateOf(false) }
    var showQuick by remember { mutableStateOf(false) }

    val main = listOf(YinoDestination.Chat, YinoDestination.Voice, YinoDestination.Automation, YinoDestination.Apps)
    val more = listOf(YinoDestination.Memory, YinoDestination.Settings, YinoDestination.Identity)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(Modifier.fillMaxWidth().padding(YinoSpacing.l)) {
                YinoAvatar(size = 56.dp)
                Spacer(Modifier.width(YinoSpacing.s))
                Text("Yino AI", color = YinoColors.textPrimary, fontSize = 20.sp)
                Spacer(Modifier.size(YinoSpacing.l))
                YinoDestination.entries.forEach { dest ->
                    if (dest == null) return@forEach
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = YinoSpacing.s)
                            .clickable {
                                navigateTo(dest, navController)
                                scope.launch { drawerState.close() }
                            },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            dest.icon,
                            null,
                            tint = if (currentRoute == dest.route) YinoColors.accentSecondary else YinoColors.textTertiary,
                        )
                        Spacer(Modifier.width(YinoSpacing.m))
                        Text(
                            dest.label,
                            color = if (currentRoute == dest.route) YinoColors.accentSecondary else YinoColors.textPrimary,
                        )
                    }
                }
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            YinoAvatar(size = 36.dp)
                            Spacer(Modifier.width(YinoSpacing.s))
                            Column {
                                Text("Yino AI", color = YinoColors.textPrimary, fontSize = 18.sp)
                                Text("Asistente Inteligente", color = YinoColors.textSecondary, fontSize = 12.sp)
                            }
                        }
                    },
                    navigationIcon = {
                        YinoIconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, null)
                        }
                    },
                    actions = {
                        YinoIconButton(onClick = { showQuick = true }) {
                            Icon(Icons.Filled.Bolt, null)
                        }
                    },
                )
            },
            bottomBar = {
                NavigationBar {
                    main.filterNotNull().forEach { dest ->
                        val selected = currentRoute == dest.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navigateTo(dest, navController) },
                            icon = { Icon(dest.icon, contentDescription = dest.label, tint = if (selected) YinoColors.accentSecondary else YinoColors.textTertiary) },
                            label = { Text(dest.label, color = if (selected) YinoColors.accentSecondary else YinoColors.textTertiary) },
                        )
                    }
                    val moreSelected = more.filterNotNull().any { it.route == currentRoute }
                    NavigationBarItem(
                        selected = moreSelected,
                        onClick = { showMore = true },
                        icon = { Icon(Icons.Filled.MoreVert, contentDescription = "Más", tint = if (moreSelected) YinoColors.accentSecondary else YinoColors.textTertiary) },
                        label = { Text("Más", color = if (moreSelected) YinoColors.accentSecondary else YinoColors.textTertiary) },
                    )
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
                    enterTransition = { fadeIn(tween(YinoMotion.normal)) + slideInHorizontally(tween(YinoMotion.normal)) { it } },
                    exitTransition = { fadeOut(tween(YinoMotion.normal)) + slideOutHorizontally(tween(YinoMotion.normal)) { -it } },
                    popEnterTransition = { fadeIn(tween(YinoMotion.normal)) + slideInHorizontally(tween(YinoMotion.normal)) { -it } },
                    popExitTransition = { fadeOut(tween(YinoMotion.normal)) + slideOutHorizontally(tween(YinoMotion.normal)) { it } },
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

    if (showMore) {
        ModalBottomSheet(onDismissRequest = { showMore = false }, sheetState = rememberModalBottomSheetState()) {
            Column(Modifier.fillMaxWidth().padding(YinoSpacing.l)) {
                Text("Más", color = YinoColors.textPrimary, fontSize = 18.sp)
                Spacer(Modifier.size(YinoSpacing.s))
                more.filterNotNull().forEach { dest ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = YinoSpacing.s)
                            .clickable { navigateTo(dest, navController); showMore = false },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(dest.icon, null, tint = YinoColors.accentSecondary)
                        Spacer(Modifier.width(YinoSpacing.m))
                        Text(dest.label, color = YinoColors.textPrimary)
                    }
                }
            }
        }
    }

    if (showQuick) {
        ModalBottomSheet(onDismissRequest = { showQuick = false }, sheetState = rememberModalBottomSheetState()) {
            Column(Modifier.fillMaxWidth().padding(YinoSpacing.l)) {
                Text("Acciones rápidas", color = YinoColors.textPrimary, fontSize = 18.sp)
                Spacer(Modifier.size(YinoSpacing.s))
                listOf(YinoDestination.Chat, YinoDestination.Voice, YinoDestination.Automation, YinoDestination.Apps).filterNotNull().forEach { dest ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = YinoSpacing.s)
                            .clickable { navigateTo(dest, navController); showQuick = false },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(dest.icon, null, tint = YinoColors.accentSecondary)
                        Spacer(Modifier.width(YinoSpacing.m))
                        Text(dest.label, color = YinoColors.textPrimary)
                    }
                }
            }
        }
    }
}

private fun navigateTo(destination: YinoDestination, navController: NavHostController) {
    navController.navigate(destination.route) {
        popUpTo(navController.graph.findStartDestination().id)
        launchSingleTop = true
    }
}
