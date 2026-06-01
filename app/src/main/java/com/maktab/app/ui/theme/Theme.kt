package com.maktab.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Teal10, onPrimary = Color.White, primaryContainer = TealContainer, onPrimaryContainer = Teal10,
    secondary = Blue10, onSecondary = Color.White, secondaryContainer = BlueContainer, onSecondaryContainer = Blue10,
    background = Surface, onBackground = OnSurface,
    surface = Color.White, onSurface = OnSurface,
    surfaceVariant = SurfaceVariant, onSurfaceVariant = OnSurfaceVariant, outline = Outline,
)

private val DarkColors = darkColorScheme(
    primary = Teal20, onPrimary = Color.Black, primaryContainer = Color(0xFF003D2B), onPrimaryContainer = Teal20,
    secondary = Color(0xFF5B9BD5), onSecondary = Color.Black,
    secondaryContainer = Color(0xFF0D2E50), onSecondaryContainer = Color(0xFF5B9BD5),
    background = DarkBg, onBackground = Color(0xFFDFE4E1),
    surface = DarkSurf, onSurface = Color(0xFFDFE4E1),
    surfaceVariant = DarkSurfV, onSurfaceVariant = Color(0xFF8FA39E), outline = DarkOutline,
)

@Composable
fun MaktabTheme(isDark: Boolean = false, language: String = "uz", content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalIsDark provides isDark,
        LocalAppLang provides language
    ) {
        MaterialTheme(
            colorScheme = if (isDark) DarkColors else LightColors,
            typography = Typography(),
            content = content
        )
    }
}
