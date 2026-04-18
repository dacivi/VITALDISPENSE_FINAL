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

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
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
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen()
        }
        composable(Screen.Patients.route) {
            PacienteDashboardScreen(onMedicamentoClick = { medicamento ->
                navController.navigate(Screen.DetalleMedicamento.createRoute(medicamento.id))
            })
        }
        composable(Screen.Dispensers.route) { /* TODO: Dispensers Screen */ }
        composable(Screen.Alerts.route) { /* TODO: Alerts Screen */ }
        composable(Screen.Settings.route) { /* TODO: Settings Screen */ }
        
        composable(Screen.DetalleMedicamento.route) { backStackEntry ->
            val medicamentoId = backStackEntry.arguments?.getString("medicamentoId") ?: ""
            DetalleMedicamentoScreen(
                medicamentoId = medicamentoId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
