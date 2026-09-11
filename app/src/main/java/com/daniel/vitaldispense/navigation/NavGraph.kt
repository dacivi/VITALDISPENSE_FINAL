package com.daniel.vitaldispense.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.daniel.vitaldispense.features.auth.LoginScreen
import com.daniel.vitaldispense.features.auth.RegisterScreen
import com.daniel.vitaldispense.features.hardware.HardwareScreen
import com.daniel.vitaldispense.features.home.HomeScreen
import com.daniel.vitaldispense.features.notifications.NotificationsScreen
import com.daniel.vitaldispense.features.pacientes.AgregarPacienteScreen
import com.daniel.vitaldispense.features.pacientes.DetallePacienteScreen
import com.daniel.vitaldispense.features.pacientes.PacientesScreen
import com.daniel.vitaldispense.features.settings.SettingsScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {

        // ── Auth ─────────────────────────────────────────────────────────────
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
                onBackClick = { navController.popBackStack() }
            )
        }

        // ── Tab 1: Inicio / Dashboard ─────────────────────────────────────────
        composable(Screen.Inicio.route) {
            HomeScreen(
                onNotificationsClick = {
                    navController.navigate(Screen.Alertas.route)
                }
            )
        }

        // Notificaciones — accesible desde el header del Home
        composable(Screen.Alertas.route) {
            NotificationsScreen()
        }

        // ── Tab 2: Pacientes ──────────────────────────────────────────────────
        composable(Screen.Pacientes.route) {
            PacientesScreen(
                onPacienteClick = { pacienteId ->
                    navController.navigate(Screen.DetallePaciente.createRoute(pacienteId))
                },
                onAgregarPaciente = {
                    navController.navigate(Screen.AgregarPaciente.route)
                }
            )
        }

        composable(Screen.AgregarPaciente.route) {
            AgregarPacienteScreen(
                onBackClick = { navController.popBackStack() },
                onPacienteGuardado = {
                    navController.navigate(Screen.Pacientes.route) {
                        popUpTo(Screen.Pacientes.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.DetallePaciente.route) { backStackEntry ->
            val pacienteId = backStackEntry.arguments?.getString("pacienteId") ?: ""
            DetallePacienteScreen(
                pacienteId = pacienteId,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ── Tab 3: Dispensador ────────────────────────────────────────────────
        composable(Screen.Dispensador.route) {
            HardwareScreen()
        }

        // ── Tab 4: Perfil / Ajustes ───────────────────────────────────────────
        composable(Screen.Ajustes.route) {
            SettingsScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onInvitacionesClick = {
                    navController.navigate(Screen.Invitaciones.route)
                }
            )
        }

        composable(Screen.Invitaciones.route) {
            // InvitacionesScreen se mantiene, ahora accesible desde Perfil
            com.daniel.vitaldispense.features.invitaciones.InvitacionesScreen()
        }
    }
}
