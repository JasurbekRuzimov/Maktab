package com.maktab.app.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maktab.app.data.MockData
import com.maktab.app.ui.components.*
import com.maktab.app.ui.theme.*

// ─────────────────────────────────────────────
// 1. JADVAL
// ─────────────────────────────────────────────

// StudentScheduleScreen is defined in ScheduleScreens.kt

// ─────────────────────────────────────────────
// 2. DARSLIKLAR
// ─────────────────────────────────────────────

@Composable
fun StudentDarsliklarsScreen() {
    val colors = listOf(
        Pair(Blue10, BlueContainer), Pair(Purple10, PurpleContainer),
        Pair(Teal10, TealContainer), Pair(Amber10, AmberContainer)
    )
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { SectionHeader("Darsliklar va materiallar") {} }
        itemsIndexed(MockData.subjects) { idx, subject ->
            val (color, container) = colors[idx % colors.size]
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, Outline),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(container), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MenuBook, null, tint = color, modifier = Modifier.size(22.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Text(subject.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                InfoChip("${subject.lessons} dars", color)
                                InfoChip("${subject.tests} test", color)
                                InfoChip("${subject.quizzes} quiz", color)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = Outline, thickness = 0.5.dp)
                    Spacer(Modifier.height(10.dp))
                    subject.items.forEach { item ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val (iIcon, iColor) = when (item.type) {
                                "Test"    -> Pair(Icons.Default.Assignment, Red10)
                                "Quiz"    -> Pair(Icons.Default.Quiz, Amber10)
                                "Imtixon" -> Pair(Icons.Default.FactCheck, Purple10)
                                else      -> Pair(Icons.Default.Article, Blue10)
                            }
                            Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(iColor.copy(0.1f)), contentAlignment = Alignment.Center) {
                                Icon(iIcon, null, tint = iColor, modifier = Modifier.size(14.dp))
                            }
                            Text(item.title, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            StatusChip(item.type, iColor, iColor.copy(0.1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(text: String, color: Color) {
    Box(
        Modifier.clip(RoundedCornerShape(4.dp)).background(color.copy(0.1f)).padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text, fontSize = 10.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

// ─────────────────────────────────────────────
// 3. UY VAZIFALARI
// ─────────────────────────────────────────────

@Composable
fun StudentUyVazifaScreen() {
    val submitted = remember { mutableStateListOf<Int>() }
    val colors = listOf(Pair(Blue10, BlueContainer), Pair(Purple10, PurpleContainer), Pair(Teal10, TealContainer))

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val done = MockData.childHomework.count { it.isDone } + submitted.size
                StatCard("Jami", "${MockData.childHomework.size}", Blue10, Modifier.weight(1f))
                StatCard("Bajarildi", "$done", Teal10, Modifier.weight(1f))
                StatCard("Kutilmoqda", "${MockData.childHomework.size - done}", Amber10, Modifier.weight(1f))
            }
        }
        itemsIndexed(MockData.childHomework) { idx, hw ->
            val done = hw.isDone || hw.id in submitted
            val (color, container) = colors[idx % colors.size]
            AppCard {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(container), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.MenuBook, null, tint = color, modifier = Modifier.size(22.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(hw.subject, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            StatusChip(
                                if (done) "Bajarildi" else "Kutilmoqda",
                                if (done) Teal10 else Amber10,
                                if (done) TealContainer else AmberContainer
                            )
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(hw.task, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Schedule, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Muddat: ${hw.deadline}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (!done) {
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {},
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.Upload, null, Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Fayl yuklash", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { submitted.add(hw.id) },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = color),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.Send, null, Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Topshirish", fontSize = 12.sp)
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
// 4. BAHOLAR
// ─────────────────────────────────────────────

@Composable
fun StudentBaholarScreen() {
    val colors = listOf(
        Pair(Blue10, BlueContainer), Pair(Purple10, PurpleContainer),
        Pair(Teal10, TealContainer), Pair(Amber10, AmberContainer)
    )
    val avgAll = MockData.grades.map { it.avg }.average().toInt()

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("O'rtacha", "$avgAll", Blue10, Modifier.weight(1f))
                StatCard("Fanlar", "${MockData.grades.size}", Purple10, Modifier.weight(1f))
                StatCard("Reyting", "3-o'rin", Amber10, Modifier.weight(1f))
            }
        }
        item { Text("Fan bo'yicha baholar", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
        itemsIndexed(MockData.grades) { idx, g ->
            val (color, container) = colors[idx % colors.size]
            AppCard {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(container), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.MenuBook, null, tint = color, modifier = Modifier.size(22.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(g.subject, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("${g.avg}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            g.scores.forEach { sc ->
                                Box(
                                    Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(container).padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("$sc", fontSize = 13.sp, color = color, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                            Box(Modifier.fillMaxWidth(g.avg / 100f).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(color))
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 5. DAVOMAT
// ─────────────────────────────────────────────

@Composable
fun StudentDavomatScreen() {
    val presentCount = MockData.calendarDays.count { it.status == "present" }
    val absentCount  = MockData.calendarDays.count { it.status == "absent" }
    val totalDays    = MockData.calendarDays.size
    val pct          = if (totalDays > 0) (presentCount * 100 / totalDays) else 0
    val dayH         = listOf("D", "S", "Ch", "P", "J", "Sh", "Y")

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SectionHeader("Davomat") {} }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Jami", "$totalDays", Blue10, Modifier.weight(1f))
                StatCard("Keldi", "$presentCount", Teal10, Modifier.weight(1f))
                StatCard("Kelmadi", "$absentCount", Red10, Modifier.weight(1f))
            }
        }
        item {
            AppCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Davomat foizi", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text("$pct%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (pct >= 80) Teal10 else Red10)
                }
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                    Box(Modifier.fillMaxWidth(pct / 100f).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(if (pct >= 80) Teal10 else Red10))
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
                    items(35) { i ->
                        val di = i - 4
                        val cd = if (di >= 0 && di < MockData.calendarDays.size) MockData.calendarDays[di] else null
                        if (cd != null) {
                            val (bg, tc) = when (cd.status) {
                                "present" -> Pair(TealContainer, Teal10)
                                "absent"  -> Pair(RedContainer, Red10)
                                "today"   -> Pair(AmberContainer, Amber10)
                                else      -> Pair(Color.Transparent, MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Box(Modifier.aspectRatio(1f).clip(RoundedCornerShape(6.dp)).background(bg), contentAlignment = Alignment.Center) {
                                Text("${cd.day}", fontSize = 12.sp, color = tc, fontWeight = if (cd.status == "today") FontWeight.Bold else FontWeight.Normal)
                            }
                        } else Box(Modifier.aspectRatio(1f))
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
// 6. IMTIHONLAR
// ─────────────────────────────────────────────

@Composable
fun StudentImtihonlarScreen() {
    val myExams = MockData.examTasks.filter { it.studentName == "Asilbek Karimov" }
    val colors  = listOf(Pair(Blue10, BlueContainer), Pair(Purple10, PurpleContainer), Pair(Teal10, TealContainer))

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SectionHeader("Imtihon natijalari") {} }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Jami", "${myExams.size}", Blue10, Modifier.weight(1f))
                StatCard("Baholandi", "${MockData.examFeedbacks.size}", Teal10, Modifier.weight(1f))
                StatCard("Kutilmoqda", "${myExams.size - MockData.examFeedbacks.filter { it.key <= myExams.size }.size}", Amber10, Modifier.weight(1f))
            }
        }
        itemsIndexed(myExams) { idx, exam ->
            val (color, container) = colors[idx % colors.size]
            val fb = MockData.examFeedbacks[exam.id]
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, Outline),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(container), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Assignment, null, tint = color, modifier = Modifier.size(22.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(exam.subject, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                StatusChip(
                                    if (fb != null) "Baholandi" else "Kutilmoqda",
                                    if (fb != null) Teal10 else Amber10,
                                    if (fb != null) TealContainer else AmberContainer
                                )
                            }
                            Text(exam.title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${exam.date} · ${exam.questionCount} savol", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (fb != null) {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = Outline, thickness = 0.5.dp)
                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${fb.first}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
                                Text("Ball", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${fb.second}/${exam.questionCount}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
                                Text("To'g'ri", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                            Box(Modifier.fillMaxWidth(fb.first / 100f).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(color))
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(container).padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Comment, null, tint = color, modifier = Modifier.size(16.dp))
                            Text(fb.third, fontSize = 12.sp, color = color, lineHeight = 17.sp)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 7. XABARNOMALAR
// ─────────────────────────────────────────────

@Composable
fun StudentXabarnomaScreen() {
    val xabarlar = listOf(
        Triple("Matematika", "Ertangi dars 201-xonada bo'ladi", "5 daqiqa oldin"),
        Triple("Maktab", "3-iyun ota-onalar yig'ilishi", "1 soat oldin"),
        Triple("Ingliz tili", "Uy vazifasi muddati bugun", "2 soat oldin"),
        Triple("Maktab", "Sport musobaqasi natijalari e'lon qilindi", "Bugun 10:00"),
        Triple("Fizika", "Nazorat ishi 5-iyun kuni", "Kecha 18:00"),
    )
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { SectionHeader("Xabarnomalar") {} }
        itemsIndexed(xabarlar) { idx, (from, text, time) ->
            val color = when (from) { "Matematika" -> Blue10; "Ingliz tili" -> Purple10; "Fizika" -> Teal10; else -> Amber10 }
            val container = when (from) { "Matematika" -> BlueContainer; "Ingliz tili" -> PurpleContainer; "Fizika" -> TealContainer; else -> AmberContainer }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(if (idx < 2) 1.dp else 0.5.dp, if (idx < 2) Blue10.copy(0.3f) else Outline),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(container), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Notifications, null, tint = color, modifier = Modifier.size(20.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(from, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = color)
                            if (idx < 2) Box(Modifier.size(8.dp).clip(CircleShape).background(Blue10))
                        }
                        Text(text, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(time, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 8. SO'ROVNOMALAR
// ─────────────────────────────────────────────

@Composable
fun StudentSurveylar() {
    val answers = remember { mutableStateMapOf<Int, Int>() }
    val surveys  = MockData.teacherSurveys

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SectionHeader("So'rovnomalar") {} }
        items(surveys) { s ->
            AppCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(s.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    StatusChip(
                        if (answers.containsKey(s.id)) "Javob berildi" else "Kutilmoqda",
                        if (answers.containsKey(s.id)) Teal10 else Amber10,
                        if (answers.containsKey(s.id)) TealContainer else AmberContainer
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(s.question, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                    Text("Muddat: ${s.deadline}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (!answers.containsKey(s.id)) {
                    Spacer(Modifier.height(12.dp))
                    s.options.forEachIndexed { idx, opt ->
                        val sel = answers[s.id] == idx
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (sel) BlueContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { answers[s.id] = idx }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(Modifier.size(20.dp).clip(CircleShape)
                                .background(if (sel) Blue10 else Color.Transparent)
                                .border(1.5.dp, if (sel) Blue10 else Outline, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (sel) Box(Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                            }
                            Text(opt, fontSize = 13.sp, color = if (sel) Blue10 else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (sel) FontWeight.Medium else FontWeight.Normal)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { if (answers.containsKey(s.id)) {} },
                        enabled = answers.containsKey(s.id),
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue10),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Javob yuborish", fontSize = 13.sp) }
                } else {
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(TealContainer).padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CheckCircle, null, tint = Teal10, modifier = Modifier.size(16.dp))
                        Text("Javobingiz qabul qilindi", fontSize = 12.sp, color = Teal10)
                    }
                }
            }
        }
    }
}