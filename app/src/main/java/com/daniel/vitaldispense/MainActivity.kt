package com.daniel.vitaldispense

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
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
import com.daniel.vitaldispense.navigation.Screen
import com.daniel.vitaldispense.ui.theme.TealMedical
import com.daniel.vitaldispense.ui.theme.VITALDISPENSE_FINALTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)

        createNotificationChannel()

        setContent {
            VITALDISPENSE_FINALTheme {
                MainContent()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alertas de VitalDispense"
            val descriptionText = "Canal para notificaciones de toma de medicamentos y alertas del dispensador"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("vital_alerts_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun MainContent() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    
    val startDest = if (auth.currentUser != null) Screen.Home.route else Screen.Login.route

    val items = listOf(
        BottomNavItem("Home", Screen.Home.route, icon = Icons.Default.Home),
        BottomNavItem("Pacientes", Screen.Patients.route, icon = Icons.Default.People),
        BottomNavItem("Hardware", Screen.Dispensers.route, isCentral = true),
        BottomNavItem("Alertas", Screen.Alerts.route, icon = Icons.Default.Notifications),
        BottomNavItem("Ajustes", Screen.Settings.route, icon = Icons.Default.Settings),
    )

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                val isAuthScreen = currentDestination?.route == Screen.Login.route || 
                                  currentDestination?.route == Screen.Register.route
                
                val showBottomBar = !isAuthScreen && items.any { it.route == currentDestination?.route }

                if (showBottomBar) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(start = 24.dp, end = 24.dp, bottom = 20.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp),
                            shape = RoundedCornerShape(50.dp),
                            color = TealMedical,
                            tonalElevation = 8.dp,
                            shadowElevation = 10.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items.forEach { item ->
                                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                                    
                                    val scale by animateFloatAsState(
                                        targetValue = if (selected) 1.2f else 1.0f,
                                        animationSpec = tween(durationMillis = 300),
                                        label = "scale"
                                    )

                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(CircleShape)
                                    ) {
                                        if (selected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(if (item.isCentral) 54.dp else 44.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.2f))
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                if (!selected) {
                                                    navController.navigate(item.route) {
                                                        popUpTo(navController.graph.findStartDestination().id) {
                                                            saveState = true
                                                        }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            },
                                            modifier = Modifier.scale(if (item.isCentral) scale * 1.1f else scale)
                                        ) {
                                            if (item.isCentral) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_dispensador),
                                                    contentDescription = item.name,
                                                    tint = Color.Unspecified,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = item.icon!!,
                                                    contentDescription = item.name,
                                                    tint = if (selected) Color.White else Color.White.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                NavHost(
                    navController = navController,
                    startDestination = startDest,
                    modifier = Modifier
                        .padding(innerPadding)
                        .statusBarsPadding()
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
                    composable(Screen.Home.route) { HomeScreen() }
                    composable(Screen.Patients.route) {
                        PacienteDashboardScreen(onMedicamentoClick = { medicamento ->
                            navController.navigate(Screen.DetalleMedicamento.createRoute(medicamento.id))
                        })
                    }
                    composable(Screen.Dispensers.route) { /* TODO */ }
                    composable(Screen.Alerts.route) { NotificationsScreen() }
                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            onLogout = {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
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
        }
    }
}

data class BottomNavItem(
    val name: String, 
    val route: String, 
    val icon: ImageVector? = null,
    val isCentral: Boolean = false
)
