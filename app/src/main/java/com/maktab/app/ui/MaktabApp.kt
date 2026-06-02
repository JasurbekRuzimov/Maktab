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
import com.maktab.app.ui.theme.*
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
// DRAWER ITEMS — Teacher
// ─────────────────────────────────────────────

val teacherDrawerItems = listOf(
    NavItem("jadval",   "Jadval",        Icons.Default.TableChart,  "Asosiy"),
    NavItem("jurnal",   "Jurnal",         Icons.Default.GridOn,      "Asosiy"),
    NavItem("kontent",  "Dars kontenti",  Icons.Default.MenuBook,    "Asosiy"),
    NavItem("davomat",  "Davomat",        Icons.Default.HowToReg,    "Nazorat"),
    NavItem("baholash", "Baholash",       Icons.Default.Assessment,  "Nazorat"),
    NavItem("sinflar",  "Sinflarim",      Icons.Default.People,      "Nazorat"),
)

// ─────────────────────────────────────────────
// DRAWER ITEMS — Parent
// ─────────────────────────────────────────────

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

// ─────────────────────────────────────────────
// DRAWER ITEMS — Chef
// ─────────────────────────────────────────────

val studentDrawerItems = listOf(
    NavItem("jadval",       "Dars jadvali",     Icons.Default.TableChart,    "O'qish"),
    NavItem("darsliklar",   "Darsliklar",        Icons.Default.MenuBook,      "O'qish"),
    NavItem("vazifa",       "Uy vazifalari",     Icons.Default.AssignmentTurnedIn, "O'qish"),
    NavItem("baholar",      "Baholar",           Icons.Default.Star,          "Natijalar"),
    NavItem("davomat",      "Davomat",           Icons.Default.HowToReg,      "Natijalar"),
    NavItem("imtihonlar",   "Imtihonlar",        Icons.Default.Assignment,    "Natijalar"),
    NavItem("xabarnomalar", "Xabarnomalar",      Icons.Default.Notifications, "Boshqa"),
    NavItem("surveylar",    "So'rovnomalar",     Icons.Default.Poll,          "Boshqa"),
)

