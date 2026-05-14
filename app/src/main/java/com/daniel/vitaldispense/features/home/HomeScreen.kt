package com.daniel.vitaldispense.features.home

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen() {
    val proximasTomas = listOf(
        TomaMock("Paracetamol", "08:00 AM", "Daniel (Yo)", Color(0xFFE3F2FD), Color(0xFF1976D2)),
        TomaMock("Ibuprofeno", "12:00 PM", "María (Madre)", Color(0xFFFFF3E0), Color(0xFFFFA726)),
        TomaMock("Vitamina C", "09:00 PM", "Daniel (Yo)", Color(0xFFE8F5E9), Color(0xFF66BB6A))
    )

    Scaffold(
        containerColor = Color(0xFFF8F9FB)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header Compacto y Clean
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
                            text = "Hola, Daniel",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A1C1E)
                        )
                        Text(
                            text = "Tu salud está al día",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                    IconButton(
                        onClick = { },
                        modifier = Modifier.background(Color.White, CircleShape)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF1976D2))
                    }
                }
            }

            // Card de Adherencia Minimalista
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = 0.92f,
                                modifier = Modifier.fillMaxSize(),
                                strokeWidth = 6.dp,
                                color = Color(0xFF1976D2),
                                trackColor = Color(0xFFE3F2FD)
                            )
                            Text("92%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Buen progreso", fontWeight = FontWeight.Bold)
                            Text("Has cumplido con casi todas tus dosis", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Próximas tomas",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(proximasTomas) { toma ->
                CleanTomaCard(toma)
            }
            
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun CleanTomaCard(toma: TomaMock) {
    Card(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(toma.bgColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "💊", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(toma.nombre, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(toma.paciente, fontSize = 12.sp, color = Color.Gray)
            }
            Text(
                text = toma.hora,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF1976D2)
            )
        }
    }
}

data class TomaMock(val nombre: String, val hora: String, val paciente: String, val bgColor: Color, val iconColor: Color)
