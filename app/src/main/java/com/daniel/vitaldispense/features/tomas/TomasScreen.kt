package com.daniel.vitaldispense.features.tomas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TomasScreen() {
    val tomas = listOf(
        TomaItem("Paracetamol", "08:00 AM", "Daniel (Yo)", Color(0xFFFFEBEE)),
        TomaItem("Ibuprofeno", "12:00 PM", "María (Madre)", Color(0xFFFFF3E0)),
        TomaItem("Vitamina C", "09:00 PM", "Daniel (Yo)", Color(0xFFE8F5E9)),
        TomaItem("Aspirina", "10:00 PM", "Carlos (Padre)", Color(0xFFE3F2FD))
    )

    Scaffold(
        containerColor = Color(0xFFF7F9FC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = "Tomas Próximas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(tomas) { toma ->
                    TomaDetailedCard(toma)
                }
            }
        }
    }
}

@Composable
fun TomaDetailedCard(toma: TomaItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    .size(52.dp)
                    .background(toma.bgColor, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "💊", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = toma.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = toma.paciente,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Surface(
                color = Color(0xFFF0F7FF),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = toma.hora,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
            }
        }
    }
}

data class TomaItem(val nombre: String, val hora: String, val paciente: String, val bgColor: Color)
