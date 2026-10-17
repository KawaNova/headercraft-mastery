package com.kawanova.headercraft.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kawanova.headercraft.ui.*
import com.kawanova.headercraft.ui.theme.HeaderSky
import com.kawanova.headercraft.ui.theme.Slate400
import com.kawanova.headercraft.ui.theme.Slate900

sealed class Screen(val route: String, val titleJa: String, val icon: ImageVector) {
    object Builder : Screen("builder", "ビルダー", Icons.Default.Build)
    object Presets : Screen("presets", "プリセット", Icons.Default.ElectricBolt)
    object Glossary : Screen("glossary", "辞典", Icons.Default.MenuBook)
    object Saved : Screen("saved", "保存済", Icons.Default.Bookmark)
}

val bottomNavItems = listOf(
    Screen.Builder,
    Screen.Presets,
    Screen.Glossary,
    Screen.Saved
)

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: HeaderViewModel,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Slate900
            ) {
                bottomNavItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.titleJa) },
                        label = { Text(screen.titleJa) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = HeaderSky,
                            selectedTextColor = HeaderSky,
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400,
                            indicatorColor = Slate900
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Builder.route,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(Screen.Builder.route) {
                HeaderBuilderScreen(viewModel = viewModel)
            }
            composable(Screen.Presets.route) {
                HeaderPresetsScreen(
                    viewModel = viewModel,
                    onPresetApplied = {
                        navController.navigate(Screen.Builder.route) {
                            popUpTo(Screen.Builder.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Glossary.route) {
                HeaderGlossaryScreen(
                    viewModel = viewModel,
                    onHeaderAddedToBuilder = {
                        navController.navigate(Screen.Builder.route) {
                            popUpTo(Screen.Builder.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Saved.route) {
                SavedHeadersScreen(
                    viewModel = viewModel,
                    onRestored = {
                        navController.navigate(Screen.Builder.route) {
                            popUpTo(Screen.Builder.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
