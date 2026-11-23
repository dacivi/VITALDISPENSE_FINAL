package com.daniel.vitaldispense.features.home

import com.daniel.vitaldispense.ui.theme.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

// Borde sutil clínico para tarjetas planas (estilo HealthTech limpio)
private val CardBorder = Color(0xFFE6E9EE)

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

    val alertasCriticasCount = alertas.count { it.esCritica }

    Scaffold(containerColor = BackgroundSecondary) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 104.dp)
        ) {

            // ── Header ────────────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Estación de Enfermería",
                            fontSize = 23.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.4).sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(BrandPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sala B · Ala Norte · Turno Matutino",
                                fontSize = 13.sp,
                                color = BrandPrimary,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    BadgedBox(
                        badge = {
                            if (alertasCriticasCount > 0) {
                                Badge(
                                    containerColor = ErrorRed,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = "$alertasCriticasCount",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = onNotificationsClick,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, CardBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificaciones",
                                tint = BrandPrimary,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }
                }
            }

            // ── Resumen rápido (KPIs) ─────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 22.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ResumenCard(
                        label = "Pacientes",
                        valor = "8",
                        color = BrandPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    ResumenCard(
                        label = "Alertas",
                        valor = "$alertasCriticasCount",
                        color = ErrorRed,
                        modifier = Modifier.weight(1f)
                    )
                    ResumenCard(
                        label = "Concluido",
                        valor = "${proximasDosis.count { it.estado == EstadoDosis.COMPLETADA }}",
                        color = SuccessDark,
                        modifier = Modifier.weight(1f)
                    )
                    ResumenCard(
                        label = "Pendientes",
                        valor = "${proximasDosis.count { it.estado == EstadoDosis.PENDIENTE }}",
                        color = WarningOrange,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Alertas críticas ──────────────────────────────────────────────
            if (alertas.isNotEmpty()) {
                item {
                    SectionHeader(
                        titulo = "Alertas Activas",
                        contador = "${alertas.size}",
                        contadorBg = ErrorSoft,
                        contadorColor = ErrorRed,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
                items(alertas) { alerta ->
                    AlertaCamaCard(alerta)
                }
                item { Spacer(modifier = Modifier.height(14.dp)) }
            }

            // ── Próximas dosis ────────────────────────────────────────────────
            item {
                SectionHeader(
                    titulo = "Ronda de Medicación",
                    contador = "${proximasDosis.size} dosis",
                    contadorBg = PrimarySoft,
                    contadorColor = BrandPrimary,
                    modifier = Modifier.padding(
                        start = 20.dp, end = 20.dp,
                        top = 6.dp, bottom = 6.dp
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
fun ResumenCard(
    label: String,
    valor: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Indicador superior minimalista
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.85f))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = valor,
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AlertaCamaCard(alerta: AlertaCama) {
    val bgColor = if (alerta.esCritica) ErrorSoft else WarningSoft
    val accentColor = if (alerta.esCritica) ErrorRed else WarningOrange
    val textColor = if (alerta.esCritica) ErrorRed else WarningDark
    val tagLabel = if (alerta.esCritica) "Crítica" else "Aviso"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.22f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.85f))
                    .border(1.dp, accentColor.copy(alpha = 0.18f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Cama ${alerta.cama} · ${alerta.paciente}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = alerta.mensaje,
                    fontSize = 12.sp,
                    color = textColor,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = Color.White.copy(alpha = 0.85f),
                shape = CircleShape,
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
            ) {
                Text(
                    text = tagLabel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun ProximaDosisCard(dosis: ProximaDosis) {
    val (bgColor, estadoColor, estadoLabel) = when (dosis.estado) {
        EstadoDosis.COMPLETADA -> Triple(SuccessSoft, SuccessDark, "Completada")
        EstadoDosis.RETRASADA  -> Triple(ErrorSoft, ErrorRed, "Retrasada")
        EstadoDosis.PENDIENTE  -> Triple(PrimarySoft, BrandPrimary, "Pendiente")
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bloque horario con identidad BrandPrimary
            Surface(
                color = PrimarySoft,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BrandPrimary.copy(alpha = 0.12f))
            ) {
                Text(
                    text = dosis.hora,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = BrandPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dosis.medicamento,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Cama ${dosis.cama} · ${dosis.paciente}",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Píldora de estado con micro-indicador circular
            Surface(
                color = bgColor,
                shape = CircleShape,
                border = BorderStroke(1.dp, estadoColor.copy(alpha = 0.18f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    when (dosis.estado) {
                        EstadoDosis.COMPLETADA -> {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(estadoColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                        EstadoDosis.RETRASADA -> {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(estadoColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                        EstadoDosis.PENDIENTE -> {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(estadoColor)
                            )
                        }
                    }

                    Text(
                        text = estadoLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = estadoColor
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    titulo: String,
    contador: String? = null,
    contadorBg: Color = PrimarySoft,
    contadorColor: Color = BrandPrimary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = titulo,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        if (contador != null) {
            Surface(
                color = contadorBg,
                shape = CircleShape
            ) {
                Text(
                    text = contador,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = contadorColor
                )
            }
        }
    }
}