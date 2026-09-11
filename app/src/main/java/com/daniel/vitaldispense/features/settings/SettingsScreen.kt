package com.daniel.vitaldispense.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
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
    onLogout: () -> Unit,
    onInvitacionesClick: () -> Unit = {}
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Obtener nombre del usuario (antes del @)
    val nombreUsuario = user?.email
        ?.substringBefore("@")
        ?.replaceFirstChar { it.uppercase() }
        ?: "Enfermero/a"

    Scaffold(containerColor = Color(0xFFF5F6FA)) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Título ────────────────────────────────────────────────────────
            Text(
                text = "Perfil",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)
            )

            // ── Card de perfil del turno ──────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1976D2)
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = nombreUsuario,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user?.email ?: "",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Turno Matutino · Sala B",
                                modifier = Modifier.padding(
                                    horizontal = 10.dp, vertical = 4.dp
                                ),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Sección: Mi Cuenta ────────────────────────────────────────────
            SectionLabel(
                texto = "Mi Cuenta",
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 10.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SettingsCard(
                    icon = Icons.Default.Person,
                    title = "Mi Perfil",
                    subtitle = "Editar nombre y datos",
                    iconColor = Color(0xFF1976D2),
                    iconBgColor = Color(0xFFF0F7FF)
                ) { /* TODO: navegar a editar perfil */ }

                SettingsCard(
                    icon = Icons.Default.Schedule,
                    title = "Mi Turno",
                    subtitle = "Sala B · Ala Norte · Matutino",
                    iconColor = Color(0xFF1976D2),
                    iconBgColor = Color(0xFFF0F7FF)
                ) { /* TODO: cambiar turno */ }

                SettingsCard(
                    icon = Icons.Default.Notifications,
                    title = "Notificaciones",
                    subtitle = "Alertas y recordatorios",
                    iconColor = Color(0xFF1976D2),
                    iconBgColor = Color(0xFFF0F7FF)
                ) { /* TODO: navegar a notificaciones */ }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Sección: Administración ───────────────────────────────────────
            SectionLabel(
                texto = "Administración",
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 10.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SettingsCard(
                    icon = Icons.Default.Mail,
                    title = "Invitaciones",
                    subtitle = "Gestionar acceso al equipo",
                    iconColor = Color(0xFF7B1FA2),
                    iconBgColor = Color(0xFFF3E5F5)
                ) { onInvitacionesClick() }

                SettingsCard(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    title = "Cerrar Sesión",
                    subtitle = "Finalizar turno activo",
                    iconColor = Color(0xFFD32F2F),
                    iconBgColor = Color(0xFFFFEBEE),
                    textColor = Color(0xFFD32F2F)
                ) { showLogoutDialog = true }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // ── Diálogo de cierre de sesión ───────────────────────────────────────
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("¿Cerrar sesión?") },
                text = {
                    Text("Se cerrará tu turno activo. Tendrás que volver a iniciar sesión para acceder.")
                },
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

// ── Componentes ───────────────────────────────────────────────────────────────

@Composable
fun SectionLabel(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        modifier = modifier,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        letterSpacing = 0.5.sp
    )
}

@Composable
fun SettingsCard(
    icon: ImageVector,
    title: String,
    subtitle: String = "",
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
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}
