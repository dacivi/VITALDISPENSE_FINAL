package com.daniel.vitaldispense.ui.theme

import android.app.Activity
import android.graphics.Color as AndroidColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = TealMedical,
    onPrimary = SurfaceWhite,
    secondary = BlueMedical,
    onSecondary = SurfaceWhite,
    background = BackgroundGray,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    error = AlertRed,
    onError = SurfaceWhite,
    surfaceVariant = Color(0xFFE0F2F1), // Teal suave para fondos de tarjetas
    onSurfaceVariant = TextSecondary
)

@Composable
fun VITALDISPENSE_FINALTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Forzar que el contenido se dibuje detrás de las barras del sistema (Edge-to-Edge)
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
            // Hacer que las barras del sistema sean totalmente transparentes
            window.statusBarColor = AndroidColor.TRANSPARENT
            window.navigationBarColor = AndroidColor.TRANSPARENT
            
            // Configurar iconos claros para la barra de estado (blancos)
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            
            // Configurar iconos para la barra de navegación (depende del fondo, pero solemos preferir oscuro si el fondo es claro)
            insetsController.isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
