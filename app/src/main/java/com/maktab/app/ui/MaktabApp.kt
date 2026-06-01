package com.maktab.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maktab.app.ui.screens.*
import com.maktab.app.ui.theme.*

sealed class Screen {
    object Splash : Screen(); object RoleSelect : Screen()
    data class Login(val role: String) : Screen()
    data class PinSetup(val role: String) : Screen()
    data class PinEntry(val role: String) : Screen()
    data class Dashboard(val role: String) : Screen()
}

data class NavTab(val id: String, val labelUz: String, val labelRu: String, val labelEn: String, val icon: ImageVector) {
    fun label(lang: String) = when(lang) { "ru" -> labelRu; "en" -> labelEn; else -> labelUz }
}

val teacherTabs = listOf(
    NavTab("jadval",  "Jadval",       "Расписание",  "Schedule",   Icons.Default.TableChart),
    NavTab("jurnal",  "Jurnal",        "Журнал",      "Journal",    Icons.Default.GridOn),
    NavTab("kontent", "Dars",         "Контент",     "Content",    Icons.Default.MenuBook),
    NavTab("davomat", "Davomat",      "Явка",        "Attendance", Icons.Default.HowToReg),
    NavTab("baholash","Baholash",     "Оценки",      "Assessment", Icons.Default.Assessment),
    NavTab("sinflar", "Sinflarim",    "Классы",      "Classes",    Icons.Default.People),
)
val parentTabs = listOf(
    NavTab("jadval",  "Jadval",       "Расписание",  "Schedule",   Icons.Default.TableChart),
    NavTab("davomat", "Davomat",      "Явка",        "Attendance", Icons.Default.CalendarToday),
    NavTab("baholar", "Baholar",      "Оценки",      "Grades",     Icons.Default.Star),
    NavTab("vazifa",  "Vazifa",       "Задание",     "Homework",   Icons.Default.Home),
    NavTab("xulq",    "Xulq",         "Поведение",   "Behavior",   Icons.Default.Favorite),
    NavTab("shikoyat","Shikoyat",     "Жалоба",      "Complaint",  Icons.Default.Feedback),
)

@Composable
fun MaktabApp() {
    var screen   by remember { mutableStateOf<Screen>(Screen.Splash) }
    var savedPin  by remember { mutableStateOf("") }
    var savedRole by remember { mutableStateOf("") }
    var isDark    by remember { mutableStateOf(false) }
    var language  by remember { mutableStateOf("uz") }

    MaktabTheme(isDark = isDark, language = language) {
        when (val s = screen) {
            Screen.Splash -> SplashScreen(onFinished = {
                screen = if (savedPin.isNotEmpty() && savedRole.isNotEmpty()) Screen.PinEntry(savedRole) else Screen.RoleSelect
            })
            Screen.RoleSelect -> LandingScreen(onRoleSelected = { role -> savedRole = role; screen = Screen.Login(role) })
            is Screen.Login -> LoginScreen(role = s.role, onSuccess = {
                screen = if (savedPin.isEmpty()) Screen.PinSetup(s.role) else Screen.Dashboard(s.role)
            }, onBack = { screen = Screen.RoleSelect })
            is Screen.PinSetup -> PinSetupScreen(role = s.role, onPinSet = { pin -> savedPin = pin; screen = Screen.Dashboard(s.role) })
            is Screen.PinEntry -> PinEntryScreen(role = s.role, savedPin = savedPin,
                userName = if (s.role == "teacher") "Karimova Nargiza" else "Karimov Bobur",
                onSuccess = { screen = Screen.Dashboard(s.role) },
                onForgotPin = { savedPin = ""; screen = Screen.RoleSelect })
            is Screen.Dashboard -> RoleApp(
                role = s.role, language = language, isDark = isDark,
                onToggleDark = { isDark = !isDark },
                onLanguageChange = { language = it },
                onLogout = { savedPin = ""; savedRole = ""; screen = Screen.RoleSelect }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleApp(
    role: String, language: String, isDark: Boolean,
    onToggleDark: () -> Unit, onLanguageChange: (String) -> Unit, onLogout: () -> Unit
) {
    val isTeacher = role == "teacher"
    val tabs = if (isTeacher) teacherTabs else parentTabs
    val accent = if (isTeacher) Teal10 else Blue10
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        SozlamalarScreen(
            role = role, isDark = isDark, language = language,
            onToggleDark = onToggleDark,
            onLanguageChange = onLanguageChange,
            onLogout = onLogout
        )
        // Back button overlay
        Box(modifier = Modifier.fillMaxSize()) {
            SmallFloatingActionButton(
                onClick = { showSettings = false },
                modifier = androidx.compose.ui.Modifier.padding(16.dp).align(Alignment.BottomEnd),
                containerColor = accent,
                contentColor = androidx.compose.ui.graphics.Color.White
            ) { Icon(Icons.Default.ArrowBack, null) }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when {
                                isTeacher -> when(language){"ru"->"Панель учителя";"en"->"Teacher Panel";else->"O'qituvchi paneli"}
                                else -> when(language){"ru"->"Панель родителя";"en"->"Parent Panel";else->"Ota-ona paneli"}
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
                        Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp), color = accent.copy(0.12f)) {
                            Icon(if (isTeacher) Icons.Default.School else Icons.Default.FamilyRestroom, null, tint = accent, modifier = Modifier.padding(8.dp).size(20.dp))
                        }
                    }
                },
                actions = {
                    // Lang indicator
                    Box(Modifier.padding(end = 4.dp)) {
                        Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp), color = accent.copy(0.1f)) {
                            Text(language.uppercase(), modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accent)
                        }
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = when(language){"ru"->"Настройки";"en"->"Settings";else->"Sozlamalar"}, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    "jadval"  -> TeacherScheduleScreen()
                    "jurnal"  -> JurnalScreen(language)
                    "kontent" -> DarsKontentiScreen(language)
                    "davomat" -> DavomatScreen()
                    "baholash"-> BaholashScreen(language)
                    "sinflar" -> SinflarimScreen(language)
                    else      -> TeacherScheduleScreen()
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