val hrDrawerItems = listOf(
    NavItem("xodimlar",    "Xodimlar ro'yxati",  Icons.Default.People,         "Xodimlar"),
    NavItem("yangi",       "Yangi xodim",         Icons.Default.PersonAdd,      "Xodimlar"),
    NavItem("lavozimlar",  "Lavozimlar",          Icons.Default.Business,       "Xodimlar"),
    NavItem("davomat",     "Xodimlar davomati",   Icons.Default.CalendarToday,  "Davomat va Ta'til"),
    NavItem("tatil",       "Ta'til va ruxsatlar", Icons.Default.BeachAccess,    "Davomat va Ta'til"),
    NavItem("maosh",       "Maosh hisob-kitobi",  Icons.Default.Payments,       "Moliya"),
    NavItem("analitika",   "HR analitika",        Icons.Default.BarChart,       "Hisobotlar"),
    NavItem("hujjatlar",   "Hujjatlar",           Icons.Default.Description,    "Hisobotlar"),
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
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(RedContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ExitToApp, null, tint = Red10, modifier = Modifier.size(24.dp))
            }
        },
        title = {
            Text(
                "Ilovadan chiqasizmi?",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                "Ilovani yopmoqchimisiz? Keyingi kirishda PIN kod so'raladi.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Red10),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Ha, chiqish", fontSize = 14.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Outline)
            ) {
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

    var savedPin  by remember { mutableStateOf(prefs.getString("pin", "") ?: "") }
    var savedRole by remember { mutableStateOf(prefs.getString("role", "") ?: "") }
    var isDark    by remember { mutableStateOf(false) }
    var language  by remember { mutableStateOf("uz") }
    var screen    by remember { mutableStateOf<Screen>(Screen.Splash) }
    var showExitDialog by remember { mutableStateOf(false) }

    fun saveSession(pin: String, role: String) {
        prefs.edit().putString("pin", pin).putString("role", role).apply()
        savedPin = pin; savedRole = role
    }
    fun clearSession() {
        prefs.edit().remove("pin").remove("role").apply()
        savedPin = ""; savedRole = ""
    }

    MaktabTheme(isDark = isDark, language = language) {

        // Ilovadan chiqish dialogi
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
                    onSuccess = {
                        screen = if (savedPin.isEmpty()) Screen.PinSetup(s.role)
                        else Screen.Dashboard(s.role)
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
                userName = when (s.role) {
                    "teacher" -> "Karimova Nargiza"
                    "chef"    -> "Toshmatov Sardor"
                    else      -> "Karimov Bobur"
                },
                onSuccess = { screen = Screen.Dashboard(s.role) },
                onForgotPin = { clearSession(); screen = Screen.RoleSelect }
            )

            is Screen.Dashboard -> {
                // Back bosilganda exit dialog ko'rsat
                BackHandler { showExitDialog = true }

                RoleApp(
                    role = s.role, language = language, isDark = isDark,
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
    onToggleDark: () -> Unit, onLanguageChange: (String) -> Unit, onLogout: () -> Unit
) {
    when (role) {
        "chef"    -> ChefApp(language, isDark, onToggleDark, onLanguageChange, onLogout)
        "student" -> DrawerApp(role, language, isDark, onToggleDark, onLanguageChange, onLogout)
        "hr"      -> DrawerApp(role, language, isDark, onToggleDark, onLanguageChange, onLogout)
        else      -> DrawerApp(role, language, isDark, onToggleDark, onLanguageChange, onLogout)
    }
}

// ─────────────────────────────────────────────
// DRAWER APP — Teacher, Parent, Chef uchun umumiy
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerApp(
    role: String, language: String, isDark: Boolean,
    onToggleDark: () -> Unit, onLanguageChange: (String) -> Unit, onLogout: () -> Unit
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
        "teacher" -> teacherDrawerItems
        "student" -> studentDrawerItems
        "hr"      -> hrDrawerItems
        else      -> parentDrawerItems
    }
    val defaultId = drawerItems.first().id

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedId by remember { mutableStateOf(defaultId) }
    var showSettings by remember { mutableStateOf(false) }

    val currentTitle = drawerItems.find { it.id == selectedId }?.labelUz ?: drawerItems.first().labelUz

    val userName = when (role) {
        "teacher" -> "Karimova Nargiza"
        "student" -> "Asilbek Karimov"
        "hr"      -> "Rahimova Aziza"
        else      -> "Karimov Bobur"
    }
    val userSub = when (role) {
        "teacher" -> "O'qituvchi · 5-A sinf"
        "student" -> "O'quvchi · 5-A sinf"
        "hr"      -> "HR mutaxassis"
        else      -> "Ota-ona · 5-A sinf"
    }
    val roleIcon = when (role) {
        "teacher" -> Icons.Default.School
        "student" -> Icons.Default.Person
        "hr"      -> Icons.Default.People
        else      -> Icons.Default.FamilyRestroom
    }

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(accentContainer)
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(20.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier.size(52.dp).clip(CircleShape)
                                .background(accent.copy(0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(roleIcon, null, tint = accent, modifier = Modifier.size(26.dp))
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(userName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = accent)
                        Text(userSub, fontSize = 12.sp, color = accent.copy(0.7f))
                    }
                }

                // Guruh bo'yicha scrollable menyu
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
                ) {
                    drawerItems.map { it.group }.distinct().forEach { group ->
                        item {
                            Text(
                                text = group.uppercase(),
                                fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.8.sp,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                            )
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
                                onClick = {
                                    selectedId = item.id
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 1.dp),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = accentContainer,
                                    unselectedContainerColor = Color.Transparent
                                )
                            )
                        }
                        item {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                                color = Outline, thickness = 0.5.dp
                            )
                        }
                    }
                }

                // Sozlamalar + Chiqish — har doim pastda ko'rinadi
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
                onToggleDark = onToggleDark, onLanguageChange = onLanguageChange, onLogout = onLogout
            )
            return@ModalNavigationDrawer
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menyu", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    title = {
                        Column {
                            Text(currentTitle, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("$userName · ${if (isTeacher) "5-A" else "5-A sinf"}",
                                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    actions = {
                        Box(Modifier.padding(end = 12.dp)) {
                            Surface(shape = RoundedCornerShape(6.dp), color = accent.copy(0.1f)) {
                                Text(language.uppercase(),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accent)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                if (isTeacher) {
                    when (selectedId) {
                        "jadval"   -> TeacherScheduleScreen()
                        "jurnal"   -> JurnalScreen(language)
                        "kontent"  -> DarsKontentiScreen(language)
                        "davomat"  -> DavomatScreen()
                        "baholash" -> BaholashScreen(language)
                        "sinflar"  -> SinflarimScreen(language)
                        else       -> TeacherScheduleScreen()
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
                    when (selectedId) {
                        "dashboard"   -> ParentDashboardScreen()
                        "profil"      -> ParentProfilScreen()
                        "davomat"     -> ParentDavomatScreen()
                        "baholar"     -> BaholarScreen()
                        "vazifa"      -> ParentUygaVazifaScreen()
                        "imtihonlar"  -> ParentImtihonlarScreen()
                        "tolovlar"    -> ParentTolovlarScreen()
                        "yangiliklar" -> ParentYangiliklar()
                        "bloglar"     -> ParentBloglar()
                        "surveylar"   -> ParentSurveylar()
                        "murojaatlar" -> ParentMurojaatlar()
                        else          -> ParentDashboardScreen()
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// CHEF APP — alohida (amber rang)
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChefApp(
    language: String, isDark: Boolean,
    onToggleDark: () -> Unit, onLanguageChange: (String) -> Unit, onLogout: () -> Unit
) {
    val accent = Amber10
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedId by remember { mutableStateOf("dashboard") }
    var showSettings by remember { mutableStateOf(false) }

    val currentTitle = chefDrawerItems.find { it.id == selectedId }?.labelUz ?: "Bosh panel"

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AmberContainer)
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(20.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier.size(52.dp).clip(CircleShape)
                                .background(Amber10.copy(0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Restaurant, null, tint = Amber10, modifier = Modifier.size(26.dp))
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("Toshmatov Sardor", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Amber10)
                        Text("Oshpaz · Kafeteriya", fontSize = 12.sp, color = Amber10.copy(0.7f))
                    }
                }

                // Guruh bo'yicha scrollable menyu
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
                ) {
                    chefDrawerItems.map { it.group }.distinct().forEach { group ->
                        item {
                            Text(
                                text = group.uppercase(),
                                fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.8.sp,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                            )
                        }
                        items(chefDrawerItems.filter { it.group == group }) { item ->
                            val isSelected = selectedId == item.id
                            NavigationDrawerItem(
                                icon = {
                                    Box(
                                        modifier = Modifier.size(34.dp).clip(RoundedCornerShape(9.dp))
                                            .background(if (isSelected) Amber10.copy(0.15f) else MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
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
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                                color = Outline, thickness = 0.5.dp
                            )
                        }
                    }
                }

                // Sozlamalar + Chiqish — pastda qotib turadi
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
                onToggleDark = onToggleDark, onLanguageChange = onLanguageChange, onLogout = onLogout
            )
            return@ModalNavigationDrawer
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menyu", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    title = {
                        Column {
                            Text(currentTitle, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Toshmatov Sardor · Kafeteriya",
                                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    actions = {
                        Box(Modifier.padding(end = 12.dp)) {
                            Surface(shape = RoundedCornerShape(6.dp), color = accent.copy(0.1f)) {
                                Text(language.uppercase(),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accent)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (selectedId) {
                    "dashboard"  -> ChefDashboardScreen()
                    "ombor"      -> ChefOmborScreen()
                    "ingredient" -> ChefIngredientsScreen()
                    "retsept"    -> ChefRecipesScreen()
                    "menyu"      -> ChefMenuCalendarScreen()
                    "harakat"    -> ChefStockMovementsScreen()
                    "analitika"  -> ChefAnalyticsScreen()
                    else         -> ChefDashboardScreen()
                }
            }
        }
    }
}