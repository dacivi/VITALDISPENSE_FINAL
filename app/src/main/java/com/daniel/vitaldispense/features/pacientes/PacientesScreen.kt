package com.daniel.vitaldispense.features.pacientes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Modelos mock ──────────────────────────────────────────────────────────────

data class PacienteLista(
    val id: String,
    val nombre: String,
    val cama: String,
    val habitacion: String,
    val diagnostico: String,
    val proximaDosis: String,
    val estado: EstadoPaciente,
    val tieneAlergia: Boolean = false
)

enum class EstadoPaciente { ACTIVO, CRITICO, DISPONIBLE }

// ── Pantalla ──────────────────────────────────────────────────────────────────

@Composable
fun PacientesScreen(
    onPacienteClick: (String) -> Unit,
    onAgregarPaciente: () -> Unit
) {
    var busqueda by remember { mutableStateOf("") }

    val pacientes = listOf(
        PacienteLista("1", "Juan Pérez",    "101", "Hab. 10", "Hipertensión arterial",    "10:00 AM", EstadoPaciente.ACTIVO),
        PacienteLista("2", "María García",  "102", "Hab. 10", "Diabetes Mellitus Tipo 2", "08:30 AM", EstadoPaciente.CRITICO, tieneAlergia = true),
        PacienteLista("3", "Elena Solís",   "104", "Hab. 11", "Arritmia cardíaca",         "09:00 AM", EstadoPaciente.ACTIVO),
        PacienteLista("4", "Roberto Díaz",  "106", "Hab. 11", "Post-operatorio rodilla",   "10:00 AM", EstadoPaciente.ACTIVO),
        PacienteLista("5", "Carlos Ruiz",   "110", "Hab. 12", "Insuficiencia renal",       "11:00 AM", EstadoPaciente.CRITICO, tieneAlergia = true),
        PacienteLista("6", "Sofía Martín",  "112", "Hab. 12", "Neumonía bacteriana",       "12:00 PM", EstadoPaciente.ACTIVO),
        PacienteLista("7", "Luis Hernández","115", "Hab. 13", "Fractura de cadera",        "02:00 PM", EstadoPaciente.ACTIVO),
        PacienteLista("8", "Carmen Vega",   "118", "Hab. 13", "Gastroenteritis aguda",     "03:00 PM", EstadoPaciente.ACTIVO)
    )

    val pacientesFiltrados = remember(busqueda) {
        if (busqueda.isBlank()) pacientes
        else pacientes.filter {
            it.nombre.contains(busqueda, ignoreCase = true) ||
            it.cama.contains(busqueda, ignoreCase = true) ||
            it.diagnostico.contains(busqueda, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F6FA),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAgregarPaciente,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo Paciente", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {

            // ── Header ────────────────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = "Pacientes",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A1C1E)
                    )
                    Text(
                        text = "${pacientes.size} pacientes · ${pacientes.count { it.estado == EstadoPaciente.CRITICO }} críticos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1976D2),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ── Buscador ──────────────────────────────────────────────────────
            item {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    placeholder = { Text("Buscar por nombre, cama o diagnóstico…") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )
            }

            // ── Filtros rápidos ───────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FiltroChip(
                        label = "Todos",
                        count = pacientes.size,
                        activo = busqueda.isEmpty()
                    ) { busqueda = "" }
                    FiltroChip(
                        label = "Críticos",
                        count = pacientes.count { it.estado == EstadoPaciente.CRITICO },
                        activo = false,
                        color = Color(0xFFD32F2F)
                    ) { busqueda = "" }
                    FiltroChip(
                        label = "Con alergias",
                        count = pacientes.count { it.tieneAlergia },
                        activo = false,
                        color = Color(0xFFE65100)
                    ) { busqueda = "" }
                }
            }

            // ── Lista de pacientes ────────────────────────────────────────────
            if (pacientesFiltrados.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Sin resultados", color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(
                            "Intenta con otro nombre o cama",
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                items(pacientesFiltrados) { paciente ->
                    PacienteCard(
                        paciente = paciente,
                        onClick = { onPacienteClick(paciente.id) }
                    )
                }
            }
        }
    }
}

// ── Componentes ───────────────────────────────────────────────────────────────

@Composable
fun PacienteCard(paciente: PacienteLista, onClick: () -> Unit) {
    val (camaColor, camaBg) = when (paciente.estado) {
        EstadoPaciente.CRITICO    -> Color(0xFFD32F2F) to Color(0xFFFFEBEE)
        EstadoPaciente.ACTIVO     -> Color(0xFF1976D2) to Color(0xFFF0F7FF)
        EstadoPaciente.DISPONIBLE -> Color(0xFF4CAF50) to Color(0xFFE8F5E9)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 5.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge de cama
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(camaBg, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = paciente.cama,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = camaColor
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = paciente.nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1A1C1E)
                    )
                    if (paciente.tieneAlergia) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(Color(0xFFFFEBEE), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Tiene alergias",
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
                Text(
                    text = paciente.diagnostico,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFF0F7FF),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = paciente.habitacion,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            color = Color(0xFF1976D2),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Próx: ${paciente.proximaDosis}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun FiltroChip(
    label: String,
    count: Int,
    activo: Boolean,
    color: Color = Color(0xFF1976D2),
    onClick: () -> Unit
) {
    val bgColor = if (activo) color.copy(alpha = 0.12f) else Color.White
    val textColor = if (activo) color else Color.Gray
    val borderColor = if (activo) color.copy(alpha = 0.4f) else Color.LightGray

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = textColor)
            Surface(
                color = color.copy(alpha = 0.15f),
                shape = CircleShape
            ) {
                Text(
                    text = count.toString(),
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}
