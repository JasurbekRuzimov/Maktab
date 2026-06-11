package com.maktab.app.ui.screens.parent

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maktab.app.data.MockData
import com.maktab.app.network.ApiResult
import com.maktab.app.ui.components.*
import com.maktab.app.ui.theme.*
import com.maktab.app.ui.theme.str
import com.maktab.app.viewmodel.ParentViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────
// MOCK DATA — Parent uchun
// ─────────────────────────────────────────────

private data class ExamResult(
    val subject: String, val title: String, val date: String,
    val score: Int, val maxScore: Int, val grade: String, val status: String
)

private data class PaymentItem(
    val id: Int, val title: String, val amount: String,
    val date: String, val status: String, val type: String
)

private data class NewsItem(
    val id: Int, val title: String, val body: String,
    val date: String, val category: String, val isRead: Boolean
)

private data class BlogPost(
    val id: Int, val title: String, val author: String,
    val date: String, val preview: String, val likes: Int
)

private data class SurveyItem(
    val id: Int, val title: String, val question: String,
    val options: List<String>, val deadline: String, val answered: Boolean
)

private data class MurojaatItem(
    val id: Int, val title: String, val category: String,
    val date: String, val status: String, val reply: String?
)

private object ParentMock {
    val examResults = listOf(
        ExamResult("Matematika", "1-chorak yakuniy imtihon", "18.05.2026", 87, 100, "4", "Baholandi"),
        ExamResult("Ingliz tili", "Grammar nazorat ishi", "17.05.2026", 73, 100, "3", "Baholandi"),
        ExamResult("Fizika", "Newton qonunlari testi", "15.05.2026", 92, 100, "5", "Baholandi"),
        ExamResult("Biologiya", "Hujayra tuzilishi", "14.05.2026", 0, 100, "-", "Kutilmoqda"),
    )
    val payments = listOf(
        PaymentItem(1, "Aprel oyi to'lovi", "250 000 UZS", "01.04.2026", "To'langan", "oylik"),
        PaymentItem(2, "May oyi to'lovi", "250 000 UZS", "01.05.2026", "To'langan", "oylik"),
        PaymentItem(3, "Iyun oyi to'lovi", "250 000 UZS", "01.06.2026", "Kutilmoqda", "oylik"),
        PaymentItem(4, "Kitoblar uchun", "85 000 UZS", "15.03.2026", "To'langan", "qo'shimcha"),
        PaymentItem(5, "Sport to'garak", "50 000 UZS", "01.06.2026", "Kutilmoqda", "qo'shimcha"),
    )
    val news = listOf(
        NewsItem(1, "Yozgi ta'til jadvali e'lon qilindi", "2026-2027 o'quv yili uchun yozgi ta'til 15-iyundan boshlanadi. Barcha ota-onalar e'tiborga olsinlar.", "02.06.2026", "E'lon", false),
        NewsItem(2, "Olimpiada natijalari", "Matematika olimpiadasida maktabimiz o'quvchilari 3 ta medal qo'lga kiritdi.", "01.06.2026", "Yangilik", true),
        NewsItem(3, "Ota-onalar yig'ilishi", "5-A sinf ota-onalar yig'ilishi 5-iyun kuni soat 17:00 da bo'lib o'tadi.", "30.05.2026", "E'lon", true),
        NewsItem(4, "Sport musobaqasi", "Maktablararo futbol musobaqasida jamoamiz g'olib bo'ldi!", "28.05.2026", "Yangilik", true),
    )
    val blogs = listOf(
        BlogPost(1, "Farzandingizga o'qishni sevdirish yo'llari", "Psixolog Aziza Rahimova", "30.05.2026", "Har bir bola o'z tezligida o'rganadi. Ota-onalar sabr bilan...", 24),
        BlogPost(2, "Raqamli texnologiyalar va ta'lim", "O'qituvchi Bobur Toshev", "25.05.2026", "Zamonaviy ta'limda texnologiyalardan foydalanish...", 18),
        BlogPost(3, "Bolalarda stressni kamaytirish", "Psixolog Malika Yusupova", "20.05.2026", "Imtihon davrida bolalar ko'p stress his qiladi...", 31),
    )
    val surveys = listOf(
        SurveyItem(1, "Maktab xizmatlari sifati", "Maktab xizmatlaridan qanchalik mamnunsiz?", listOf("Juda mamnun", "Mamnun", "O'rtacha", "Mamnun emas"), "10.06.2026", false),
        SurveyItem(2, "Darslik sifatini baholang", "Joriy darsliklar bolangizga qanchalik qulay?", listOf("A'lo", "Yaxshi", "Qoniqarli", "Yomon"), "08.06.2026", true),
        SurveyItem(3, "Ovqatlanish sifati", "Maktab kafeterisidagi ovqat sifatini baholang", listOf("Ajoyib", "Yaxshi", "O'rtacha", "Yomon"), "05.06.2026", false),
    )
    val murojaatlar = listOf(
        MurojaatItem(1, "Dars jadvalidagi xatolik", "O'quv jarayoni", "25.05.2026", "Javob berildi", "Jadval tuzatildi. E'tiboringiz uchun rahmat."),
        MurojaatItem(2, "Ovqatlanish sifati haqida", "Infratuzilma", "20.05.2026", "Ko'rib chiqilmoqda", null),
        MurojaatItem(3, "O'qituvchi munosabati", "O'qituvchi", "15.05.2026", "Yopildi", "Muammo hal qilindi. Aloqa uchun rahmat."),
    )
}

