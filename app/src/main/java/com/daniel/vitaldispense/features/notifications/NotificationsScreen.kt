package com.daniel.vitaldispense.features.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daniel.vitaldispense.ui.theme.TealMedical

data class NotificationItem(
    val id: Int,
    val title: String,
    val message: String,
    val time: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen() {
    // Ejemplo de datos para la visualización
    val notifications = listOf(
        NotificationItem(1, "Dosis completada", "El paciente ha tomado su dosis de Paracetamol correctamente.", "Hace 5 min"),
        NotificationItem(2, "Alerta de inventario", "Quedan pocas unidades de Omeprazol en el dispensador.", "Hace 1 hora"),
        NotificationItem(3, "Nueva invitación", "Tu invitación para el Dr. García ha sido aceptada.", "Hace 2 horas"),
        NotificationItem(4, "Recordatorio", "Es hora de revisar el estado de conexión del dispositivo.", "Hace 5 horas")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            EmptyNotificationsView(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp) // Espacio para la barra flotante
            ) {
                items(notifications) { notification ->
                    NotificationCard(notification)
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: NotificationItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de campana con fondo TealMedical circular
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(TealMedical.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = TealMedical,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = notification.time,
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Top)
            )
        }
    }
}

@Composable
fun EmptyNotificationsView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Todo en orden por aquí",
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        Text(
            text = "No tienes notificaciones pendientes.",
            fontSize = 14.sp,
            color = Color.LightGray
        )
    }
}
