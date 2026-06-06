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
import com.maktab.app.network.ApiResult
import com.maktab.app.ui.components.*
import com.maktab.app.ui.theme.*
import com.maktab.app.viewmodel.ParentViewModel
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────
// MOCK DATA
// ─────────────────────────────────────────────

private data class NewsItem(val id: Int, val title: String, val body: String, val date: String, val category: String, val isRead: Boolean)
private data class BlogPost(val id: Int, val title: String, val author: String, val date: String, val preview: String, val likes: Int)
private data class SurveyItem(val id: Int, val title: String, val question: String, val options: List<String>, val deadline: String, val answered: Boolean)
private data class MurojaatItem(val id: Int, val title: String, val category: String, val date: String, val status: String, val reply: String?)
private data class PaymentItem(val id: Int, val title: String, val amount: String, val date: String, val status: String, val type: String)

private object ParentMock {
    val news = listOf(
        NewsItem(1, "Yozgi ta'til jadvali e'lon qilindi", "2026-2027 o'quv yili uchun yozgi ta'til 15-iyundan boshlanadi.", "02.06.2026", "E'lon", false),
        NewsItem(2, "Olimpiada natijalari", "Matematika olimpiadasida maktabimiz o'quvchilari 3 ta medal qo'lga kiritdi.", "01.06.2026", "Yangilik", true),
        NewsItem(3, "Ota-onalar yig'ilishi", "5-A sinf ota-onalar yig'ilishi 5-iyun kuni soat 17:00 da bo'lib o'tadi.", "30.05.2026", "E'lon", true),
    )
    val blogs = listOf(
        BlogPost(1, "Farzandingizga o'qishni sevdirish yo'llari", "Psixolog Aziza Rahimova", "30.05.2026", "Har bir bola o'z tezligida o'rganadi...", 24),
        BlogPost(2, "Raqamli texnologiyalar va ta'lim", "O'qituvchi Bobur Toshev", "25.05.2026", "Zamonaviy ta'limda texnologiyalardan foydalanish...", 18),
    )
    val surveys = listOf(
        SurveyItem(1, "Maktab xizmatlari sifati", "Maktab xizmatlaridan qanchalik mamnunsiz?", listOf("Juda mamnun", "Mamnun", "O'rtacha", "Mamnun emas"), "10.06.2026", false),
        SurveyItem(2, "Darslik sifatini baholang", "Joriy darsliklar bolangizga qanchalik qulay?", listOf("A'lo", "Yaxshi", "Qoniqarli", "Yomon"), "08.06.2026", true),
    )
    val murojaatlar = listOf(
        MurojaatItem(1, "Dars jadvalidagi xatolik", "O'quv jarayoni", "25.05.2026", "Javob berildi", "Jadval tuzatildi."),
        MurojaatItem(2, "Ovqatlanish sifati haqida", "Infratuzilma", "20.05.2026", "Ko'rib chiqilmoqda", null),
    )
}

// ─────────────────────────────────────────────
// 1. DASHBOARD
// ─────────────────────────────────────────────

