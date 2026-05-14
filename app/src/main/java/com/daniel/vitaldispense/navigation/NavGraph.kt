package com.daniel.vitaldispense.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.daniel.vitaldispense.features.home.HomeScreen
import com.daniel.vitaldispense.features.paciente.DetalleMedicamentoScreen
import com.daniel.vitaldispense.features.paciente.PacienteDashboardScreen
import com.daniel.vitaldispense.features.auth.LoginScreen
import com.daniel.vitaldispense.features.auth.RegisterScreen
import com.daniel.vitaldispense.features.notifications.NotificationsScreen
import com.daniel.vitaldispense.features.settings.SettingsScreen
import com.daniel.vitaldispense.features.tomas.TomasScreen
import com.daniel.vitaldispense.features.hardware.HardwareScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Inicio.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Inicio.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Inicio.route) {
            HomeScreen()
        }
        composable(Screen.Tomas.route) {
            TomasScreen()
        }
        composable(Screen.Resumen.route) {
            PacienteDashboardScreen(onPacienteClick = { camaId ->
                // Por ahora no navegamos o podrías navegar a un detalle de cama
            })
        }
        composable(Screen.Dispensador.route) {
            HardwareScreen()
        }
        composable(Screen.Alertas.route) {
            NotificationsScreen()
        }
        composable(Screen.Ajustes.route) {
            SettingsScreen(onLogout = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            })
        }
        
        composable(Screen.DetalleMedicamento.route) { backStackEntry ->
            val medicamentoId = backStackEntry.arguments?.getString("medicamentoId") ?: ""
            DetalleMedicamentoScreen(
                medicamentoId = medicamentoId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
