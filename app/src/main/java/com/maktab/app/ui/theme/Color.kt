package com.maktab.app.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// Light colors
val Teal10 = Color(0xFF0F6E56); val Teal20 = Color(0xFF1D9E75); val TealContainer = Color(0xFFE1F5EE)
val Blue10 = Color(0xFF185FA5); val BlueContainer = Color(0xFFE6F1FB)
val Amber10 = Color(0xFFBA7517); val AmberContainer = Color(0xFFFAEEDA)
val Red10 = Color(0xFFA32D2D); val RedContainer = Color(0xFFFCEBEB)
val Green10 = Color(0xFF3B6D11); val GreenContainer = Color(0xFFEAF3DE)
val Purple10 = Color(0xFF534AB7); val PurpleContainer = Color(0xFFEEEDFE)
val Surface = Color(0xFFF8F9FA); val SurfaceVariant = Color(0xFFF1F0EC)
val OnSurface = Color(0xFF1A1A18); val OnSurfaceVariant = Color(0xFF6B6A66); val Outline = Color(0xFFD4D2CC)

// Dark mode extra
val DarkBg     = Color(0xFF111714)
val DarkSurf   = Color(0xFF1A2120)
val DarkSurfV  = Color(0xFF232B29)
val DarkOutline = Color(0xFF2E3835)

// CompositionLocals
val LocalAppLang   = compositionLocalOf { "uz" }
val LocalIsDark    = compositionLocalOf { false }
