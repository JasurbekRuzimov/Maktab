package com.maktab.app.ui

import android.app.Activity
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.maktab.app.ui.screens.*
import com.maktab.app.ui.screens.auth.*
import com.maktab.app.ui.screens.teacher.*
import com.maktab.app.ui.screens.student.*
import com.maktab.app.ui.screens.parent.*
import com.maktab.app.ui.screens.chef.*
import com.maktab.app.ui.screens.hr.*
import com.maktab.app.ui.screens.common.*
import com.maktab.app.ui.theme.*
import com.maktab.app.viewmodel.ParentViewModel
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────
// ENCRYPTED PREFS
// ─────────────────────────────────────────────

private fun getSecurePrefs(context: Context) = EncryptedSharedPreferences.create(
    context,
    "maktab_secure_prefs",
    MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)

// ─────────────────────────────────────────────
// SCREEN MODEL
// ─────────────────────────────────────────────

sealed class Screen {
    object Splash : Screen()
    object RoleSelect : Screen()
    data class Login(val role: String) : Screen()
    data class PinSetup(val role: String) : Screen()
    data class PinEntry(val role: String) : Screen()
    data class Dashboard(val role: String) : Screen()
}

data class NavItem(
    val id: String,
    val labelUz: String,
    val icon: ImageVector,
    val group: String = ""
)

// ─────────────────────────────────────────────
// DRAWER ITEMS
// ─────────────────────────────────────────────

val teacherDrawerItems = listOf(
    NavItem("jadval",   "Jadval",        Icons.Default.TableChart,  "Asosiy"),
    NavItem("jurnal",   "Jurnal",         Icons.Default.GridOn,      "Asosiy"),
    NavItem("kontent",  "Dars kontenti",  Icons.Default.MenuBook,    "Asosiy"),
    NavItem("davomat",  "Davomat",        Icons.Default.HowToReg,    "Nazorat"),
    NavItem("baholash", "Baholash",       Icons.Default.Assessment,  "Nazorat"),
    NavItem("sinflar",  "Sinflarim",      Icons.Default.People,      "Nazorat"),
)

val parentDrawerItems = listOf(
    NavItem("dashboard",  "Dashboard",       Icons.Default.Dashboard,       "Bosh sahifa"),
    NavItem("profil",     "Profil",          Icons.Default.Person,          "Bosh sahifa"),
    NavItem("davomat",    "Davomat",         Icons.Default.CalendarToday,   "Farzand ta'limi"),
    NavItem("baholar",    "Baholar",         Icons.Default.Star,            "Farzand ta'limi"),
    NavItem("vazifa",     "Uy vazifalari",   Icons.Default.MenuBook,        "Farzand ta'limi"),
    NavItem("imtihonlar", "Imtihonlar",      Icons.Default.Assignment,      "Farzand ta'limi"),
    NavItem("tolovlar",   "To'lovlar",       Icons.Default.Payment,         "Farzand ta'limi"),
    NavItem("yangiliklar","Yangiliklar",     Icons.Default.Notifications,   "Faollik"),
    NavItem("bloglar",    "Bloglar",         Icons.Default.Article,         "Faollik"),
    NavItem("surveylar",  "So'rovnomalar",   Icons.Default.Poll,            "Faollik"),
    NavItem("murojaatlar","Murojaatlar",     Icons.Default.Feedback,        "Faollik"),
)

val studentDrawerItems = listOf(
    NavItem("jadval",       "Dars jadvali",     Icons.Default.TableChart,         "O'qish"),
    NavItem("darsliklar",   "Darsliklar",        Icons.Default.MenuBook,           "O'qish"),
    NavItem("vazifa",       "Uy vazifalari",     Icons.Default.AssignmentTurnedIn, "O'qish"),
    NavItem("baholar",      "Baholar",           Icons.Default.Star,               "Natijalar"),
    NavItem("davomat",      "Davomat",           Icons.Default.HowToReg,           "Natijalar"),
    NavItem("imtihonlar",   "Imtihonlar",        Icons.Default.Assignment,         "Natijalar"),
    NavItem("xabarnomalar", "Xabarnomalar",      Icons.Default.Notifications,      "Boshqa"),
    NavItem("surveylar",    "So'rovnomalar",     Icons.Default.Poll,               "Boshqa"),
)

