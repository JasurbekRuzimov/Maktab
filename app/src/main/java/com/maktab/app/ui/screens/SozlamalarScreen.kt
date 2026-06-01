package com.maktab.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maktab.app.ui.theme.*

@Composable
fun SozlamalarScreen(
    role: String,
    isDark: Boolean,
    language: String,
    onToggleDark: () -> Unit,
    onLanguageChange: (String) -> Unit,
    onLogout: () -> Unit
) {
    val isTeacher = role == "teacher"
    val accent = if (isTeacher) Teal10 else Blue10
    val accentContainer = if (isTeacher) TealContainer else BlueContainer

    val roleName = when (language) {
        "ru" -> if (isTeacher) "Учитель" else "Родитель"
        "en" -> if (isTeacher) "Teacher" else "Parent"
        else -> if (isTeacher) "O'qituvchi" else "Ota-ona"
    }
    val settingsTitle = when (language) { "ru" -> "Настройки"; "en" -> "Settings"; else -> "Sozlamalar" }
    val themeTitle    = when (language) { "ru" -> "Внешний вид"; "en" -> "Appearance"; else -> "Ko'rinish" }
    val darkTitle     = when (language) { "ru" -> "Тёмный режим"; "en" -> "Dark Mode"; else -> "Tungi rejim" }
    val langTitle     = when (language) { "ru" -> "Язык"; "en" -> "Language"; else -> "Til" }
    val aboutTitle    = when (language) { "ru" -> "О приложении"; "en" -> "About"; else -> "Ilova haqida" }
    val logoutTitle   = when (language) { "ru" -> "Выйти"; "en" -> "Logout"; else -> "Chiqish" }
    val profileTitle  = when (language) { "ru" -> "Профиль"; "en" -> "Profile"; else -> "Profil" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp, 12.dp)
        ) {
            Text(settingsTitle, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(12.dp))

        // ── Profil ──────────────────────────────────────────────────────
        SettingsSection(profileTitle) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(56.dp).clip(CircleShape).background(accent.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(if (isTeacher) Icons.Default.School else Icons.Default.FamilyRestroom, null, tint = accent, modifier = Modifier.size(28.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(if (isTeacher) "Karimova Nargiza" else "Karimov Bobur", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(roleName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.clip(RoundedCornerShape(4.dp)).background(accentContainer).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text(if (isTeacher) "Matematika o'qituvchisi" else "5-A sinf ota-onasi", fontSize = 11.sp, color = accent)
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── Ko'rinish ────────────────────────────────────────────────────
        SettingsSection(themeTitle) {
            SettingsSwitchRow(
                icon = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                label = darkTitle,
                description = when (language) { "ru" -> "Тёмная тема интерфейса"; "en" -> "Dark interface theme"; else -> "Qorong'i interfeys rejimi" },
                checked = isDark,
                onToggle = onToggleDark,
                accent = accent
            )
        }

        Spacer(Modifier.height(10.dp))

        // ── Til ──────────────────────────────────────────────────────────
        SettingsSection(langTitle) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LanguageButton("🇺🇿", "O'zbekcha", "uz", language, accent) { onLanguageChange("uz") }
                LanguageButton("🇷🇺", "Русский", "ru", language, accent) { onLanguageChange("ru") }
                LanguageButton("🇬🇧", "English", "en", language, accent) { onLanguageChange("en") }
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── Ilova haqida ─────────────────────────────────────────────────
        SettingsSection(aboutTitle) {
            SettingsInfoRow(Icons.Default.Info, "Maktab", "v1.0.0")
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.5f), thickness = 0.5.dp)
            SettingsInfoRow(Icons.Default.Code, when(language){"ru"->"Разработчик";"en"->"Developer";else->"Dasturchi"}, "Jasurbek")
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.5f), thickness = 0.5.dp)
            SettingsInfoRow(Icons.Default.Build, "Android", "Jetpack Compose · Kotlin")
        }

        Spacer(Modifier.height(10.dp))

        // ── Chiqish ──────────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLogout)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Logout, null, tint = Red10, modifier = Modifier.size(20.dp))
                Text(logoutTitle, fontSize = 15.sp, color = Red10, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(0.dp)
        ) { content() }
    }
}

@Composable
private fun SettingsSwitchRow(icon: ImageVector, label: String, description: String, checked: Boolean, onToggle: () -> Unit, accent: Color) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(if (checked) accent.copy(0.15f) else MaterialTheme.colorScheme.surfaceVariant), Alignment.Center) {
            Icon(icon, null, tint = if (checked) accent else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = { onToggle() }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent))
    }
}

@Composable
private fun LanguageButton(flag: String, name: String, code: String, current: String, accent: Color, onClick: () -> Unit) {
    val isSelected = current == code
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) accent.copy(0.1f) else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(flag, fontSize = 22.sp)
        Text(name, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = if (isSelected) accent else MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = accent, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SettingsInfoRow(icon: ImageVector, label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Text(label, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
