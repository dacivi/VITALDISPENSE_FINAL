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
    primary = PrimaryMedicalBlue,
    onPrimary = SurfaceWhite,
    secondary = SecondaryMedicalBlue,
    onSecondary = SurfaceWhite,
    background = BackgroundLight,
    onBackground = TextDark,
    surface = SurfaceWhite,
    onSurface = TextDark,
    error = ErrorRed,
    onError = SurfaceWhite,
    surfaceVariant = BlueSoft,
    onSurfaceVariant = TextGray,
    tertiary = AlertCoral,
    onTertiary = SurfaceWhite
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
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
            window.statusBarColor = AndroidColor.TRANSPARENT
            window.navigationBarColor = AndroidColor.TRANSPARENT
            
            val insetsController = WindowCompat.getInsetsController(window, view)
            // Barra de estado: iconos oscuros porque el fondo suele ser claro (o azul, pero el sistema puede manejarlo)
            // Si el header es azul oscuro, preferiríamos iconos claros (isAppearanceLightStatusBars = false)
            // Si el fondo general es claro, iconos oscuros (isAppearanceLightStatusBars = true)
            insetsController.isAppearanceLightStatusBars = true
            insetsController.isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
