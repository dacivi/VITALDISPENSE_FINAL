package com.daniel.vitaldispense.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsScreen(
    onLogout: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF7F9FC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Título grande integrado (Imagen 5)
            Text(
                text = "Ajustes",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp)
            )

            // Sección de Perfil (Avatar Azul y Email)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1976D2)), // Azul médico primario
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = user?.email ?: "usuario@ejemplo.com",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF455A64),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Lista de Opciones como Tarjetas (Imagen 5)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsCard(
                    icon = Icons.Default.Person,
                    title = "Mi Perfil",
                    iconColor = Color(0xFF1976D2),
                    iconBgColor = Color(0xFFF0F7FF)
                ) { /* Navegar a perfil */ }

                SettingsCard(
                    icon = Icons.Default.Notifications,
                    title = "Notificaciones",
                    iconColor = Color(0xFF1976D2),
                    iconBgColor = Color(0xFFF0F7FF)
                ) { /* Navegar a notificaciones */ }

                SettingsCard(
                    icon = Icons.Default.ExitToApp,
                    title = "Cerrar Sesión",
                    iconColor = Color(0xFFD32F2F),
                    iconBgColor = Color(0xFFFFEBEE),
                    textColor = Color(0xFFD32F2F)
                ) {
                    showLogoutDialog = true
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("¿Cerrar sesión?") },
                text = { Text("Tendrás que volver a ingresar para ver tus recordatorios.") },
                confirmButton = {
                    TextButton(onClick = { 
                        showLogoutDialog = false
                        auth.signOut()
                        onLogout()
                    }) {
                        Text("SÍ, SALIR", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("CANCELAR")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsCard(
    icon: ImageVector,
    title: String,
    iconColor: Color,
    iconBgColor: Color,
    textColor: Color = Color(0xFF1A1C1E),
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}