@Composable
fun ParentDashboardScreen(vm: ParentViewModel = viewModel()) {
    val childState by vm.childState.collectAsState()
    LaunchedEffect(Unit) { vm.loadDashboard() }
    val child = (childState as? ApiResult.Success)?.data

    val attTotal   = child?.attendance?.total   ?: 0
    val attPresent = child?.attendance?.present ?: 0
    val attendancePct  = if (attTotal > 0) (attPresent * 100 / attTotal) else 0
    val avgGrade       = child?.grades?.avgPercent?.toInt() ?: 0
    val homeworkDone   = child?.homework?.submitted ?: 0
    val homeworkTotal  = child?.homework?.total     ?: 0
    val isLoading      = childState is ApiResult.Loading

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlueContainer),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(0.5.dp, Blue10.copy(0.3f)),
                elevation = CardDefaults.cardElevation(0.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("BOG'LANGAN FARZANDLAR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Blue10, letterSpacing = 0.8.sp)
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AvatarCircle(
                            child?.fullname?.split(" ")?.take(2)?.joinToString("") { it.firstOrNull()?.uppercase() ?: "" } ?: "?",
                            Blue10, 52.dp)
                        Column(Modifier.weight(1f)) {
                            Text(child?.fullname ?: "Farzand", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Blue10)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Badge, null, tint = Blue10.copy(0.6f), modifier = Modifier.size(12.dp))
                                Text(child?.className ?: "Sinf biriktirilmagan", fontSize = 11.sp, color = Blue10.copy(0.7f))
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                StatusChip("Faol", Teal10, TealContainer)
                                StatusChip("${child?.age ?: 0} yosh", Blue10, BlueContainer.copy(0.5f))
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ParentQuickStat("Davomat",      if (isLoading) "..." else "$attendancePct%",          Icons.Default.CalendarToday,  Blue10,  Modifier.weight(1f))
                        ParentQuickStat("Baholar",      if (isLoading) "..." else "$avgGrade%",               Icons.Default.Star,            Amber10, Modifier.weight(1f))
                        ParentQuickStat("Uy vazifalari",if (isLoading) "..." else "$homeworkDone/$homeworkTotal", Icons.Default.MenuBook,   Teal10,  Modifier.weight(1f))
                        ParentQuickStat("Tangalar",     "0",                                                  Icons.Default.MonetizationOn,  Red10,   Modifier.weight(1f))
                    }
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                SectionHeader("Oxirgi yangiliklar") {}
                Spacer(Modifier.height(8.dp))
            }
        }
        item {
            Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant).padding(12.dp)) {
                Text("Yangiliklar tez orada", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        child?.payments?.let { pay ->
            if (pay.contractsCount > 0 || pay.nextPaymentDate != null) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Column(Modifier.padding(horizontal = 16.dp)) { SectionHeader("To'lovlar") {}; Spacer(Modifier.height(8.dp)) }
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp), border = BorderStroke(0.5.dp, Outline), elevation = CardDefaults.cardElevation(0.dp)) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(40.dp).clip(RoundedCornerShape(9.dp)).background(if (pay.nextPaymentDate != null) AmberContainer else TealContainer), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Payment, null, tint = if (pay.nextPaymentDate != null) Amber10 else Teal10, modifier = Modifier.size(20.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(pay.activeContract ?: "Shartnoma", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                pay.nextPaymentDate?.let { Text("Keyingi to'lov: $it", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            }
                            Text("${"%.0f".format(pay.totalAmount)} so'm", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Teal10)
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
    Column(modifier = modifier.clip(RoundedCornerShape(10.dp)).background(Color.White.copy(0.7f)).padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
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

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp)) {
        item {
            Column(Modifier.padding(16.dp)) {
                SectionHeader("Profil") {}
                Spacer(Modifier.height(12.dp))
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp), border = BorderStroke(0.5.dp, Outline), elevation = CardDefaults.cardElevation(0.dp)) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        val initials = profile?.fullname?.trim()?.split(" ")?.take(2)?.joinToString("") { it.firstOrNull()?.uppercase() ?: "" } ?: "?"
                        Box(Modifier.size(64.dp).clip(CircleShape).background(BlueContainer), contentAlignment = Alignment.Center) {
                            Text(initials, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Blue10)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(profile?.fullname ?: "Ota-ona", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(2.dp))
                            Text("Ota-ona", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(6.dp))
                            Box(Modifier.clip(RoundedCornerShape(6.dp)).background(BlueContainer).padding(horizontal = 10.dp, vertical = 3.dp)) {
                                Text("Maktab ota-onasi", fontSize = 11.sp, color = Blue10, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text("XAVFSIZLIK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.8.sp)
                Spacer(Modifier.height(8.dp))
                Text("Parolni almashtirish", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("Kabinet parolini vaqt-vaqti bilan yangilab turing.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                ParolField("Joriy parol", joriyParol, { joriyParol = it }, joriyVisible) { joriyVisible = !joriyVisible }
                Spacer(Modifier.height(12.dp))
                ParolField("Yangi parol", yangiParol, { yangiParol = it }, yangiVisible) { yangiVisible = !yangiVisible }
                Spacer(Modifier.height(12.dp))
                ParolField("Parolni tasdiqlang", tasdiqlash, { tasdiqlash = it }, tasdiqlashVisible) { tasdiqlashVisible = !tasdiqlashVisible }
                Spacer(Modifier.height(16.dp))
                if (saved) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(TealContainer).padding(12.dp)) {
                        Icon(Icons.Default.CheckCircle, null, tint = Teal10, modifier = Modifier.size(18.dp))
                        Text("Parol muvaffaqiyatli o'zgartirildi!", fontSize = 13.sp, color = Teal10)
                    }
                    Spacer(Modifier.height(12.dp))
                }
                Button(onClick = { if (yangiParol.isNotEmpty()) scope.launch { saved = true; kotlinx.coroutines.delay(3000); saved = false } },
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue10), shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Default.Lock, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Parolni saqlash", fontSize = 14.sp)
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
        OutlinedTextField(value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(), singleLine = true,
            shape = RoundedCornerShape(10.dp),
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onToggle) {
                    Icon(if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Blue10, unfocusedBorderColor = Outline))
    }
}

// ─────────────────────────────────────────────
// 3. DAVOMAT
// ─────────────────────────────────────────────

@Composable
fun ParentDavomatScreen(vm: ParentViewModel = viewModel()) {
    val childState by vm.childState.collectAsState()
    LaunchedEffect(Unit) { vm.loadDashboard() }
    val child        = (childState as? ApiResult.Success)?.data
    val presentCount = child?.attendance?.present ?: 0
    val absentCount  = child?.attendance?.absent  ?: 0
    val totalDays    = child?.attendance?.total   ?: 0
    val attendancePct = if (totalDays > 0) (presentCount * 100 / totalDays) else 0
    val dayH = listOf("D", "S", "Ch", "P", "J", "Sh", "Y")

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SectionHeader("${child?.fullname ?: "Farzand"} — Davomat") {} }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Jami kun", "$totalDays",    Blue10, Modifier.weight(1f))
                StatCard("Keldi",    "$presentCount", Teal10, Modifier.weight(1f))
                StatCard("Kelmadi", "$absentCount",  Red10,  Modifier.weight(1f))
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp), border = BorderStroke(0.5.dp, Outline), elevation = CardDefaults.cardElevation(0.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Davomat foizi", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("$attendancePct%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (attendancePct >= 80) Teal10 else Red10)
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        Box(Modifier.fillMaxWidth(attendancePct / 100f).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(if (attendancePct >= 80) Teal10 else Red10))
                    }
                }
            }
        }
        item {
            AppCard {
                Text("Davomat taqvimi", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth()) {
                    dayH.forEach { d -> Text(d, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                Spacer(Modifier.height(4.dp))
                LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp), userScrollEnabled = false) {
                    items(35) { Box(Modifier.aspectRatio(1f)) } // TODO: real calendar
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
        if ((child?.grades?.completedCount ?: 0) == 0) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                        Text("Baholar hali kiritilmagan", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            child?.grades?.recent?.let { recent ->
                itemsIndexed(recent) { idx, g ->
                    val (color, container) = gc[idx % gc.size]
                    AppCard {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(container), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.MenuBook, null, tint = color, modifier = Modifier.size(20.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(g.subject, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                                    Text("${"%.0f".format(g.grade ?: 0.0)}", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = color)
                                }
                                Text(g.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
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
    val hw   = child?.homework
    val hwC  = listOf(Pair(Blue10, BlueContainer), Pair(Purple10, PurpleContainer), Pair(Amber10, AmberContainer))

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SectionHeader("Uyga vazifalar") {} }
        if (childState is ApiResult.Loading) {
            item { Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), Alignment.Center) { CircularProgressIndicator(color = Blue10, strokeWidth = 2.dp) } }
        } else if ((hw?.total ?: 0) == 0) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.MenuBook, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                        Text("Uyga vazifalar hali berilmagan", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Jami", "${hw?.total ?: 0}", Blue10, Modifier.weight(1f))
                    StatCard("Topshirildi", "${hw?.submitted ?: 0}", Teal10, Modifier.weight(1f))
                    StatCard("Kutilmoqda", "${hw?.pending ?: 0}", Amber10, Modifier.weight(1f))
                }
            }
            if (!hw?.recent.isNullOrEmpty()) {
                item { Text("So'nggi vazifalar", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
                itemsIndexed(hw!!.recent) { idx, h ->
                    val (color, container) = hwC[idx % hwC.size]
                    val statusColor = when (h.status.lowercase()) { "submitted" -> Teal10; "late" -> Red10; else -> Amber10 }
                    val statusBg    = when (h.status.lowercase()) { "submitted" -> TealContainer; "late" -> RedContainer; else -> AmberContainer }
                    val statusLabel = when (h.status.lowercase()) { "submitted" -> "Topshirildi"; "late" -> "Kech"; "pending" -> "Kutilmoqda"; else -> h.status }
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
                                if (h.title.isNotEmpty()) { Spacer(Modifier.height(3.dp)); Text(h.title, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                if (h.dueDate.isNotEmpty()) {
                                    Spacer(Modifier.height(3.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.Schedule, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Muddat: ${h.dueDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                h.grade?.let { g -> Spacer(Modifier.height(4.dp)); Text("Baho: ${"%.0f".format(g)}", fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium) }
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
            item { Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), Alignment.Center) { CircularProgressIndicator(color = Blue10, strokeWidth = 2.dp) } }
        } else {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Jami",       "${exams?.attemptsCount ?: 0}",  Blue10,  Modifier.weight(1f))
                    StatCard("Baholandi",  "${exams?.gradedCount ?: 0}",    Teal10,  Modifier.weight(1f))
                    StatCard("Kutilmoqda", "${(exams?.attemptsCount ?: 0) - (exams?.gradedCount ?: 0)}", Amber10, Modifier.weight(1f))
                }
            }
            if ((exams?.attemptsCount ?: 0) == 0) {
                item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Assignment, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                            Text("Imtihon natijalari hali yo'q", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else if (!exams?.recent.isNullOrEmpty()) {
                item { Text("So'nggi imtihonlar", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
                itemsIndexed(exams!!.recent) { idx, exam ->
                    val (color, container) = gc[idx % gc.size]
                    val isGraded = exam.status.lowercase() in listOf("graded", "baholandi", "completed")
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp), border = BorderStroke(0.5.dp, Outline), elevation = CardDefaults.cardElevation(0.dp)) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(container), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Assignment, null, tint = color, modifier = Modifier.size(22.dp))
                                }
                                Column(Modifier.weight(1f)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(exam.subject.ifEmpty { exam.name }, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                        StatusChip(if (isGraded) "Baholandi" else "Kutilmoqda", if (isGraded) Teal10 else Amber10, if (isGraded) TealContainer else AmberContainer)
                                    }
                                    if (exam.date.isNotEmpty()) Text(exam.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (isGraded && exam.score != null) {
                                Spacer(Modifier.height(10.dp))
                                HorizontalDivider(color = Outline, thickness = 0.5.dp)
                                Spacer(Modifier.height(10.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("${"%.0f".format(exam.score)}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
                                        Text("Ball", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    exam.maxScore?.let { max ->
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("${"%.0f".format(max)}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("Maksimal", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        val pct = (exam.score / max).toFloat().coerceIn(0f, 1f)
                                        Box(Modifier.size(52.dp).clip(CircleShape).background(container), contentAlignment = Alignment.Center) {
                                            Text("${"%.0f".format(pct * 100)}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
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
}

// ─────────────────────────────────────────────
// 7. TO'LOVLAR
// ─────────────────────────────────────────────

@Composable
fun ParentTolovlarScreen(vm: ParentViewModel = viewModel()) {
    val childState by vm.childState.collectAsState()
    LaunchedEffect(Unit) { vm.loadDashboard() }
    val child = (childState as? ApiResult.Success)?.data
    val pay   = child?.payments

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SectionHeader("To'lovlar") {} }
        if (childState is ApiResult.Loading) {
            item { Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), Alignment.Center) { CircularProgressIndicator(color = Teal10, strokeWidth = 2.dp) } }
        } else {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Shartnomalar", "${pay?.contractsCount ?: 0}", Blue10, Modifier.weight(1f))
                    StatCard("Jami summa", if ((pay?.totalAmount ?: 0.0) > 0) "${"%.0f".format(pay?.totalAmount)} so'm" else "0", Teal10, Modifier.weight(1f))
                }
            }
            pay?.activeContract?.let { contract ->
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp), border = BorderStroke(0.5.dp, Teal10.copy(0.4f)), elevation = CardDefaults.cardElevation(0.dp)) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(TealContainer), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Description, null, tint = Teal10, modifier = Modifier.size(22.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Text("Faol shartnoma", fontSize = 12.sp, color = Teal10, fontWeight = FontWeight.Medium)
                                Text(contract, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                            StatusChip("Faol", Teal10, TealContainer)
                        }
                    }
                }
            }
            pay?.nextPaymentDate?.let { date ->
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp), border = BorderStroke(0.5.dp, Amber10.copy(0.4f)), elevation = CardDefaults.cardElevation(0.dp)) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(AmberContainer), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CalendarToday, null, tint = Amber10, modifier = Modifier.size(22.dp))
                            }
                            Column { Text("Keyingi to'lov sanasi", fontSize = 12.sp, color = Amber10, fontWeight = FontWeight.Medium); Text(date, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
                        }
                    }
                }
            }
            if ((pay?.contractsCount ?: 0) == 0) {
                item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Payment, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                            Text("Shartnoma va to'lovlar hali yo'q", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        items(ParentMock.news) { n ->
            val catColor = when (n.category) { "E'lon" -> Pair(Blue10, BlueContainer); else -> Pair(Teal10, TealContainer) }
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White),
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
                            Text("Batafsil o'qish", fontSize = 12.sp, color = Blue10)
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
                        val answered = s.answered || answers.containsKey(s.id)
                        StatusChip(if (answered) "Javob berildi" else "Kutilmoqda", if (answered) Teal10 else Amber10, if (answered) TealContainer else AmberContainer)
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
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(8.dp))
                                .background(if (sel) BlueContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { answers[s.id] = idx }.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(Modifier.size(20.dp).clip(CircleShape).background(if (sel) Blue10 else Color.Transparent).border(1.5.dp, if (sel) Blue10 else Outline, CircleShape), contentAlignment = Alignment.Center) {
                                    if (sel) Box(Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                                }
                                Text(opt, fontSize = 13.sp, color = if (sel) Blue10 else MaterialTheme.colorScheme.onSurface, fontWeight = if (sel) FontWeight.Medium else FontWeight.Normal)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = {}, enabled = answers.containsKey(s.id), modifier = Modifier.fillMaxWidth().height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue10), shape = RoundedCornerShape(10.dp)) {
                            Text("Javob yuborish", fontSize = 13.sp)
                        }
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
            item { Text("Mening murojaatlarim", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
            items(ParentMock.murojaatlar) { m ->
                val (sColor, sContainer) = when (m.status) { "Javob berildi" -> Pair(Teal10, TealContainer); "Yopildi" -> Pair(MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surfaceVariant); else -> Pair(Amber10, AmberContainer) }
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
                            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(TealContainer.copy(0.5f)).padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Reply, null, tint = Teal10, modifier = Modifier.size(16.dp))
                                Text(m.reply, fontSize = 12.sp, color = Teal10, lineHeight = 17.sp)
                            }
                        }
                    }
                }
            }
        }
        item { Text("Yangi murojaat", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
        if (submitted) {
            item {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(64.dp).clip(CircleShape).background(TealContainer), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Check, null, tint = Teal10, modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Murojaatingiz qabul qilindi", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(6.dp))
                    Text("Maktab ma'muriyati 2 ish kuni ichida ko'rib chiqadi", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(onClick = { submitted = false; selCat = ""; text = "" }, shape = RoundedCornerShape(10.dp)) { Text("Yangi murojaat") }
                }
            }
        } else {
            item {
                AppCard {
                    Text("Murojaat turi", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    cats.chunked(2).forEach { chunk ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                            chunk.forEach { cat ->
                                val isSel = selCat == cat
                                Surface(onClick = { selCat = cat }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp),
                                    color = if (isSel) BlueContainer else Color.Transparent, border = BorderStroke(0.5.dp, if (isSel) Blue10 else Outline)) {
                                    Text(cat, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), fontSize = 12.sp, textAlign = TextAlign.Center,
                                        color = if (isSel) Blue10 else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (isSel) FontWeight.Medium else FontWeight.Normal)
                                }
                            }
                            if (chunk.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = text, onValueChange = { text = it }, placeholder = { Text("Muammoni batafsil yozing...", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp), shape = RoundedCornerShape(10.dp), minLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Blue10))
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { if (selCat.isNotEmpty() && text.isNotEmpty()) submitted = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp), enabled = selCat.isNotEmpty() && text.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue10), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.Send, null, Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text("Yuborish", fontSize = 15.sp)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ─────────────────────────────────────────────
// 12. XULQ
// ─────────────────────────────────────────────

@Composable
fun XulqScreen() {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SectionHeader("Xulq va xatti-harakat") {} }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Ijobiy", "0", Teal10, Modifier.weight(1f))
                StatCard("Neytral", "0", Amber10, Modifier.weight(1f))
                StatCard("Salbiy", "0", Red10, Modifier.weight(1f))
            }
        }
        item {
            Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), Alignment.Center) {
                Text("Xulq yozuvlari hali yo'q", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}