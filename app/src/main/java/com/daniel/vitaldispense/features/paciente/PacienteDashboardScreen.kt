package com.daniel.vitaldispense.features.paciente

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class EstadoPaciente(
    val cama: String,
    val nombre: String,
    val proximaDosis: String,
    val estado: String,
    val esCritico: Boolean = false
)

@Composable
fun PacienteDashboardScreen(
    onPacienteClick: (String) -> Unit
) {
    val pacientesCamas = listOf(
        EstadoPaciente("101", "Juan Perez", "08:00 AM", "Completado"),
        EstadoPaciente("102", "Maria Garcia", "02:15 PM", "Pendiente", esCritico = true),
        EstadoPaciente("105", "Vacio", "-", "Disponible"),
        EstadoPaciente("110", "Carlos Ruiz", "01:00 PM", "Retrasado", esCritico = true),
        EstadoPaciente("112", "Elena Solis", "04:00 PM", "Pendiente")
    )

    Scaffold(
        containerColor = Color(0xFFF8F9FB)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header de Estación de Enfermería
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = "Monitor de Pacientes",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A1C1E)
                    )
                    Text(
                        text = "Sala B - Ala Norte",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1976D2),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Resumen de la Sala (Cuadricula limpia)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // CORRECCIÓN: Se especifican start, end y bottom por separado
                        .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ResumenSalaCard("Activos", "4", Color(0xFF1976D2), Modifier.weight(1f))
                    ResumenSalaCard("Alertas", "2", Color(0xFFD32F2F), Modifier.weight(1f))
                    ResumenSalaCard("Libres", "1", Color(0xFF4CAF50), Modifier.weight(1f))
                }
            }

            item {
                Text(
                    text = "Lista de Camas",
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(pacientesCamas) { pac ->
                PacienteCamaItem(pac, onClick = { onPacienteClick(pac.cama) })
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun PacienteCamaItem(pac: EstadoPaciente, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 6.dp)
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
            // Indicador de Cama estilizado
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        if (pac.esCritico) Color(0xFFFFEBEE) else Color(0xFFF0F7FF),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pac.cama,
                    fontWeight = FontWeight.Bold,
                    color = if (pac.esCritico) Color(0xFFD32F2F) else Color(0xFF1976D2)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pac.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = if (pac.proximaDosis == "-") "Sin rondas" else "Próxima: ${pac.proximaDosis}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                if (pac.esCritico) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = pac.estado,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = when (pac.estado) {
                        "Retrasado" -> Color(0xFFD32F2F)
                        "Completado" -> Color(0xFF2E7D32)
                        "Disponible" -> Color(0xFF4CAF50)
                        else -> Color(0xFFFFA726)
                    }
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}

@Composable
fun ResumenSalaCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}
