package com.daniel.vitaldispense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.daniel.vitaldispense.features.auth.LoginScreen
import com.daniel.vitaldispense.features.auth.RegisterScreen
import com.daniel.vitaldispense.features.home.HomeScreen
import com.daniel.vitaldispense.features.notifications.NotificationsScreen
import com.daniel.vitaldispense.features.paciente.DetalleMedicamentoScreen
import com.daniel.vitaldispense.features.paciente.PacienteDashboardScreen
import com.daniel.vitaldispense.features.settings.SettingsScreen
import com.daniel.vitaldispense.features.tomas.TomasScreen
import com.daniel.vitaldispense.navigation.Screen
import com.daniel.vitaldispense.ui.theme.VITALDISPENSE_FINALTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            VITALDISPENSE_FINALTheme {
                MainContent()
            }
        }
    }
}

@Composable
fun MainContent() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        BottomNavItem("Inicio", Screen.Inicio.route, Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem("Rondas", Screen.Tomas.route, Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
        BottomNavItem("Hardware", Screen.Dispensador.route, Icons.Filled.Memory, Icons.Outlined.Memory),
        BottomNavItem("Alertas", Screen.Alertas.route, Icons.Filled.Notifications, Icons.Outlined.Notifications),
        BottomNavItem("Ajustes", Screen.Ajustes.route, Icons.Filled.Settings, Icons.Outlined.Settings)
    )

    Scaffold(
        bottomBar = {
            val isAuthScreen = currentDestination?.route == Screen.Login.route || 
                              currentDestination?.route == Screen.Register.route
            
            if (!isAuthScreen) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    items.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = { 
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon, 
                                    contentDescription = item.name 
                                ) 
                            },
                            label = { Text(item.name) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) { 
                LoginScreen(
                    onLoginSuccess = { navController.navigate(Screen.Inicio.route) { popUpTo(0) } }, 
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                ) 
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = { navController.navigate(Screen.Inicio.route) { popUpTo(0) } },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.Inicio.route) { HomeScreen() }
            composable(Screen.Tomas.route) { TomasScreen() }
            composable(Screen.Dispensador.route) { /* TODO: Nueva pantalla de Hardware */ }
            composable(Screen.Alertas.route) { NotificationsScreen() }
            composable(Screen.Ajustes.route) { 
                SettingsScreen(onLogout = { 
                    navController.navigate(Screen.Login.route) { popUpTo(0) } 
                }) 
            }
        }
    }
}

data class BottomNavItem(
    val name: String, 
    val route: String, 
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)
