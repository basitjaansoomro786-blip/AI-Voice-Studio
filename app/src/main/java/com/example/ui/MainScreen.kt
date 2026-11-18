package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Mic)
    object Library : Screen("library", "Voices", Icons.Default.RecordVoiceOver)
    object History : Screen("history", "History", Icons.Default.History)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object About : Screen("about", "About", Icons.Default.Info)
}

val screens = listOf(
    Screen.Home,
    Screen.Library,
    Screen.History,
    Screen.Settings,
    Screen.About
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                AdMobBanner()
                NavigationBar(
                    containerColor = SurfaceDark,
                    tonalElevation = 8.dp
                ) {
                    screens.forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NeonCyan,
                                selectedTextColor = NeonCyan,
                                unselectedIconColor = TextGray,
                                unselectedTextColor = TextGray,
                                indicatorColor = CardDark
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                is Screen.Home -> HomeScreen(viewModel)
                is Screen.Library -> VoiceLibraryScreen(viewModel)
                is Screen.History -> AudioHistoryScreen(viewModel)
                is Screen.Settings -> SettingsScreen(viewModel)
                is Screen.About -> AboutScreen()
            }
        }
    }
}
