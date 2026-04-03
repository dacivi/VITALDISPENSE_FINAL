package com.daniel.vitaldispense.features.paciente

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daniel.vitaldispense.data.model.Medicamento

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacienteDashboardScreen(
    onMedicamentoClick: (Medicamento) -> Unit
) {
    val medicamentosHoy = listOf(
        Medicamento("1", "Paracetamol", "500mg", "08:00 AM", true, "💊"),
        Medicamento("2", "Ibuprofeno", "400mg", "02:00 PM", false, "💊"),
        Medicamento("3", "Vitamina C", "1g", "09:00 PM", false, "🍊")
    )

    val proximaToma = medicamentosHoy.firstOrNull { !it.tomado } ?: medicamentosHoy.first()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vital Dispense", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* Perfil */ }) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "¡Hola, Juan!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Aquí tienes tu resumen de hoy",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }

            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PRÓXIMA TOMA", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = proximaToma.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(text = "A las ${proximaToma.horario} • ${proximaToma.dosis}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            item {
                Text(text = "Medicamentos de Hoy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            items(medicamentosHoy) { medicamento ->
                MedicamentoItem(medicamento = medicamento, onClick = { onMedicamentoClick(medicamento) })
            }
        }
    }
}

@Composable
fun MedicamentoItem(medicamento: Medicamento, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (medicamento.tomado) Color(0xFFE8F5E9) else Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = medicamento.icono, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) { // CORRECCIÓN: Usar 1f en lugar de 1.dp
                Text(text = medicamento.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "${medicamento.horario} • ${medicamento.dosis}", style = MaterialTheme.typography.bodySmall)
            }
            if (medicamento.tomado) {
                Text("Tomado", color = Color(0xFF2E7D32), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}
