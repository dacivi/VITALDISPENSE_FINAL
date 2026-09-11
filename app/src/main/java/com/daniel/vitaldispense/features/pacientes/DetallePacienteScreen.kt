package com.daniel.vitaldispense.features.pacientes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Modelo mock detalle ───────────────────────────────────────────────────────

data class DetallePacienteData(
    val id: String,
    val apellidos: String,
    val nombre: String,
    val fechaNacimiento: String,
    val edad: Int,
    val sexo: String,
    val peso: String,
    val talla: String,
    val cama: String,
    val habitacion: String,
    val idDispensador: String,
    val medicoTratante: String,
    val alergias: String?,
    val diagnostico: String,
    val viaAdministracion: String,
    val medicamentos: List<MedicamentoPaciente>
)

data class MedicamentoPaciente(
    val nombre: String,
    val dosis: String,
    val frecuencia: String,
    val compartimento: String,
    val proximaDosis: String
)

// ── Datos mock ────────────────────────────────────────────────────────────────

private fun getMockPaciente(id: String) = DetallePacienteData(
    id = id,
    apellidos = "García López",
    nombre = "María Fernanda",
    fechaNacimiento = "12/03/1978",
    edad = 46,
    sexo = "Femenino",
    peso = "68 kg",
    talla = "162 cm",
    cama = "102",
    habitacion = "Hab. 10",
    idDispensador = "VD-002",
    medicoTratante = "Dr. Rodríguez Vázquez",
    alergias = "Penicilina, AINEs",
    diagnostico = "Diabetes Mellitus Tipo 2 — Control glucémico. Ingresó por descompensación hiperglucémica.",
    viaAdministracion = "Oral",
    medicamentos = listOf(
        MedicamentoPaciente("Metformina 850mg",  "1 tableta",  "Cada 12 horas", "Tolva 1", "08:30 AM"),
        MedicamentoPaciente("Glibenclamida 5mg", "1 tableta",  "Una vez al día", "Tolva 2", "07:00 AM"),
        MedicamentoPaciente("Enalapril 10mg",    "1 tableta",  "Cada 12 horas", "Tolva 3", "08:00 AM")
    )
)

// ── Pantalla ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetallePacienteScreen(
    pacienteId: String,
    onBackClick: () -> Unit
) {
    val paciente = getMockPaciente(pacienteId)
    val tieneAlergia = !paciente.alergias.isNullOrBlank()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {

            // ── Header con gradiente ──────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF1565C0),
                                    Color(0xFF1976D2)
                                )
                            ),
                            RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.background(
                                Color.White.copy(alpha = 0.2f), CircleShape
                            )
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Atrás",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Cama ${paciente.cama} · ${paciente.habitacion}",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${paciente.apellidos}, ${paciente.nombre}",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ── Alerta de alergias (si aplica) ────────────────────────────────
            if (tieneAlergia) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(top = 20.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFD32F2F).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    "⚠ ALERGIAS CONOCIDAS",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = Color(0xFFD32F2F),
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    paciente.alergias ?: "",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF1A1C1E)
                                )
                            }
                        }
                    }
                }
            }

            // ── Sección: Datos personales ─────────────────────────────────────
            item {
                SeccionDetalle(
                    titulo = "Datos Personales",
                    icono = Icons.Default.Person,
                    modifier = Modifier.padding(top = if (tieneAlergia) 20.dp else 24.dp)
                ) {
                    FilaDetalle("Fecha de nacimiento", paciente.fechaNacimiento)
                    FilaDetalle("Edad", "${paciente.edad} años")
                    FilaDetalle("Sexo", paciente.sexo)
                    FilaDetalle("Peso / Talla", "${paciente.peso} · ${paciente.talla}")
                }
            }

            // ── Sección: Ubicación ────────────────────────────────────────────
            item {
                SeccionDetalle(
                    titulo = "Ubicación y Asignación",
                    icono = Icons.Default.Bed
                ) {
                    FilaDetalle("Habitación", paciente.habitacion)
                    FilaDetalle("Cama", paciente.cama)
                    FilaDetalle("Dispensador IoT", paciente.idDispensador)
                    FilaDetalle("Médico tratante", paciente.medicoTratante)
                }
            }

            // ── Sección: Info clínica ─────────────────────────────────────────
            item {
                SeccionDetalle(
                    titulo = "Información Clínica",
                    icono = Icons.Default.LocalHospital
                ) {
                    FilaDetalle("Diagnóstico", paciente.diagnostico, multilinea = true)
                    FilaDetalle("Vía de administración", paciente.viaAdministracion)
                }
            }

            // ── Sección: Medicamentos ─────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Medication,
                        contentDescription = null,
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Esquema de Medicación",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1A1C1E)
                    )
                }
            }

            items(paciente.medicamentos.size) { index ->
                val med = paciente.medicamentos[index]
                MedicamentoCard(med)
            }
        }
    }
}

// ── Componentes ───────────────────────────────────────────────────────────────

@Composable
fun SeccionDetalle(
    titulo: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFF0F7FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icono,
                        contentDescription = null,
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1976D2)
                )
            }
            contenido()
        }
    }
}

@Composable
fun FilaDetalle(label: String, valor: String, multilinea: Boolean = false) {
    Column(modifier = Modifier.padding(vertical = 5.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
        Text(
            valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1C1E),
            softWrap = multilinea,
            maxLines = if (multilinea) Int.MAX_VALUE else 1
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            color = Color(0xFFF0F0F0)
        )
    }
}

@Composable
fun MedicamentoCard(med: MedicamentoPaciente) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 5.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFF0F7FF), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MedicalServices,
                    contentDescription = null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    med.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1A1C1E)
                )
                Text(
                    "${med.dosis} · ${med.frecuencia}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    ChipInfo(med.compartimento, Color(0xFF7B1FA2), Color(0xFFF3E5F5))
                    ChipInfo("Próx: ${med.proximaDosis}", Color(0xFF1976D2), Color(0xFFF0F7FF))
                }
            }
        }
    }
}

@Composable
fun ChipInfo(texto: String, colorTexto: Color, colorFondo: Color) {
    Surface(
        color = colorFondo,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = colorTexto
        )
    }
}