val hrDrawerItems = listOf(
    NavItem("xodimlar",   "Xodimlar ro'yxati",  Icons.Default.People,        "Xodimlar"),
    NavItem("yangi",      "Yangi xodim",         Icons.Default.PersonAdd,     "Xodimlar"),
    NavItem("lavozimlar", "Lavozimlar",          Icons.Default.Business,      "Xodimlar"),
    NavItem("davomat",    "Xodimlar davomati",   Icons.Default.CalendarToday, "Davomat va Ta'til"),
    NavItem("tatil",      "Ta'til va ruxsatlar", Icons.Default.BeachAccess,   "Davomat va Ta'til"),
    NavItem("maosh",      "Maosh hisob-kitobi",  Icons.Default.Payments,      "Moliya"),
    NavItem("analitika",  "HR analitika",        Icons.Default.BarChart,      "Hisobotlar"),
    NavItem("hujjatlar",  "Hujjatlar",           Icons.Default.Description,   "Hisobotlar"),
)

val chefDrawerItems = listOf(
    NavItem("dashboard",  "Bosh panel",            Icons.Default.Dashboard,      "Asosiy"),
    NavItem("ombor",      "Oziq-ovqat ombori",     Icons.Default.Inventory,      "Asosiy"),
    NavItem("ingredient", "Ingredientlar",          Icons.Default.Grass,          "Asosiy"),
    NavItem("retsept",    "Taom retseptlari",       Icons.Default.RestaurantMenu, "Taomlar"),
    NavItem("menyu",      "Menyu kalendari",        Icons.Default.CalendarMonth,  "Taomlar"),
    NavItem("harakat",    "Stock harakatlari",      Icons.Default.SwapVert,       "Hisobotlar"),
    NavItem("analitika",  "Kafeteriya analitikasi", Icons.Default.BarChart,       "Hisobotlar"),
)

// ─────────────────────────────────────────────
// ILOVADAN CHIQISH DIALOGI
// ─────────────────────────────────────────────