private fun initials(name: String): String =
    name.trim().split(" ").take(2)
        .joinToString("") { it.firstOrNull()?.uppercase() ?: "" }

// ─────────────────────────────────────────────
// 1. PARENT DASHBOARD
// ─────────────────────────────────────────────

@Composable
fun ParentDashboardScreen(vm: ParentViewModel = viewModel()) {
    val childState by vm.childState.collectAsState()

    LaunchedEffect(Unit) { vm.loadDashboard() }

    val child = (childState as? ApiResult.Success)?.data

    val attTotal = child?.attendance?.total ?: 0
    val attPresent = child?.attendance?.present ?: 0
    val attendancePct = if (attTotal > 0) (attPresent * 100 / attTotal) else 0
    val avgGrade = child?.grades?.avgPercent?.toInt() ?: 0
    val homeworkDone  = child?.homework?.submitted ?: 0
    val homeworkTotal = child?.homework?.total ?: 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Farzand kartochkasi
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlueContainer),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(0.5.dp, Blue10.copy(0.3f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(str.linkedChildren, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = Blue10, letterSpacing = 0.8.sp)
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AvatarCircle(child?.fullname?.split(" ")?.take(2)?.joinToString("") { it.firstOrNull()?.uppercase() ?: "" } ?: "?", Blue10, 52.dp)
                        Column(Modifier.weight(1f)) {
                            Text(child?.fullname ?: "Farzand", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Blue10)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Badge, null, tint = Blue10.copy(0.6f), modifier = Modifier.size(12.dp))
                                Text("${child?.className ?: "Sinf biriktirilmagan"}", fontSize = 11.sp, color = Blue10.copy(0.7f))
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                StatusChip("Faol", Teal10, TealContainer)
                                StatusChip("6 yosh", Blue10, BlueContainer.copy(0.5f))
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ParentQuickStat("Davomat", if (childState is ApiResult.Loading) "..." else "$attendancePct%", Icons.Default.CalendarToday, Blue10, Modifier.weight(1f))
                        ParentQuickStat("Baholar", if (childState is ApiResult.Loading) "..." else "$avgGrade%", Icons.Default.Star, Amber10, Modifier.weight(1f))
                        ParentQuickStat("Uy vazifalari", if (childState is ApiResult.Loading) "..." else "$homeworkDone/$homeworkTotal", Icons.Default.MenuBook, Teal10, Modifier.weight(1f))
                        ParentQuickStat("Tangalar", "0", Icons.Default.MonetizationOn, Red10, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {}, modifier = Modifier.weight(1f).height(34.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp),
                            border = BorderStroke(0.5.dp, Blue10.copy(0.4f))
                        ) { Text(str.viewAttendance, fontSize = 11.sp, color = Blue10) }
                        OutlinedButton(
                            onClick = {}, modifier = Modifier.weight(1f).height(34.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp),
                            border = BorderStroke(0.5.dp, Blue10.copy(0.4f))
                        ) { Text(str.viewGrades, fontSize = 11.sp, color = Blue10) }
                        OutlinedButton(
                            onClick = {}, modifier = Modifier.weight(1f).height(34.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp),
                            border = BorderStroke(0.5.dp, Blue10.copy(0.4f))
                        ) { Text(str.viewPayments, fontSize = 11.sp, color = Blue10) }
                    }
                }
            }
        }

        // Oxirgi yangiliklar
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                SectionHeader("Oxirgi yangiliklar") {}
                Spacer(Modifier.height(8.dp))
            }
        }
        // Yangiliklar hali API dan kelmaydi
        item {
            Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp)) {
                Text(str.newsComingSoon, fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // To'lovlar — API dan
        child?.payments?.let { pay ->
            if ((pay.contractsCount) > 0 || pay.nextPaymentDate != null) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        SectionHeader("To'lovlar") {}
                        Spacer(Modifier.height(8.dp))
                    }
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.5.dp, Outline),
                        elevation = CardDefaults.cardElevation(0.dp)) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(40.dp).clip(RoundedCornerShape(9.dp)).background(
                                if (pay.nextPaymentDate != null) AmberContainer else TealContainer),
                                contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Payment, null,
                                    tint = if (pay.nextPaymentDate != null) Amber10 else Teal10,
                                    modifier = Modifier.size(20.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(pay.activeContract ?: "Shartnoma", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                pay.nextPaymentDate?.let { date ->
                                    Text("Keyingi to'lov: $date", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text("${"%.0f".format(pay.totalAmount)} so'm", fontSize = 13.sp,
                                fontWeight = FontWeight.Bold, color = Teal10)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun ParentQuickStat(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(0.7f))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 9.sp, color = color.copy(0.7f), textAlign = TextAlign.Center, lineHeight = 12.sp)
    }
}

// ─────────────────────────────────────────────
// 2. PROFIL
// ─────────────────────────────────────────────

@Composable
fun ParentProfilScreen(vm: ParentViewModel = viewModel()) {
    val profile by vm.profile.collectAsState()
    LaunchedEffect(Unit) { vm.loadDashboard() }
    var joriyParol by remember { mutableStateOf("") }
    var yangiParol by remember { mutableStateOf("") }
    var tasdiqlash by remember { mutableStateOf("") }
    var joriyVisible by remember { mutableStateOf(false) }
    var yangiVisible by remember { mutableStateOf(false) }
    var tasdiqlashVisible by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Column(Modifier.padding(16.dp)) {
                SectionHeader("Profil") {}
                Spacer(Modifier.height(12.dp))

                // Profil kartochkasi
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.5.dp, Outline),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(64.dp).clip(CircleShape).background(BlueContainer), contentAlignment = Alignment.Center) {
                            Text(profile?.fullname?.let { initials(it) } ?: "?", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Blue10)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(profile?.fullname ?: "Foydalanuvchi", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(2.dp))
                            Text(str.parentRole, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(6.dp))
                            Box(Modifier.clip(RoundedCornerShape(6.dp)).background(BlueContainer).padding(horizontal = 10.dp, vertical = 3.dp)) {
                                Text("${profile?.childrenCount ?: 0} ta farzand biriktirilgan", fontSize = 11.sp, color = Blue10, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Parolni almashtirish
                Text(str.security, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.8.sp)
                Spacer(Modifier.height(8.dp))
                Text(str.changePassword, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(str.passwordHint, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))

                ParolField("Joriy parol", joriyParol, { joriyParol = it }, joriyVisible, { joriyVisible = !joriyVisible })
                Spacer(Modifier.height(12.dp))
                ParolField("Yangi parol", yangiParol, { yangiParol = it }, yangiVisible, { yangiVisible = !yangiVisible })
                Spacer(Modifier.height(12.dp))
                ParolField("Parolni tasdiqlang", tasdiqlash, { tasdiqlash = it }, tasdiqlashVisible, { tasdiqlashVisible = !tasdiqlashVisible })
                Spacer(Modifier.height(6.dp))
                Text(str.passwordStrongHint,
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                Spacer(Modifier.height(16.dp))

                if (saved) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(TealContainer).padding(12.dp)) {
                        Icon(Icons.Default.CheckCircle, null, tint = Teal10, modifier = Modifier.size(18.dp))
                        Text(str.passwordChanged, fontSize = 13.sp, color = Teal10)
                    }
                    Spacer(Modifier.height(12.dp))
                }

                Button(
                    onClick = {
                        if (yangiParol.isNotEmpty()) {
                            scope.launch { saved = true; delay(3000); saved = false }
                        }
                    },
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue10),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Lock, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(str.savePassword, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun ParolField(label: String, value: String, onValueChange: (String) -> Unit, visible: Boolean, onToggle: () -> Unit) {
    Column {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            shape = RoundedCornerShape(10.dp),
            visualTransformation = if (visible) VisualTransformation.None
            else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onToggle) {
                    Icon(if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null,
                        modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Blue10, unfocusedBorderColor = Outline)
        )
    }
}

// ─────────────────────────────────────────────
// 3. DAVOMAT
// ─────────────────────────────────────────────

@Composable
fun ParentDavomatScreen(vm: ParentViewModel = viewModel()) {
    val childState by vm.childState.collectAsState()
    LaunchedEffect(Unit) { vm.loadDashboard() }
    val child = (childState as? ApiResult.Success)?.data
    val presentCount = child?.attendance?.present ?: 0
    val absentCount  = child?.attendance?.absent  ?: 0
    val latCount     = child?.attendance?.late    ?: 0
    val totalDays    = child?.attendance?.total   ?: 0
    val attendancePct = if (totalDays > 0) (presentCount * 100 / totalDays) else 0
    val dayH = listOf("D", "S", "Ch", "P", "J", "Sh", "Y")

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SectionHeader("${child?.fullname ?: "Farzand"} — Davomat") {} }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Jami kun", "$totalDays", Blue10, Modifier.weight(1f))
                StatCard("Keldi", "$presentCount", Teal10, Modifier.weight(1f))
                StatCard("Kelmadi", "$absentCount", Red10, Modifier.weight(1f))
            }
        }
        item {
            // Progress bar
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp), border = BorderStroke(0.5.dp, Outline), elevation = CardDefaults.cardElevation(0.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(str.attendanceRate, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("$attendancePct%", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                            color = if (attendancePct >= 80) Teal10 else Red10)
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        Box(Modifier.fillMaxWidth(attendancePct / 100f).fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (attendancePct >= 80) Teal10 else Red10))
                    }
                }
            }
        }
        item {
            AppCard {
                Text("May 2026", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth()) {
                    dayH.forEach { d ->
                        Text(d, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                            fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(4.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    userScrollEnabled = false
                ) {
                    items(35) { idx ->
                        val di = idx - 4
                        // TODO: real calendar data API dan
                        val cd: Map<String, Any>? = null 
                        if (cd != null) {
                            val status = cd["status"]?.toString() ?: ""
                            val day = cd["day"]?.toString() ?: ""
                            val (bg, tc) = when (status) {
                                "present" -> Pair(TealContainer, Teal10)
                                "absent"  -> Pair(RedContainer, Red10)
                                "today"   -> Pair(AmberContainer, Amber10)
                                else      -> Pair(Color.Transparent, MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Box(Modifier.aspectRatio(1f).clip(RoundedCornerShape(6.dp)).background(bg), contentAlignment = Alignment.Center) {
                                Text(day, fontSize = 12.sp, color = tc, fontWeight = if (status == "today") FontWeight.Bold else FontWeight.Normal)
                            }
                        } else { Box(Modifier.aspectRatio(1f)) }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    listOf(Triple("Keldi", Teal10, TealContainer), Triple("Kelmadi", Red10, RedContainer), Triple("Bugun", Amber10, AmberContainer)).forEach { (l, c, bg) ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(bg).border(0.5.dp, c, RoundedCornerShape(3.dp)))
                            Text(l, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 4. BAHOLAR
// ─────────────────────────────────────────────

@Composable
fun BaholarScreen(vm: ParentViewModel = viewModel()) {
    val childState by vm.childState.collectAsState()
    LaunchedEffect(Unit) { vm.loadDashboard() }
    val child = (childState as? ApiResult.Success)?.data
    val gc = listOf(Pair(Blue10, BlueContainer), Pair(Purple10, PurpleContainer), Pair(Amber10, AmberContainer), Pair(Green10, GreenContainer))
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SectionHeader("Baholar va Reyting") {} }
        // API dan baholar hali bo'sh (backend da ma'lumot yo'q)
        // Hozircha bo'sh holat ko'rsatamiz
        if ((child?.grades?.completedCount ?: 0) == 0) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                        Text(str.noGrades, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        // TODO: real baholar keladigan bo'lsa shu qism o'chadi
        if (false) itemsIndexed(MockData.grades) { idx, g ->
            val (color, container) = gc[idx]
            AppCard {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(container), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.MenuBook, null, tint = color, modifier = Modifier.size(20.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(g.subject, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                            Text("${g.avg}", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = color)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            g.scores.forEach { sc ->
                                Box(Modifier.weight(1f).clip(RoundedCornerShape(5.dp)).background(container).padding(vertical = 3.dp), contentAlignment = Alignment.Center) {
                                    Text("$sc", fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text(str.classRanking, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp), border = BorderStroke(0.5.dp, Outline), elevation = CardDefaults.cardElevation(0.dp)) {
                Column {
                    // TODO: real student ranking API dan
                    emptyList<Map<String, Any>>().forEachIndexed { idx, student ->
                        val isMe = false
                        val sName = student["name"]?.toString() ?: ""
                        val sInitials = initials(sName)
                        val sScore = student["score"]?.toString() ?: "0"
                        if (idx > 0) HorizontalDivider(color = Outline, thickness = 0.5.dp)
                        Row(Modifier.fillMaxWidth().background(if (isMe) TealContainer.copy(0.4f) else Color.Transparent)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(Modifier.size(26.dp).clip(CircleShape).background(if (idx < 3) AmberContainer else MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                Text("${idx + 1}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (idx < 3) Amber10 else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            AvatarCircle(sInitials, if (isMe) Teal10 else Blue10, 30.dp)
                            Text(sName, fontSize = 13.sp, fontWeight = if (isMe) FontWeight.SemiBold else FontWeight.Normal, modifier = Modifier.weight(1f))
                            if (isMe) StatusChip("Siz", Teal10, TealContainer)
                            Text(sScore, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = if (isMe) Teal10 else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 5. UY VAZIFALARI
// ─────────────────────────────────────────────

@Composable
fun ParentUygaVazifaScreen(vm: ParentViewModel = viewModel()) {
    val childState by vm.childState.collectAsState()
    LaunchedEffect(Unit) { vm.loadDashboard() }
    val child = (childState as? ApiResult.Success)?.data
    val hw = child?.homework
    val hwC = listOf(Pair(Blue10, BlueContainer), Pair(Purple10, PurpleContainer), Pair(Amber10, AmberContainer))
    val scope = rememberCoroutineScope()
    var submitting by remember { mutableStateOf<Int?>(null) }
    val submitted = remember { mutableStateListOf<Int>() }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SectionHeader("Uyga vazifalar") {} }

        if (childState is ApiResult.Loading) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), Alignment.Center) {
                    CircularProgressIndicator(color = Blue10, strokeWidth = 2.dp)
                }
            }
        } else if ((hw?.total ?: 0) == 0) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.MenuBook, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                        Text(str.noHomework, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            // Statistika
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Jami", "${hw?.total ?: 0}", Blue10, Modifier.weight(1f))
                    StatCard("Topshirildi", "${hw?.submitted ?: 0}", Teal10, Modifier.weight(1f))
                    StatCard("Kutilmoqda", "${hw?.pending ?: 0}", Amber10, Modifier.weight(1f))
                }
            }
            // So'nggi vazifalar
            if (!hw?.recent.isNullOrEmpty()) {
                item { Text(str.recentHomework, fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
                itemsIndexed(hw!!.recent) { idx, h ->
                    val (color, container) = hwC[idx % hwC.size]
                    val statusColor = when(h.status.lowercase()) {
                        "submitted", "topshirildi" -> Teal10
                        "late", "kech" -> Red10
                        else -> Amber10
                    }
                    val statusBg = when(h.status.lowercase()) {
                        "submitted", "topshirildi" -> TealContainer
                        "late", "kech" -> RedContainer
                        else -> AmberContainer
                    }
                    val statusLabel = when(h.status.lowercase()) {
                        "submitted" -> "Topshirildi"
                        "late" -> "Kech"
                        "pending" -> "Kutilmoqda"
                        else -> h.status
                    }
                    AppCard {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(container), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.MenuBook, null, tint = color, modifier = Modifier.size(20.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(h.subject.ifEmpty { "Fan" }, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                    StatusChip(statusLabel, statusColor, statusBg)
                                }
                                if (h.title.isNotEmpty()) {
                                    Spacer(Modifier.height(3.dp))
                                    Text(h.title, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (h.dueDate.isNotEmpty()) {
                                    Spacer(Modifier.height(3.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.Schedule, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Muddat: ${h.dueDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                h.grade?.let { g ->
                                    Spacer(Modifier.height(4.dp))
                                    Text("Baho: ${"%.0f".format(g)}", fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
        if (false) itemsIndexed(MockData.childHomework) { idx, hw ->
            val done = hw.isDone || hw.id in submitted
            val (color, container) = hwC[idx % hwC.size]
            AppCard {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(container), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.MenuBook, null, tint = color, modifier = Modifier.size(20.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(hw.subject, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            StatusChip(if (done) "Bajarildi" else "Kutilmoqda", if (done) Teal10 else Amber10, if (done) TealContainer else AmberContainer)
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(hw.task, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Schedule, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Muddat: ${hw.deadline}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (!done) {
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = {}, modifier = Modifier.weight(1f).height(34.dp), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(0.dp)) {
                                    Icon(Icons.Default.Upload, null, Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)); Text(str.file, fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { scope.launch { submitting = hw.id; delay(1500); submitted.add(hw.id); submitting = null } },
                                    enabled = submitting != hw.id,
                                    modifier = Modifier.weight(1f).height(34.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = color),
                                    shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(if (submitting == hw.id) Icons.Default.HourglassEmpty else Icons.Default.Send, null, Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (submitting == hw.id) "Yuborilmoqda..." else "Topshirish", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 6. IMTIHONLAR
// ─────────────────────────────────────────────

@Composable
fun ParentImtihonlarScreen(vm: ParentViewModel = viewModel()) {
    val childState by vm.childState.collectAsState()
    LaunchedEffect(Unit) { vm.loadDashboard() }
    val child = (childState as? ApiResult.Success)?.data
    val exams = child?.exams
    val gc = listOf(Pair(Blue10, BlueContainer), Pair(Purple10, PurpleContainer), Pair(Teal10, TealContainer), Pair(Amber10, AmberContainer))

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SectionHeader("Imtihon natijalari") {} }

        if (childState is ApiResult.Loading) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), Alignment.Center) {
                    CircularProgressIndicator(color = Blue10, strokeWidth = 2.dp)
                }
            }
        } else if ((exams?.attemptsCount ?: 0) == 0) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Jami", "0", Blue10, Modifier.weight(1f))
                    StatCard("Baholandi", "0", Teal10, Modifier.weight(1f))
                    StatCard("Kutilmoqda", "0", Amber10, Modifier.weight(1f))
                }
            }
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Assignment, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                        Text(str.noExams, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Jami", "${exams?.attemptsCount ?: 0}", Blue10, Modifier.weight(1f))
                    StatCard("Baholandi", "${exams?.gradedCount ?: 0}", Teal10, Modifier.weight(1f))
                    StatCard("Kutilmoqda", "${(exams?.attemptsCount ?: 0) - (exams?.gradedCount ?: 0)}", Amber10, Modifier.weight(1f))
                }
            }
            exams?.avgPercent?.let { avg ->
                item {
                    AppCard {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(str.avgScore, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("${"%.1f".format(avg)}%", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                                color = if (avg >= 70) Teal10 else Red10)
                        }
                    }
                }
            }
            if (!exams?.recent.isNullOrEmpty()) {
                item { Text(str.recentExams, fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
                itemsIndexed(exams!!.recent) { idx, exam ->
                    val (color, container) = gc[idx % gc.size]
                    val isGraded = exam.status.lowercase() in listOf("graded", "baholandi", "completed")
                    Card(modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.5.dp, Outline),
                        elevation = CardDefaults.cardElevation(0.dp)) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(container), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Assignment, null, tint = color, modifier = Modifier.size(22.dp))
                                }
                                Column(Modifier.weight(1f)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(exam.subject.ifEmpty { exam.name }, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                        StatusChip(if (isGraded) "Baholandi" else "Kutilmoqda",
                                            if (isGraded) Teal10 else Amber10,
                                            if (isGraded) TealContainer else AmberContainer)
                                    }
                                    if (exam.name.isNotEmpty() && exam.name != exam.subject)
                                        Text(exam.name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (exam.date.isNotEmpty())
                                        Text(exam.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (isGraded && exam.score != null) {
                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider(color = Outline, thickness = 0.5.dp)
                                Spacer(Modifier.height(10.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("${"%.0f".format(exam.score)}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
                                        Text(str.ball, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    exam.maxScore?.let { max ->
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("${"%.0f".format(max)}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(str.max, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        val pct = (exam.score / max).toFloat().coerceIn(0f, 1f)
                                        Box(Modifier.size(52.dp).clip(CircleShape).background(container), contentAlignment = Alignment.Center) {
                                            Text("${"%.0f".format(pct * 100)}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
                                        }
                                    }
                                }
                                exam.maxScore?.let { max ->
                                    Spacer(Modifier.height(8.dp))
                                    val pct = (exam.score / max).toFloat().coerceIn(0f, 1f)
                                    Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                                        Box(Modifier.fillMaxWidth(pct).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(color))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 7. TO'LOVLAR


@Composable
private fun PaymentCard(p: PaymentItem, modifier: Modifier = Modifier) {
    val isPaid = p.status == "To'langan"
    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp), border = BorderStroke(0.5.dp, Outline), elevation = CardDefaults.cardElevation(0.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(9.dp)).background(if (isPaid) TealContainer else AmberContainer), contentAlignment = Alignment.Center) {
                Icon(if (isPaid) Icons.Default.CheckCircle else Icons.Default.Payment, null,
                    tint = if (isPaid) Teal10 else Amber10, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(p.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("${p.date} · ${p.type}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(p.amount, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isPaid) Teal10 else Amber10)
                StatusChip(p.status, if (isPaid) Teal10 else Amber10, if (isPaid) TealContainer else AmberContainer)
            }
        }
    }
}

// ─────────────────────────────────────────────
// 8. YANGILIKLAR
// ─────────────────────────────────────────────

@Composable
fun XulqScreen() {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SectionHeader("Xulq va xatti-harakat") {} }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Ijobiy", "2", Teal10, Modifier.weight(1f))
                StatCard("Neytral", "1", Amber10, Modifier.weight(1f))
                StatCard("Salbiy", "1", Red10, Modifier.weight(1f))
            }
        }
        item {
            Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), Alignment.Center) {
                Text(str.noBehaviorRecords, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (false) items(MockData.behaviorRecords) { rec ->
            val (icon, color, container) = when (rec.type) {
                "positive" -> Triple(Icons.Default.ThumbUp, Teal10, TealContainer)
                "negative" -> Triple(Icons.Default.Warning, Red10, RedContainer)
                else       -> Triple(Icons.Default.Remove, Amber10, AmberContainer)
            }
            AppCard {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(container), contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(rec.title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text(rec.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(rec.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Person, null, Modifier.size(11.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(rec.teacher, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 7. TO'LOVLAR
// ─────────────────────────────────────────────

@Composable
fun ParentTolovlarScreen(vm: ParentViewModel = viewModel()) {
    val childState by vm.childState.collectAsState()
    LaunchedEffect(Unit) { vm.loadDashboard() }
    val child = (childState as? ApiResult.Success)?.data
    val pay = child?.payments

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SectionHeader("To'lovlar") {} }

        if (childState is ApiResult.Loading) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), Alignment.Center) {
                    CircularProgressIndicator(color = Teal10, strokeWidth = 2.dp)
                }
            }
        } else {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Shartnomalar", "${pay?.contractsCount ?: 0}", Blue10, Modifier.weight(1f))
                    StatCard("Jami summa", if ((pay?.totalAmount ?: 0.0) > 0)
                        "${"%.0f".format(pay?.totalAmount)} so'm" else "0", Teal10, Modifier.weight(1f))
                }
            }
            // Faol shartnoma
            pay?.activeContract?.let { contract ->
                item {
                    Card(modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.5.dp, Teal10.copy(0.4f)),
                        elevation = CardDefaults.cardElevation(0.dp)) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(TealContainer),
                                contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Description, null, tint = Teal10, modifier = Modifier.size(22.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(str.activeContract, fontSize = 12.sp, color = Teal10, fontWeight = FontWeight.Medium)
                                Text(contract, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                            StatusChip("Faol", Teal10, TealContainer)
                        }
                    }
                }
            }
            // Keyingi to'lov sanasi
            pay?.nextPaymentDate?.let { date ->
                item {
                    Card(modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.5.dp, Amber10.copy(0.4f)),
                        elevation = CardDefaults.cardElevation(0.dp)) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(AmberContainer),
                                contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CalendarToday, null, tint = Amber10, modifier = Modifier.size(22.dp))
                            }
                            Column {
                                Text(str.nextPaymentDate, fontSize = 12.sp, color = Amber10, fontWeight = FontWeight.Medium)
                                Text(date, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
            // To'lov tarixi yo'q holat
            if ((pay?.contractsCount ?: 0) == 0) {
                item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Payment, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                            Text(str.noPayments, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
// ─────────────────────────────────────────────
// 8. YANGILIKLAR

// ─────────────────────────────────────────────
@Composable
fun ParentYangiliklar() {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SectionHeader("Yangiliklar va E'lonlar") {} }
        items(ParentMock.news) { n -> NewsCard(n) }
    }
}

@Composable
private fun NewsCard(n: NewsItem, modifier: Modifier = Modifier) {
    val catColor = when (n.category) { "E'lon" -> Pair(Blue10, BlueContainer); else -> Pair(Teal10, TealContainer) }
    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp), border = BorderStroke(0.5.dp, if (!n.isRead) Blue10.copy(0.3f) else Outline), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                StatusChip(n.category, catColor.first, catColor.second)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!n.isRead) Box(Modifier.size(8.dp).clip(CircleShape).background(Blue10))
                    Text(n.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(n.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(n.body, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
        }
    }
}

// ─────────────────────────────────────────────
// 9. BLOGLAR
// ─────────────────────────────────────────────

@Composable
fun ParentBloglar() {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SectionHeader("Bloglar") {} }
        items(ParentMock.blogs) { b ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp), border = BorderStroke(0.5.dp, Outline), elevation = CardDefaults.cardElevation(0.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text(b.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Person, null, tint = Blue10, modifier = Modifier.size(14.dp))
                        Text(b.author, fontSize = 12.sp, color = Blue10)
                        Text("·", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(b.date, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(b.preview, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Favorite, null, tint = Red10, modifier = Modifier.size(14.dp))
                        Text("${b.likes}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = {}, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(str.readMore, fontSize = 12.sp, color = Blue10)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 10. SO'ROVNOMALAR
// ─────────────────────────────────────────────

@Composable
fun ParentSurveylar() {
    val answers = remember { mutableStateMapOf<Int, Int>() }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SectionHeader("So'rovnomalar") {} }
        items(ParentMock.surveys) { s ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp), border = BorderStroke(0.5.dp, Outline), elevation = CardDefaults.cardElevation(0.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(s.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        StatusChip(
                            if (s.answered || answers.containsKey(s.id)) "Javob berildi" else "Kutilmoqda",
                            if (s.answered || answers.containsKey(s.id)) Teal10 else Amber10,
                            if (s.answered || answers.containsKey(s.id)) TealContainer else AmberContainer
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(s.question, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                        Text("Muddat: ${s.deadline}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (!s.answered && !answers.containsKey(s.id)) {
                        Spacer(Modifier.height(12.dp))
                        s.options.forEachIndexed { idx, opt ->
                            val sel = answers[s.id] == idx
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (sel) BlueContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { answers[s.id] = idx }.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(Modifier.size(20.dp).clip(CircleShape)
                                    .background(if (sel) Blue10 else Color.Transparent)
                                    .border(1.5.dp, if (sel) Blue10 else Outline, CircleShape),
                                    contentAlignment = Alignment.Center) {
                                    if (sel) Box(Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                                }
                                Text(opt, fontSize = 13.sp, color = if (sel) Blue10 else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (sel) FontWeight.Medium else FontWeight.Normal)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { if (answers.containsKey(s.id)) answers[s.id] = answers[s.id]!! },
                            enabled = answers.containsKey(s.id),
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue10),
                            shape = RoundedCornerShape(10.dp)
                        ) { Text(str.submitAnswer, fontSize = 13.sp) }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 11. MUROJAATLAR
// ─────────────────────────────────────────────

@Composable
fun ParentMurojaatlar() {
    var selCat by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    val cats = listOf("O'qituvchi munosabati", "Infratuzilma", "O'quv jarayoni", "Xavfsizlik", "Boshqa")

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SectionHeader("Murojaatlar") {} }
        if (ParentMock.murojaatlar.isNotEmpty()) {
            item { Text(str.myComplaints, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
            items(ParentMock.murojaatlar) { m ->
                val (sColor, sContainer) = when (m.status) {
                    "Javob berildi" -> Pair(Teal10, TealContainer)
                    "Yopildi"       -> Pair(MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
                    else            -> Pair(Amber10, AmberContainer)
                }
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp), border = BorderStroke(0.5.dp, Outline), elevation = CardDefaults.cardElevation(0.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(m.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            StatusChip(m.status, sColor, sContainer)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusChip(m.category, Blue10, BlueContainer)
                            Text(m.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (m.reply != null) {
                            Spacer(Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                .background(TealContainer.copy(0.5f)).padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Reply, null, tint = Teal10, modifier = Modifier.size(16.dp))
                                Text(m.reply, fontSize = 12.sp, color = Teal10, lineHeight = 17.sp)
                            }
                        }
                    }
                }
            }
        }
        item { Text(str.newComplaint, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
        if (submitted) {
            item {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(64.dp).clip(CircleShape).background(TealContainer), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Check, null, tint = Teal10, modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(str.complaintAccepted, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(6.dp))
                    Text(str.complaintReplyTime, fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(onClick = { submitted = false; selCat = ""; text = "" }, shape = RoundedCornerShape(10.dp)) {
                        Text(str.newComplaint)
                    }
                }
            }
        } else {
            item {
                AppCard {
                    Text(str.complaintType, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    cats.chunked(2).forEach { chunk ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                            chunk.forEach { cat ->
                                val isSel = selCat == cat
                                Surface(onClick = { selCat = cat }, modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSel) BlueContainer else Color.Transparent,
                                    border = BorderStroke(0.5.dp, if (isSel) Blue10 else Outline)) {
                                    Text(cat, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        fontSize = 12.sp, textAlign = TextAlign.Center,
                                        color = if (isSel) Blue10 else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isSel) FontWeight.Medium else FontWeight.Normal)
                                }
                            }
                            if (chunk.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = text, onValueChange = { text = it },
                        placeholder = { Text(str.complaintDetailHint, fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                        shape = RoundedCornerShape(10.dp), minLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Blue10)
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { if (selCat.isNotEmpty() && text.isNotEmpty()) submitted = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = selCat.isNotEmpty() && text.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue10),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Send, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(str.send, fontSize = 15.sp)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}