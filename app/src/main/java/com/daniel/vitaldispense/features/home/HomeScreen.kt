package com.daniel.vitaldispense.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Modelos mock ─────────────────────────────────────────────────────────────

data class AlertaCama(
    val cama: String,
    val paciente: String,
    val mensaje: String,
    val esCritica: Boolean = false
)

data class ProximaDosis(
    val hora: String,
    val cama: String,
    val paciente: String,
    val medicamento: String,
    val estado: EstadoDosis
)

enum class EstadoDosis { PENDIENTE, RETRASADA, COMPLETADA }

// ── Pantalla principal ────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    onNotificationsClick: () -> Unit = {}
) {
    val alertas = listOf(
        AlertaCama("102", "María García", "Dosis retrasada 45 min", esCritica = true),
        AlertaCama("110", "Carlos Ruiz", "Dosis retrasada 20 min", esCritica = true),
        AlertaCama("107", "Ana López", "Stock bajo: Omeprazol", esCritica = false)
    )

    val proximasDosis = listOf(
        ProximaDosis("08:00", "101", "Juan Pérez",    "Paracetamol 500mg",  EstadoDosis.COMPLETADA),
        ProximaDosis("08:30", "102", "María García",  "Metformina 850mg",   EstadoDosis.RETRASADA),
        ProximaDosis("09:00", "104", "Elena Solís",   "Amlodipino 5mg",     EstadoDosis.PENDIENTE),
        ProximaDosis("10:00", "106", "Roberto Díaz",  "Ibuprofeno 400mg",   EstadoDosis.PENDIENTE),
        ProximaDosis("11:00", "110", "Carlos Ruiz",   "Losartán 50mg",      EstadoDosis.RETRASADA),
        ProximaDosis("12:00", "112", "Sofía Martín",  "Vitamina D 1000UI",  EstadoDosis.PENDIENTE)
    )

    Scaffold(containerColor = Color(0xFFF5F6FA)) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

            // ── Header ────────────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Estación de Enfermería",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A1C1E)
                        )
                        Text(
                            text = "Sala B · Ala Norte · Turno Matutino",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1976D2),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    BadgedBox(
                        badge = {
                            Badge(containerColor = Color(0xFFD32F2F)) {
                                Text("${alertas.count { it.esCritica }}")
                            }
                        }
                    ) {
                        IconButton(
                            onClick = onNotificationsClick,
                            modifier = Modifier.background(Color.White, CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notificaciones",
                                tint = Color(0xFF1976D2)
                            )
                        }
                    }
                }
            }

            // ── Resumen rápido ────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ResumenCard(
                        label = "Pacientes",
                        valor = "8",
                        color = Color(0xFF1976D2),
                        modifier = Modifier.weight(1f)
                    )
                    ResumenCard(
                        label = "Alertas",
                        valor = "${alertas.count { it.esCritica }}",
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.weight(1f)
                    )
                    ResumenCard(
                        label = "Completadas",
                        valor = "${proximasDosis.count { it.estado == EstadoDosis.COMPLETADA }}",
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )
                    ResumenCard(
                        label = "Pendientes",
                        valor = "${proximasDosis.count { it.estado == EstadoDosis.PENDIENTE }}",
                        color = Color(0xFFFFA726),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Alertas críticas ──────────────────────────────────────────────
            if (alertas.isNotEmpty()) {
                item {
                    SectionHeader(
                        titulo = "Alertas Activas",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )
                }
                items(alertas) { alerta ->
                    AlertaCamaCard(alerta)
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            // ── Próximas dosis ────────────────────────────────────────────────
            item {
                SectionHeader(
                    titulo = "Ronda de Medicación",
                    modifier = Modifier.padding(
                        start = 24.dp, end = 24.dp,
                        top = 12.dp, bottom = 4.dp
                    )
                )
            }
            items(proximasDosis) { dosis ->
                ProximaDosisCard(dosis)
            }
        }
    }
}

// ── Componentes ───────────────────────────────────────────────────────────────

@Composable
fun ResumenCard(label: String, valor: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(valor, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun AlertaCamaCard(alerta: AlertaCama) {
    val bgColor = if (alerta.esCritica) Color(0xFFFFEBEE) else Color(0xFFFFF8E1)
    val borderColor = if (alerta.esCritica) Color(0xFFD32F2F) else Color(0xFFFFA726)
    val textColor = if (alerta.esCritica) Color(0xFFD32F2F) else Color(0xFFF57F17)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(borderColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = borderColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Cama ${alerta.cama} · ${alerta.paciente}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1A1C1E)
                )
                Text(
                    text = alerta.mensaje,
                    fontSize = 12.sp,
                    color = textColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ProximaDosisCard(dosis: ProximaDosis) {
    val (bgColor, estadoColor, estadoLabel) = when (dosis.estado) {
        EstadoDosis.COMPLETADA -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Completada")
        EstadoDosis.RETRASADA  -> Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), "Retrasada")
        EstadoDosis.PENDIENTE  -> Triple(Color(0xFFF0F7FF), Color(0xFF1976D2), "Pendiente")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hora
            Surface(
                color = Color(0xFFF0F7FF),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = dosis.hora,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = Color(0xFF1976D2)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dosis.medicamento,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1A1C1E)
                )
                Text(
                    text = "Cama ${dosis.cama} · ${dosis.paciente}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Surface(
                color = bgColor,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = estadoLabel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = estadoColor
                )
            }
        }
    }
}

@Composable
fun SectionHeader(titulo: String, modifier: Modifier = Modifier) {
    Text(
        text = titulo,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1A1C1E)
    )
}