@Composable
fun ExitAppDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(RedContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ExitToApp, null, tint = Red10, modifier = Modifier.size(24.dp))
            }
        },
        title = {
            Text("Ilovadan chiqasizmi?", fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        },
        text = {
            Text("Ilovani yopmoqchimisiz? Keyingi kirishda PIN kod so'raladi.",
                fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
        },
        confirmButton = {
            Button(onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Red10),
                shape = RoundedCornerShape(10.dp)) {
                Text("Ha, chiqish", fontSize = 14.sp)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Outline)) {
                Text("Bekor qilish", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

// ─────────────────────────────────────────────
// ROOT
// ─────────────────────────────────────────────

@Composable
fun MaktabApp() {
    val context = LocalContext.current
    val activity = context as? Activity
    val prefs = remember { getSecurePrefs(context) }

    var savedPin      by remember { mutableStateOf(prefs.getString("pin", "") ?: "") }
    var savedRole     by remember { mutableStateOf(prefs.getString("role", "") ?: "") }
    var savedFullname by remember { mutableStateOf(prefs.getString("fullname", "") ?: "") }
    var savedUsername by remember { mutableStateOf(prefs.getString("username", "") ?: "") }
    var savedBranch   by remember { mutableStateOf(prefs.getString("branch", "") ?: "") }
    var isDark        by remember { mutableStateOf(false) }
    var language      by remember { mutableStateOf("uz") }
    var screen        by remember { mutableStateOf<Screen>(Screen.Splash) }
    var showExitDialog by remember { mutableStateOf(false) }

    fun saveSession(pin: String, role: String) {
        prefs.edit().putString("pin", pin).putString("role", role).apply()
        savedPin = pin; savedRole = role
    }
    fun saveUserInfo(session: com.maktab.app.network.SessionInfo) {
        prefs.edit()
            .putString("role",         session.role)
            .putString("fullname",     session.fullname)
            .putString("username",     session.username)
            .putString("branch",       session.branchName)   // ID emas, "Shahar" kabi nom
            .putString("accessToken",  session.accessToken)
            .putString("refreshToken", session.refreshToken)
            .putString("renewEndpoint",session.renewEndpoint)
            .apply()
        savedRole     = session.role
        savedFullname = session.fullname
        savedUsername = session.username
        savedBranch   = session.branchName
        com.maktab.app.network.RetrofitClient.accessToken = session.accessToken
    }
    fun clearSession() {
        prefs.edit()
            .remove("pin").remove("role").remove("fullname")
            .remove("username").remove("branch")
            .remove("accessToken").remove("refreshToken").remove("renewEndpoint")
            .apply()
        savedPin = ""; savedRole = ""; savedFullname = ""; savedUsername = ""
    }

    MaktabTheme(isDark = isDark, language = language) {

        if (showExitDialog) {
            ExitAppDialog(
                onConfirm = { activity?.finish() },
                onDismiss = { showExitDialog = false }
            )
        }

        when (val s = screen) {
            Screen.Splash -> SplashScreen(onFinished = {
                screen = if (savedPin.isNotEmpty() && savedRole.isNotEmpty())
                    Screen.PinEntry(savedRole) else Screen.RoleSelect
            })

            Screen.RoleSelect -> LandingScreen(onRoleSelected = { role ->
                savedRole = role; screen = Screen.Login(role)
            })

            is Screen.Login -> {
                BackHandler { screen = Screen.RoleSelect }
                LoginScreen(
                    role = s.role,
                    onSuccess = { session ->
                        saveUserInfo(session)
                        screen = if (savedPin.isEmpty()) Screen.PinSetup(session.role)
                        else Screen.Dashboard(session.role)
                    },
                    onBack = { screen = Screen.RoleSelect }
                )
            }

            is Screen.PinSetup -> PinSetupScreen(
                role = s.role,
                onPinSet = { pin -> saveSession(pin, s.role); screen = Screen.Dashboard(s.role) }
            )

            is Screen.PinEntry -> PinEntryScreen(
                role = s.role, savedPin = savedPin,
                userName = savedFullname.ifEmpty {
                    when (s.role) {
                        "teacher" -> "O'qituvchi"; "chef" -> "Oshpaz"
                        "hr" -> "HR"; "student" -> "O'quvchi"; else -> "Foydalanuvchi"
                    }
                },
                onSuccess = { screen = Screen.Dashboard(s.role) },
                onForgotPin = { clearSession(); screen = Screen.RoleSelect }
            )

            is Screen.Dashboard -> {
                BackHandler { showExitDialog = true }
                RoleApp(
                    role = s.role, language = language, isDark = isDark,
                    fullname = savedFullname, username = savedUsername,
                    onToggleDark = { isDark = !isDark },
                    onLanguageChange = { language = it },
                    onLogout = { clearSession(); screen = Screen.RoleSelect }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// ROLE ROUTER
// ─────────────────────────────────────────────

@Composable
fun RoleApp(
    role: String, language: String, isDark: Boolean,
    fullname: String = "", username: String = "",
    onToggleDark: () -> Unit, onLanguageChange: (String) -> Unit, onLogout: () -> Unit
) {
    when (role) {
        "chef"    -> ChefApp(language, isDark, onToggleDark, onLanguageChange, onLogout, fullname)
        "student" -> DrawerApp(role, language, isDark, onToggleDark, onLanguageChange, onLogout, fullname, username)
        "hr"      -> DrawerApp(role, language, isDark, onToggleDark, onLanguageChange, onLogout, fullname, username)
        else      -> DrawerApp(role, language, isDark, onToggleDark, onLanguageChange, onLogout, fullname, username)
    }
}

// ─────────────────────────────────────────────
// DRAWER APP — Teacher, Parent, Student, HR
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerApp(
    role: String, language: String, isDark: Boolean,
    onToggleDark: () -> Unit, onLanguageChange: (String) -> Unit, onLogout: () -> Unit,
    fullname: String = "", username: String = ""
) {
    val isTeacher = role == "teacher"
    val isStudent = role == "student"
    val isHR      = role == "hr"
    val accent = when (role) {
        "teacher" -> Teal10; "student" -> Blue10; "hr" -> Purple10; else -> Blue10
    }
    val accentContainer = when (role) {
        "teacher" -> TealContainer; "student" -> BlueContainer; "hr" -> PurpleContainer; else -> BlueContainer
    }
    val drawerItems = when (role) {
        "teacher" -> teacherDrawerItems; "student" -> studentDrawerItems
        "hr" -> hrDrawerItems; else -> parentDrawerItems
    }
    val defaultId = drawerItems.first().id

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedId by remember { mutableStateOf(defaultId) }
    var showSettings by remember { mutableStateOf(false) }

    val currentTitle = drawerItems.find { it.id == selectedId }?.labelUz ?: drawerItems.first().labelUz

    val userName = fullname.ifEmpty {
        when (role) { "teacher" -> "O'qituvchi"; "student" -> "O'quvchi"; "hr" -> "HR"; else -> "Foydalanuvchi" }
    }
    val userSub = when (role) {
        "teacher" -> "O'qituvchi"; "student" -> "O'quvchi"; "hr" -> "HR mutaxassis"; else -> "Ota-ona"
    }
    val roleIcon = when (role) {
        "teacher" -> Icons.Default.School; "student" -> Icons.Default.Person
        "hr" -> Icons.Default.People; else -> Icons.Default.FamilyRestroom
    }
    val initials = userName.trim().split(" ").filter { it.isNotEmpty() }.take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }.ifEmpty { "?" }

    // Teacher uchun ViewModel — o'quv yili uchun
    val teacherVm: com.maktab.app.viewmodel.TeacherViewModel? =
        if (isTeacher) androidx.lifecycle.viewmodel.compose.viewModel() else null
    val academicYear by (teacherVm?.academicYear
        ?: kotlinx.coroutines.flow.MutableStateFlow("")).collectAsState()

    // Prefs dan filial nomini olamiz
    val context = LocalContext.current
    val savedBranch = remember {
        try {
            val mk = androidx.security.crypto.MasterKey.Builder(context)
                .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM).build()
            androidx.security.crypto.EncryptedSharedPreferences.create(
                context, "maktab_secure_prefs", mk,
                androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ).getString("branch", "") ?: ""
        } catch (e: Exception) { "" }
    }

    // TopBar subtitle: 2 qator
    // 1-qator: "Karimova Nargiza · O'qituvchi"
    // 2-qator: "2025/2026 · Shahar"
    val subtitleLine1 = if (userName.isNotEmpty() && userName != "Foydalanuvchi")
        "$userName · $userSub" else userSub
    val subtitleLine2 = listOfNotNull(
        academicYear.ifEmpty { null },
        savedBranch.ifEmpty { null }
    ).joinToString(" · ")

    BackHandler(enabled = drawerState.isOpen) { scope.launch { drawerState.close() } }
    BackHandler(enabled = showSettings) { showSettings = false }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerShape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
                windowInsets = WindowInsets(0)
            ) {
                // Header
                Box(
                    modifier = Modifier.fillMaxWidth().background(accentContainer)
                        .windowInsetsPadding(WindowInsets.statusBars).padding(20.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier.size(52.dp).clip(CircleShape).background(accent.copy(0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (initials != "?") {
                                Text(initials, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = accent)
                            } else {
                                Icon(roleIcon, null, tint = accent, modifier = Modifier.size(26.dp))
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(userName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = accent)
                        Text(userSub, fontSize = 12.sp, color = accent.copy(0.7f))
                        if (username.isNotEmpty()) {
                            Text("@$username", fontSize = 11.sp, color = accent.copy(0.5f))
                        }
                    }
                }

                // Scrollable menyu
                LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)) {
                    drawerItems.map { it.group }.distinct().forEach { group ->
                        item {
                            Text(group.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.8.sp,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
                        }
                        items(drawerItems.filter { it.group == group }) { item ->
                            val isSelected = selectedId == item.id
                            NavigationDrawerItem(
                                icon = {
                                    Box(
                                        modifier = Modifier.size(34.dp).clip(RoundedCornerShape(9.dp))
                                            .background(if (isSelected) accent.copy(0.15f) else MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(item.icon, null,
                                            tint = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp))
                                    }
                                },
                                label = {
                                    Text(item.labelUz, fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) accent else MaterialTheme.colorScheme.onSurface)
                                },
                                selected = isSelected,
                                onClick = { selectedId = item.id; scope.launch { drawerState.close() } },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 1.dp),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = accentContainer,
                                    unselectedContainerColor = Color.Transparent
                                )
                            )
                        }
                        item {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                                color = Outline, thickness = 0.5.dp)
                        }
                    }
                }

                // Sozlamalar + Chiqish
                HorizontalDivider(color = Outline, thickness = 0.5.dp)
                Column(modifier = Modifier.navigationBarsPadding().padding(vertical = 6.dp)) {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) },
                        label = { Text("Sozlamalar", fontSize = 13.sp) },
                        selected = false,
                        onClick = { showSettings = true; scope.launch { drawerState.close() } },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 1.dp),
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Logout, null, tint = Red10, modifier = Modifier.size(20.dp)) },
                        label = { Text("Chiqish", fontSize = 13.sp, color = Red10) },
                        selected = false,
                        onClick = onLogout,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 1.dp),
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                }
            }
        }
    ) {
        if (showSettings) {
            SozlamalarScreen(
                role = role, isDark = isDark, language = language,
                onToggleDark = onToggleDark, onLanguageChange = onLanguageChange, onLogout = onLogout,
                fullname = fullname, username = username,
                academicYear = if (isTeacher) academicYear else ""
            )
            return@ModalNavigationDrawer
        }

        var showLangMenu by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    // 1-qator: hamburger + sarlavha + til tanlash
                    Row(
                        modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menyu", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        Text(
                            currentTitle, fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        // Til tanlash dropdown
                        Box {
                            Surface(
                                onClick = { showLangMenu = true },
                                shape = RoundedCornerShape(6.dp),
                                color = accent.copy(0.1f),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(Icons.Default.Translate, null, tint = accent, modifier = Modifier.size(13.dp))
                                    Text(language.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accent)
                                    Icon(Icons.Default.ArrowDropDown, null, tint = accent, modifier = Modifier.size(14.dp))
                                }
                            }
                            DropdownMenu(
                                expanded = showLangMenu,
                                onDismissRequest = { showLangMenu = false }
                            ) {
                                listOf(
                                    Triple("🇺🇿", "O'zbek", "uz"),
                                    Triple("🇷🇺", "Русский", "ru"),
                                    Triple("🇬🇧", "English", "en")
                                ).forEach { (flag, name, code) ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Text(flag, fontSize = 16.sp)
                                                Text(name, fontSize = 13.sp)
                                                if (language == code) {
                                                    Spacer(Modifier.weight(1f))
                                                    Icon(Icons.Default.Check, null, tint = accent, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        },
                                        onClick = { onLanguageChange(code); showLangMenu = false }
                                    )
                                }
                            }
                        }
                    }

                    // 2-qator: filial + o'quv yili + ism chips
                    if (subtitleLine1.isNotEmpty() || subtitleLine2.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Filial
                            if (savedBranch.isNotEmpty()) {
                                HeaderChip(
                                    icon = Icons.Default.Business,
                                    text = savedBranch,
                                    accent = accent
                                )
                            }
                            // O'quv yili
                            if (academicYear.isNotEmpty()) {
                                HeaderChip(
                                    icon = Icons.Default.CalendarToday,
                                    text = academicYear,
                                    accent = accent
                                )
                            }
                            // Ism (qisqa)
                            if (userName.isNotEmpty() && userName != "Foydalanuvchi") {
                                HeaderChip(
                                    icon = Icons.Default.Translate,
                                    text = language.uppercase(),
                                    accent = accent,
                                    showIcon = false
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = Outline, thickness = 0.5.dp)
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                if (isTeacher) {
                    when (selectedId) {
                        "jadval"   -> TeacherScheduleScreen(vm = teacherVm!!)
                        "jurnal"   -> JurnalScreen(language, vm = teacherVm!!)
                        "kontent"  -> DarsKontentiScreen(language)
                        "davomat"  -> DavomatScreen(vm = teacherVm!!)
                        "baholash" -> BaholashScreen(language)
                        "sinflar"  -> SinflarimScreen(language, vm = teacherVm!!)
                        else       -> TeacherScheduleScreen(vm = teacherVm!!)
                    }
                } else if (isStudent) {
                    when (selectedId) {
                        "jadval"       -> StudentScheduleScreen()
                        "darsliklar"   -> StudentDarsliklarsScreen()
                        "vazifa"       -> StudentUyVazifaScreen()
                        "baholar"      -> StudentBaholarScreen()
                        "davomat"      -> StudentDavomatScreen()
                        "imtihonlar"   -> StudentImtihonlarScreen()
                        "xabarnomalar" -> StudentXabarnomaScreen()
                        "surveylar"    -> StudentSurveylar()
                        else           -> StudentScheduleScreen()
                    }
                } else if (isHR) {
                    when (selectedId) {
                        "xodimlar"   -> HRXodimlarScreen()
                        "yangi"      -> HRYangiXodimScreen()
                        "lavozimlar" -> HRLavozimlarScreen()
                        "davomat"    -> HRDavomatScreen()
                        "tatil"      -> HRTatilScreen()
                        "maosh"      -> HRMaoshScreen()
                        "analitika"  -> HRAnalitika()
                        "hujjatlar"  -> HRHujjatlarScreen()
                        else         -> HRXodimlarScreen()
                    }
                } else {
                    val parentVm: com.maktab.app.viewmodel.ParentViewModel =
                        androidx.lifecycle.viewmodel.compose.viewModel()
                    when (selectedId) {
                        "dashboard"   -> ParentDashboardScreen(vm = parentVm)
                        "profil"      -> ParentProfilScreen(vm = parentVm)
                        "davomat"     -> ParentDavomatScreen(vm = parentVm)
                        "baholar"     -> BaholarScreen(vm = parentVm)
                        "vazifa"      -> ParentUygaVazifaScreen(vm = parentVm)
                        "imtihonlar"  -> ParentImtihonlarScreen(vm = parentVm)
                        "tolovlar"    -> ParentTolovlarScreen(vm = parentVm)
                        "yangiliklar" -> ParentYangiliklar()
                        "bloglar"     -> ParentBloglar()
                        "surveylar"   -> ParentSurveylar()
                        "murojaatlar" -> ParentMurojaatlar()
                        else          -> ParentDashboardScreen(vm = parentVm)
                    }
                }
            }
        }
    }
}


