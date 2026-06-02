package com.maktab.app.ui

import android.content.Context
import androidx.activity.compose.BackHandler
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
// ENCRYPTED PREFS HELPER
// ─────────────────────────────────────────────

private fun getSecurePrefs(context: Context) = EncryptedSharedPreferences.create(
    context,
    "maktab_secure_prefs",
    MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)

// ─────────────────────────────────────────────
// NAVIGATION MODELS
// ─────────────────────────────────────────────

sealed class Screen {
    object Splash : Screen()
    object RoleSelect : Screen()
    data class Login(val role: String) : Screen()
    data class PinSetup(val role: String) : Screen()
    data class PinEntry(val role: String) : Screen()
    data class Dashboard(val role: String) : Screen()
}

data class NavTab(
    val id: String,
    val labelUz: String, val labelRu: String, val labelEn: String,
    val icon: ImageVector
) {
    fun label(lang: String) = when (lang) { "ru" -> labelRu; "en" -> labelEn; else -> labelUz }
}

data class DrawerItem(
    val id: String,
    val labelUz: String,
    val icon: ImageVector,
    val group: String
)

// ─────────────────────────────────────────────
// TABS
// ─────────────────────────────────────────────

val teacherTabs = listOf(
    NavTab("jadval",   "Jadval",    "Расписание", "Schedule",   Icons.Default.TableChart),
    NavTab("jurnal",   "Jurnal",    "Журнал",     "Journal",    Icons.Default.GridOn),
    NavTab("kontent",  "Dars",      "Контент",    "Content",    Icons.Default.MenuBook),
    NavTab("davomat",  "Davomat",   "Явка",       "Attendance", Icons.Default.HowToReg),
    NavTab("baholash", "Baholash",  "Оценки",     "Assessment", Icons.Default.Assessment),
    NavTab("sinflar",  "Sinflarim", "Классы",     "Classes",    Icons.Default.People),
)

val parentTabs = listOf(
    NavTab("jadval",   "Jadval",   "Расписание", "Schedule",   Icons.Default.TableChart),
    NavTab("davomat",  "Davomat",  "Явка",       "Attendance", Icons.Default.CalendarToday),
    NavTab("baholar",  "Baholar",  "Оценки",     "Grades",     Icons.Default.Star),
    NavTab("vazifa",   "Vazifa",   "Задание",    "Homework",   Icons.Default.Home),
    NavTab("xulq",     "Xulq",     "Поведение",  "Behavior",   Icons.Default.Favorite),
    NavTab("shikoyat", "Shikoyat", "Жалоба",     "Complaint",  Icons.Default.Feedback),
)

val chefDrawerItems = listOf(
    DrawerItem("dashboard",  "Bosh panel",            Icons.Default.Dashboard,      "Asosiy"),
    DrawerItem("ombor",      "Oziq-ovqat ombori",     Icons.Default.Inventory,      "Asosiy"),
    DrawerItem("ingredient", "Ingredientlar",          Icons.Default.Grass,          "Asosiy"),
    DrawerItem("retsept",    "Taom retseptlari",       Icons.Default.RestaurantMenu, "Taomlar"),
    DrawerItem("menyu",      "Menyu kalendari",        Icons.Default.CalendarMonth,  "Taomlar"),
    DrawerItem("harakat",    "Stock harakatlari",      Icons.Default.SwapVert,       "Hisobotlar"),
    DrawerItem("analitika",  "Kafeteriya analitikasi", Icons.Default.BarChart,       "Hisobotlar"),
)

// ─────────────────────────────────────────────
// ROOT
// ─────────────────────────────────────────────

