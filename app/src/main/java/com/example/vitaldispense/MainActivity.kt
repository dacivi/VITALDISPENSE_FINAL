package com.daniel.vitaldispense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.daniel.vitaldispense.features.home.HomeScreen
import com.daniel.vitaldispense.features.paciente.DetalleMedicamentoScreen
import com.daniel.vitaldispense.features.paciente.PacienteDashboardScreen
import com.daniel.vitaldispense.navigation.NavGraph
import com.daniel.vitaldispense.navigation.Screen
import com.daniel.vitaldispense.ui.theme.VITALDISPENSE_FINALTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
    val items = listOf(
        BottomNavItem("Home", Screen.Home.route, Icons.Default.Home),
        BottomNavItem("Pacientes", Screen.Patients.route, Icons.Default.People),
        BottomNavItem("Hardware", Screen.Dispensers.route, Icons.Default.Build),
        BottomNavItem("Alertas", Screen.Alerts.route, Icons.Default.Notifications),
        BottomNavItem("Ajustes", Screen.Settings.route, Icons.Default.Settings),
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            
            // Solo mostrar BottomBar en pantallas principales, no en detalles
            val showBottomBar = items.any { it.route == currentDestination?.route }

            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.name) },
                            label = { Text(item.name) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
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
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Integramos el NavGraph aquí directamente para manejar el padding
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) { HomeScreen() }
                composable(Screen.Patients.route) {
                    PacienteDashboardScreen(onMedicamentoClick = { medicamento ->
                        navController.navigate(Screen.DetalleMedicamento.createRoute(medicamento.id))
                    })
                }
                composable(Screen.Dispensers.route) { /* TODO: Pantalla Hardware */ }
                composable(Screen.Alerts.route) { /* TODO: Pantalla Alertas */ }
                composable(Screen.Settings.route) { /* TODO: Pantalla Ajustes */ }
                
                composable(Screen.DetalleMedicamento.route) { backStackEntry ->
                    val medicamentoId = backStackEntry.arguments?.getString("medicamentoId") ?: ""
                    DetalleMedicamentoScreen(
                        medicamentoId = medicamentoId,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

data class BottomNavItem(val name: String, val route: String, val icon: ImageVector)