// ─────────────────────────────────────────────
// CHEF APP
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChefApp(
    language: String, isDark: Boolean,
    onToggleDark: () -> Unit, onLanguageChange: (String) -> Unit, onLogout: () -> Unit,
    fullname: String = ""
) {
    val accent = Amber10
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedId by remember { mutableStateOf("dashboard") }
    var showSettings by remember { mutableStateOf(false) }

    val currentTitle = chefDrawerItems.find { it.id == selectedId }?.labelUz ?: "Bosh panel"
    val chefName = fullname.ifEmpty { "Oshpaz" }
    val chefInitials = chefName.trim().split(" ").filter { it.isNotEmpty() }.take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }.ifEmpty { "O" }

    BackHandler(enabled = drawerState.isOpen) { scope.launch { drawerState.close() } }
    BackHandler(enabled = showSettings) { showSettings = false }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerShape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
                windowInsets = WindowInsets(0)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().background(AmberContainer)
                        .windowInsetsPadding(WindowInsets.statusBars).padding(20.dp)
                ) {
                    Column {
                        Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(Amber10.copy(0.2f)),
                            contentAlignment = Alignment.Center) {
                            Text(chefInitials, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Amber10)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(chefName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Amber10)
                        Text("Oshpaz · Kafeteriya", fontSize = 12.sp, color = Amber10.copy(0.7f))
                    }
                }

                LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)) {
                    chefDrawerItems.map { it.group }.distinct().forEach { group ->
                        item {
                            Text(group.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.8.sp,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
                        }
                        items(chefDrawerItems.filter { it.group == group }) { item ->
                            val isSelected = selectedId == item.id
                            NavigationDrawerItem(
                                icon = {
                                    Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(9.dp))
                                        .background(if (isSelected) Amber10.copy(0.15f) else MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center) {
                                        Icon(item.icon, null,
                                            tint = if (isSelected) Amber10 else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp))
                                    }
                                },
                                label = {
                                    Text(item.labelUz, fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) Amber10 else MaterialTheme.colorScheme.onSurface)
                                },
                                selected = isSelected,
                                onClick = { selectedId = item.id; scope.launch { drawerState.close() } },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 1.dp),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = AmberContainer,
                                    unselectedContainerColor = Color.Transparent
                                )
                            )
                        }
                        item {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                                color = Outline, thickness = 0.5.dp)
                        }
                    }
                }

                HorizontalDivider(color = Outline, thickness = 0.5.dp)
                Column(modifier = Modifier.navigationBarsPadding().padding(vertical = 6.dp)) {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) },
                        label = { Text("Sozlamalar", fontSize = 13.sp) },
                        selected = false,
                        onClick = { showSettings = true; scope.launch { drawerState.close() } },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 1.dp),
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Logout, null, tint = Red10, modifier = Modifier.size(20.dp)) },
                        label = { Text("Chiqish", fontSize = 13.sp, color = Red10) },
                        selected = false,
                        onClick = onLogout,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 1.dp),
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                }
            }
        }
    ) {
        if (showSettings) {
            SozlamalarScreen(
                role = "chef", isDark = isDark, language = language,
                onToggleDark = onToggleDark, onLanguageChange = onLanguageChange, onLogout = onLogout,
                fullname = fullname
            )
            return@ModalNavigationDrawer
        }

        val chefVm: com.maktab.app.viewmodel.ChefViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menyu", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        Text(currentTitle, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        var showLangMenu by remember { mutableStateOf(false) }
                        Box {
                            Surface(
                                onClick = { showLangMenu = true },
                                shape = RoundedCornerShape(6.dp),
                                color = accent.copy(0.1f),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(Icons.Default.Translate, null, tint = accent, modifier = Modifier.size(13.dp))
                                    Text(language.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accent)
                                    Icon(Icons.Default.ArrowDropDown, null, tint = accent, modifier = Modifier.size(14.dp))
                                }
                            }
                            DropdownMenu(expanded = showLangMenu, onDismissRequest = { showLangMenu = false }) {
                                listOf(Triple("🇺🇿", "O'zbek", "uz"), Triple("🇷🇺", "Русский", "ru"), Triple("🇬🇧", "English", "en"))
                                    .forEach { (flag, name, code) ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Text(flag, fontSize = 16.sp)
                                                    Text(name, fontSize = 13.sp)
                                                    if (language == code) { Spacer(Modifier.weight(1f)); Icon(Icons.Default.Check, null, tint = accent, modifier = Modifier.size(14.dp)) }
                                                }
                                            },
                                            onClick = { onLanguageChange(code); showLangMenu = false }
                                        )
                                    }
                            }
                        }
                    }
                    // Chip lar qatori
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeaderChip(Icons.Default.Person, chefName, accent)
                        HeaderChip(Icons.Default.Restaurant, "Kafeteriya", accent)
                    }
                    HorizontalDivider(color = Outline, thickness = 0.5.dp)
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (selectedId) {
                    "dashboard"  -> ChefDashboardScreen(chefVm)
                    "ombor"      -> ChefOmborScreen(chefVm)
                    "ingredient" -> ChefIngredientsScreen(chefVm)
                    "retsept"    -> ChefRecipesScreen(chefVm)
                    "menyu"      -> ChefMenuCalendarScreen(chefVm)
                    "harakat"    -> ChefStockMovementsScreen(chefVm)
                    "analitika"  -> ChefAnalyticsScreen()
                    else         -> ChefDashboardScreen(chefVm)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// HEADER CHIP — filial, o'quv yili, til uchun
// ─────────────────────────────────────────────

@Composable
fun HeaderChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    accent: androidx.compose.ui.graphics.Color,
    showIcon: Boolean = true
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = accent.copy(0.08f),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, accent.copy(0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (showIcon) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(11.dp))
            }
            Text(text, fontSize = 11.sp, color = accent, fontWeight = FontWeight.Medium)
        }
    }
}