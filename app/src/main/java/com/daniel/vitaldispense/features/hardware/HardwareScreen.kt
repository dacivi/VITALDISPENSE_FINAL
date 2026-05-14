package com.daniel.vitaldispense.features.hardware

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Dispensador(
    val id: String,
    val cama: String,
    val paciente: String,
    val bateria: Int,
    val señal: String,
    val estado: String,
    val alertas: Boolean = false
)

@Composable
fun HardwareScreen() {
    val dispensadores = listOf(
        Dispensador("D-01", "Cama 101", "Juan Perez", 85, "Excelente", "En línea"),
        Dispensador("D-02", "Cama 102", "Maria Garcia", 12, "Media", "Batería Baja", alertas = true),
        Dispensador("D-03", "Cama 105", "Vacio", 100, "N/A", "Standby"),
        Dispensador("D-04", "Cama 110", "Carlos Ruiz", 45, "Baja", "Revisar Conexión", alertas = true)
    )

    Scaffold(
        containerColor = Color(0xFFF8F9FB)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "Gestión de Hardware",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1C1E)
                )
                Text(
                    text = "${dispensadores.size} dispositivos monitoreados",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(dispensadores) { disp ->
                    HardwareCard(disp)
                }
            }
        }
    }
}

@Composable
fun HardwareCard(disp: Dispensador) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = disp.cama, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = "ID: ${disp.id} • ${disp.paciente}", color = Color.Gray, fontSize = 13.sp)
                }
                
                if (disp.alertas) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F))
                } else {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                    )
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusInfo(Icons.Default.BatteryFull, "${disp.bateria}%", if(disp.bateria < 20) Color.Red else Color.Gray)
                StatusInfo(Icons.Default.SignalCellularAlt, disp.señal, Color.Gray)
                Text(
                    text = disp.estado, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 12.sp,
                    color = if(disp.alertas) Color(0xFFD32F2F) else Color(0xFF1976D2)
                )
            }
        }
    }
}

@Composable
fun StatusInfo(icon: ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = color)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, fontSize = 12.sp, color = color)
    }
}
