package com.maktab.app.ui.screens.common

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.maktab.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SozlamalarScreen(
    role: String,
    isDark: Boolean,
    language: String,
    onToggleDark: () -> Unit,
    onLanguageChange: (String) -> Unit,
    onLogout: () -> Unit,
    fullname: String = "",
    username: String = "",
    academicYear: String = ""
) {
    val context = LocalContext.current
    val prefs = remember { getSecurePrefs(context) }
    val scope = rememberCoroutineScope()

    // Prefs dan ma'lumotlar
    val savedFullname = fullname.ifEmpty { prefs.getString("fullname", "") ?: "" }
    val savedUsername = username.ifEmpty { prefs.getString("username", "") ?: "" }
    val branchName   = prefs.getString("branch", "") ?: ""

    // Ranglar
    val accent = when (role) {
        "teacher" -> Teal10; "chef" -> Amber10; "hr" -> Purple10; "student" -> Blue10; else -> Blue10
    }
    val accentContainer = when (role) {
        "teacher" -> TealContainer; "chef" -> AmberContainer; "hr" -> PurpleContainer; else -> BlueContainer
    }

    // Tarjimalar
    val roleName = when (role) {
        "teacher" -> when (language) { "ru" -> "Учитель"; "en" -> "Teacher"; else -> "O'qituvchi" }
        "chef"    -> when (language) { "ru" -> "Повар";   "en" -> "Chef";    else -> "Oshpaz" }
        "hr"      -> when (language) { "ru" -> "HR";      "en" -> "HR";      else -> "HR" }
        "student" -> when (language) { "ru" -> "Ученик";  "en" -> "Student"; else -> "O'quvchi" }
        else      -> when (language) { "ru" -> "Родитель";"en" -> "Parent";  else -> "Ota-ona" }
    }

    // Display name va initials
    val displayName = savedFullname.ifEmpty { roleName }
    val initials = displayName.trim().split(" ")
        .filter { it.isNotEmpty() }.take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifEmpty { "?" }

    // Parol o'zgartirish holati
    var currentPassword   by remember { mutableStateOf("") }
    var newPassword       by remember { mutableStateOf("") }
    var confirmPassword   by remember { mutableStateOf("") }
    var currentVisible    by remember { mutableStateOf(false) }
    var newVisible        by remember { mutableStateOf(false) }
    var confirmVisible    by remember { mutableStateOf(false) }
    var passwordSaved     by remember { mutableStateOf(false) }
    var passwordError     by remember { mutableStateOf("") }
    var passwordLoading   by remember { mutableStateOf(false) }

    fun onSavePassword() {
        passwordError = ""
        if (currentPassword.isBlank()) { passwordError = "Joriy parolni kiriting"; return }
        if (newPassword.length < 6)    { passwordError = "Yangi parol kamida 6 ta belgi"; return }
        if (newPassword != confirmPassword) { passwordError = "Parollar mos kelmadi"; return }
        scope.launch {
            passwordLoading = true
            // TODO: API ulanganda: authService.changePassword(currentPassword, newPassword)
            delay(1200)
            passwordLoading = false
            passwordSaved = true
            currentPassword = ""; newPassword = ""; confirmPassword = ""
            delay(3000); passwordSaved = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(accentContainer)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(20.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                // Avatar
                Box(
                    Modifier.size(64.dp).clip(CircleShape).background(accent.copy(0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = accent)
                }
                Column(Modifier.weight(1f)) {
                    Text(displayName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = accent)
                    if (savedUsername.isNotEmpty()) {
                        Text("@$savedUsername", fontSize = 13.sp, color = accent.copy(0.7f))
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(
                        Modifier.clip(RoundedCornerShape(6.dp))
                            .background(accent.copy(0.15f))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(roleName, fontSize = 11.sp, color = accent, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Profil ma'lumotlari ──
        SozSection(
            when (language) { "ru" -> "ЛИЧНЫЕ ДАННЫЕ"; "en" -> "PROFILE INFO"; else -> "PROFIL MA'LUMOTLARI" }
        ) {
            SozInfoRow(Icons.Default.Person,
                when (language) { "ru" -> "Полное имя"; "en" -> "Full name"; else -> "To'liq ism" },
                displayName.ifEmpty { "—" }
            )
            HorizontalDivider(color = Outline, thickness = 0.5.dp)
            SozInfoRow(Icons.Default.AccountCircle,
                when (language) { "ru" -> "Логин"; "en" -> "Username"; else -> "Login" },
                if (savedUsername.isNotEmpty()) "@$savedUsername" else "—"
            )
            HorizontalDivider(color = Outline, thickness = 0.5.dp)
            SozInfoRow(Icons.Default.Work,
                when (language) { "ru" -> "Должность"; "en" -> "Role"; else -> "Lavozim" },
                roleName
            )
            if (branchName.isNotEmpty()) {
                HorizontalDivider(color = Outline, thickness = 0.5.dp)
                SozInfoRow(Icons.Default.Business,
                    when (language) { "ru" -> "Филиал"; "en" -> "Branch"; else -> "Filial" },
                    branchName
                )
            }
            if (academicYear.isNotEmpty()) {
                HorizontalDivider(color = Outline, thickness = 0.5.dp)
                SozInfoRow(Icons.Default.CalendarToday,
                    when (language) { "ru" -> "Учебный год"; "en" -> "Academic year"; else -> "O'quv yili" },
                    academicYear
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Parol o'zgartirish ──
        SozSection(
            when (language) { "ru" -> "БЕЗОПАСНОСТЬ"; "en" -> "SECURITY"; else -> "XAVFSIZLIK" }
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Text(
                    when (language) { "ru" -> "Изменить пароль"; "en" -> "Change password"; else -> "Parolni o'zgartirish" },
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold
                )

                // Joriy parol
                SozPasswordField(
                    label = when (language) { "ru" -> "Текущий пароль"; "en" -> "Current password"; else -> "Joriy parol" },
                    value = currentPassword,
                    onValueChange = { currentPassword = it; passwordError = "" },
                    visible = currentVisible,
                    onToggle = { currentVisible = !currentVisible },
                    accent = accent
                )

                // Yangi parol
                SozPasswordField(
                    label = when (language) { "ru" -> "Новый пароль"; "en" -> "New password"; else -> "Yangi parol" },
                    value = newPassword,
                    onValueChange = { newPassword = it; passwordError = "" },
                    visible = newVisible,
                    onToggle = { newVisible = !newVisible },
                    accent = accent
                )

                // Tasdiqlash
                SozPasswordField(
                    label = when (language) { "ru" -> "Подтвердить пароль"; "en" -> "Confirm password"; else -> "Parolni tasdiqlang" },
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; passwordError = "" },
                    visible = confirmVisible,
                    onToggle = { confirmVisible = !confirmVisible },
                    accent = accent
                )

                // Xato
                if (passwordError.isNotEmpty()) {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                            .background(RedContainer).padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, null, tint = Red10, modifier = Modifier.size(16.dp))
                        Text(passwordError, fontSize = 12.sp, color = Red10)
                    }
                }

                // Muvaffaqiyat
                if (passwordSaved) {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                            .background(TealContainer).padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Teal10, modifier = Modifier.size(16.dp))
                        Text(
                            when (language) { "ru" -> "Пароль изменён!"; "en" -> "Password changed!"; else -> "Parol o'zgartirildi!" },
                            fontSize = 12.sp, color = Teal10
                        )
                    }
                }

                // Saqlash tugmasi
                Button(
                    onClick = { onSavePassword() },
                    enabled = !passwordLoading && !passwordSaved,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (passwordLoading) {
                        CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        when (language) { "ru" -> "Сохранить пароль"; "en" -> "Save password"; else -> "Parolni saqlash" },
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Ko'rinish ──
        SozSection(when (language) { "ru" -> "ВНЕШНИЙ ВИД"; "en" -> "APPEARANCE"; else -> "KO'RINISH" }) {
            Row(
                Modifier.fillMaxWidth().clickable(onClick = onToggleDark).padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
                        .background(if (isDark) accent.copy(0.15f) else MaterialTheme.colorScheme.surfaceVariant),
                    Alignment.Center
                ) {
                    Icon(
                        if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                        null, tint = if (isDark) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        when (language) { "ru" -> "Тёмный режим"; "en" -> "Dark mode"; else -> "Tungi rejim" },
                        fontSize = 15.sp, fontWeight = FontWeight.Medium
                    )
                    Text(
                        when (language) { "ru" -> "Тёмная тема интерфейса"; "en" -> "Dark interface theme"; else -> "Qorong'i interfeys" },
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isDark, onCheckedChange = { onToggleDark() },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Til ──
        SozSection(when (language) { "ru" -> "ЯЗЫК"; "en" -> "LANGUAGE"; else -> "TIL" }) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    Triple("🇺🇿", "O'zbekcha", "uz"),
                    Triple("🇷🇺", "Русский",   "ru"),
                    Triple("🇬🇧", "English",   "en")
                ).forEach { (flag, name, code) ->
                    val sel = language == code
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (sel) accent.copy(0.1f) else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onLanguageChange(code) }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(flag, fontSize = 22.sp)
                        Text(name, fontSize = 14.sp, fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (sel) accent else MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                        if (sel) Icon(Icons.Default.CheckCircle, null, tint = accent, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Ilova haqida ──
        SozSection(when (language) { "ru" -> "О ПРИЛОЖЕНИИ"; "en" -> "ABOUT"; else -> "ILOVA HAQIDA" }) {
            SozInfoRow(Icons.Default.Apps, "Maktab", "v1.0.0")
            HorizontalDivider(color = Outline, thickness = 0.5.dp)
            SozInfoRow(Icons.Default.Code,
                when (language) { "ru" -> "Разработчик"; "en" -> "Developer"; else -> "Dasturchi" },
                "Jasurbek Ruzimov"
            )
            HorizontalDivider(color = Outline, thickness = 0.5.dp)
            SozInfoRow(Icons.Default.PhoneAndroid, "Platform", "Android · Jetpack Compose")
        }

        Spacer(Modifier.height(12.dp))

        // ── Chiqish ──
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(0.5.dp, Red10.copy(0.3f)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onLogout).padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(RedContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Logout, null, tint = Red10, modifier = Modifier.size(20.dp))
                }
                Text(
                    when (language) { "ru" -> "Выйти из аккаунта"; "en" -> "Sign out"; else -> "Hisobdan chiqish" },
                    fontSize = 15.sp, color = Red10, fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.ChevronRight, null, tint = Red10.copy(0.5f))
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

// ─────────────────────────────────────────────
// Shared composable-lar
// ─────────────────────────────────────────────

@Composable
private fun SozSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            title, fontSize = 10.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.6.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(0.5.dp, Outline),
            elevation = CardDefaults.cardElevation(0.dp)
        ) { content() }
    }
}

@Composable
private fun SozInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Text(label, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SozPasswordField(
    label: String, value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean, onToggle: () -> Unit,
    accent: Color
) {
    Column {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(5.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            shape = RoundedCornerShape(10.dp),
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onToggle) {
                    Icon(
                        if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        null, modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accent, unfocusedBorderColor = Outline
            )
        )
    }
}

fun getSecurePrefs(context: Context): SharedPreferences {
    return try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context, "maktab_secure_prefs", masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        context.getSharedPreferences("maktab_secure_prefs", Context.MODE_PRIVATE)
    }
}