@Composable
fun MaktabApp() {
    val context = LocalContext.current
    val prefs = remember { getSecurePrefs(context) }

    // SharedPreferences dan o'qi
    var savedPin  by remember { mutableStateOf(prefs.getString("pin", "") ?: "") }
    var savedRole by remember { mutableStateOf(prefs.getString("role", "") ?: "") }
    var isDark    by remember { mutableStateOf(false) }
    var language  by remember { mutableStateOf("uz") }
    var screen    by remember { mutableStateOf<Screen>(Screen.Splash) }

    // PIN va rolni saqlash yordamchi funksiyalari
    fun saveSession(pin: String, role: String) {
        prefs.edit().putString("pin", pin).putString("role", role).apply()
        savedPin = pin
        savedRole = role
    }
    fun clearSession() {
        prefs.edit().remove("pin").remove("role").apply()
        savedPin = ""
        savedRole = ""
    }

    MaktabTheme(isDark = isDark, language = language) {
        when (val s = screen) {
            Screen.Splash -> SplashScreen(onFinished = {
                screen = if (savedPin.isNotEmpty() && savedRole.isNotEmpty())
                    Screen.PinEntry(savedRole)
                else
                    Screen.RoleSelect
            })

            Screen.RoleSelect -> LandingScreen(onRoleSelected = { role ->
                savedRole = role
                screen = Screen.Login(role)
            })

            is Screen.Login -> {
                // Orqaga: RoleSelect ga qaytish
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
                onPinSet = { pin ->
                    saveSession(pin, s.role)   // EncryptedSharedPreferences ga saqlash
                    screen = Screen.Dashboard(s.role)
                }
            )

            is Screen.PinEntry -> PinEntryScreen(
                role = s.role,
                savedPin = savedPin,
                userName = when (s.role) {
                    "teacher" -> "Karimova Nargiza"
                    "chef"    -> "Toshmatov Sardor"
                    else      -> "Karimov Bobur"
                },
                onSuccess = { screen = Screen.Dashboard(s.role) },
                onForgotPin = { clearSession(); screen = Screen.RoleSelect }
            )

            is Screen.Dashboard -> {
                // Dashboard dan ikki marta orqaga bosganda chiqish
                var backPressedOnce by remember { mutableStateOf(false) }
                BackHandler {
                    if (backPressedOnce) {
                        // Ilova minimallashadi (finish yo'q, faqat background ga o'tadi)
                        backPressedOnce = false
                    } else {
                        backPressedOnce = true
                        // 2 soniyadan keyin reset
                        kotlinx.coroutines.GlobalScope.launch {
                            kotlinx.coroutines.delay(2000)
                            backPressedOnce = false
                        }
                    }
                }
                RoleApp(
                    role = s.role,
                    language = language,
                    isDark = isDark,
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
    if (role == "chef") {
        ChefApp(language, isDark, onToggleDark, onLanguageChange, onLogout)
    } else {
        BottomNavApp(role, language, isDark, onToggleDark, onLanguageChange, onLogout)
    }
}

// ─────────────────────────────────────────────
// CHEF — ModalNavigationDrawer
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChefApp(
    language: String, isDark: Boolean,
    onToggleDark: () -> Unit, onLanguageChange: (String) -> Unit, onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedId by remember { mutableStateOf("dashboard") }
    var showSettings by remember { mutableStateOf(false) }
    val accent = Amber10
    val currentTitle = chefDrawerItems.find { it.id == selectedId }?.labelUz ?: "Bosh panel"

    // Drawer ochiq bo'lsa orqaga tugma uni yopsin
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }
    // Sozlamalar ochiq bo'lsa orqaga tugma uni yopsin
    BackHandler(enabled = showSettings) {
        showSettings = false
    }

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
                        .background(AmberContainer)
                        .padding(20.dp)
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    Column {
                        Box(
                            modifier = Modifier.size(52.dp).clip(CircleShape).background(Amber10.copy(0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Restaurant, null, tint = Amber10, modifier = Modifier.size(26.dp))
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("Toshmatov Sardor", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Amber10)
                        Text("Oshpaz · Kafeteriya", fontSize = 12.sp, color = Amber10.copy(0.7f))
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Guruh menyu
                chefDrawerItems.map { it.group }.distinct().forEach { group ->
                    Text(
                        text = group.uppercase(),
                        fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                    chefDrawerItems.filter { it.group == group }.forEach { item ->
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
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp), color = Outline, thickness = 0.5.dp)
                }

                Spacer(Modifier.weight(1f))

                // Sozlamalar va Chiqish — navigationBarsPadding bilan
                Column(modifier = Modifier.navigationBarsPadding().padding(bottom = 8.dp)) {
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
                            Text("Toshmatov Sardor · Kafeteriya", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    actions = {
                        Box(Modifier.padding(end = 4.dp)) {
                            Surface(shape = RoundedCornerShape(6.dp), color = accent.copy(0.1f)) {
                                Text(language.uppercase(), modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accent)
                            }
                        }
                        IconButton(onClick = { showSettings = true }) {
                            Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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

// ─────────────────────────────────────────────
// BOTTOM NAV — Teacher & Parent
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavApp(
    role: String, language: String, isDark: Boolean,
    onToggleDark: () -> Unit, onLanguageChange: (String) -> Unit, onLogout: () -> Unit
) {
    val isTeacher = role == "teacher"
    val tabs = if (isTeacher) teacherTabs else parentTabs
    val accent = if (isTeacher) Teal10 else Blue10
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }

    BackHandler(enabled = showSettings) { showSettings = false }

    if (showSettings) {
        SozlamalarScreen(
            role = role, isDark = isDark, language = language,
            onToggleDark = onToggleDark, onLanguageChange = onLanguageChange, onLogout = onLogout
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when {
                                isTeacher -> when (language) { "ru" -> "Панель учителя"; "en" -> "Teacher Panel"; else -> "O'qituvchi paneli" }
                                else      -> when (language) { "ru" -> "Панель родителя"; "en" -> "Parent Panel";  else -> "Ota-ona paneli" }
                            },
                            fontSize = 15.sp, fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isTeacher) "Karimova Nargiza · 5-A" else "Karimov Bobur · Asilbek otasi",
                            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    Box(Modifier.padding(start = 12.dp).size(36.dp), Alignment.Center) {
                        Surface(shape = RoundedCornerShape(10.dp), color = accent.copy(0.12f)) {
                            Icon(
                                if (isTeacher) Icons.Default.School else Icons.Default.FamilyRestroom,
                                null, tint = accent, modifier = Modifier.padding(8.dp).size(20.dp)
                            )
                        }
                    }
                },
                actions = {
                    Box(Modifier.padding(end = 4.dp)) {
                        Surface(shape = RoundedCornerShape(6.dp), color = accent.copy(0.1f)) {
                            Text(language.uppercase(), modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accent)
                        }
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                tabs.forEachIndexed { idx, tab ->
                    NavigationBarItem(
                        selected = selectedTabIndex == idx,
                        onClick = { selectedTabIndex = idx },
                        icon = { Icon(tab.icon, null, Modifier.size(22.dp)) },
                        label = { Text(tab.label(language), fontSize = 9.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = accent, selectedTextColor = accent,
                            indicatorColor = accent.copy(0.12f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            val tabId = tabs[selectedTabIndex].id
            if (isTeacher) {
                when (tabId) {
                    "jadval"   -> TeacherScheduleScreen()
                    "jurnal"   -> JurnalScreen(language)
                    "kontent"  -> DarsKontentiScreen(language)
                    "davomat"  -> DavomatScreen()
                    "baholash" -> BaholashScreen(language)
                    "sinflar"  -> SinflarimScreen(language)
                    else       -> TeacherScheduleScreen()
                }
            } else {
                when (tabId) {
                    "jadval"   -> StudentScheduleScreen()
                    "davomat"  -> ParentDavomatScreen()
                    "baholar"  -> BaholarScreen()
                    "vazifa"   -> ParentUygaVazifaScreen()
                    "xulq"     -> XulqScreen()
                    "shikoyat" -> ShikoyatScreen()
                    else       -> StudentScheduleScreen()
                }
            }
        }
    }
}