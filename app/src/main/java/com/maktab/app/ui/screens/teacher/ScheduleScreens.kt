package com.maktab.app.ui.screens

import com.maktab.app.ui.components.StatusChip
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maktab.app.data.MockData
import com.maktab.app.network.ApiResult
import com.maktab.app.viewmodel.LessonUi
import com.maktab.app.viewmodel.ScheduleDay
import com.maktab.app.viewmodel.TeacherViewModel
import com.maktab.app.ui.theme.*

private val DAY_FULL  = listOf("Dushanba","Seshanba","Chorshanba","Payshanba","Juma")
private val DAY_SHORT = listOf("Du","Se","Ch","Pa","Ju")

fun subjectColor(subject: String): Pair<Color, Color> = when {
    subject.contains("Matematika", true)  -> Pair(Blue10,   BlueContainer)
    subject.contains("Ingliz", true)      -> Pair(Purple10,  PurpleContainer)
    subject.contains("Fizika", true)      -> Pair(Amber10,   AmberContainer)
    subject.contains("Biologiya", true)   -> Pair(Green10,   GreenContainer)
    subject.contains("O'zbek", true)      -> Pair(Teal10,    TealContainer)
    subject.contains("Tarix", true)       -> Pair(Red10,     RedContainer)
    subject.contains("Geografiya", true)  -> Pair(Amber10,   AmberContainer)
    subject.contains("Informatika", true) -> Pair(Purple10,  PurpleContainer)
    else -> Pair(Blue10, BlueContainer)
}

// ─────────────────────────────────────────────
// TEACHER SCHEDULE — API bilan ulangan
// ─────────────────────────────────────────────
@Composable
fun TeacherScheduleScreen(vm: TeacherViewModel = viewModel()) {
    val state by vm.scheduleState.collectAsState()
    val error by vm.errorMsg.collectAsState()
    var selectedDay by remember { mutableStateOf(0) }
    var weekOffset  by remember { mutableStateOf(0) }

    // Jadval yuklash
    LaunchedEffect(weekOffset) { vm.loadSchedule(weekOffset) }

    // Xato xabari
    error?.let { msg ->
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(3000)
            vm.clearError()
        }
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // Hafta navigatsiyasi
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { weekOffset--; selectedDay = 0 }) {
                Icon(Icons.Default.ChevronLeft, null, tint = Teal10)
            }
            // O'quv yili oxiri: 2026-05-31, bugun undan keyin
            val academicYearEnd = java.time.LocalDate.of(2026, 5, 31)
            val baseDate = if (java.time.LocalDate.now() <= academicYearEnd)
                java.time.LocalDate.now() else academicYearEnd
            val monday = baseDate.plusWeeks(weekOffset.toLong())
                .with(java.time.DayOfWeek.MONDAY)
            val friday = monday.plusDays(4)
            val dateRange = "${monday.dayOfMonth}-${friday.dayOfMonth} ${
                when (monday.monthValue) {
                    1 -> "Yan"; 2 -> "Fev"; 3 -> "Mar"; 4 -> "Apr"
                    5 -> "May"; 6 -> "Iyn"; 9 -> "Sen"; 10 -> "Okt"
                    11 -> "Noy"; 12 -> "Dek"; else -> "Iyn"
                }
            } ${monday.year}"
            Text(
                if (weekOffset == 0) "Joriy hafta · $dateRange"
                else if (weekOffset > 0) "+$weekOffset hafta · $dateRange"
                else "$weekOffset hafta · $dateRange",
                fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Teal10
            )
            IconButton(onClick = { weekOffset++; selectedDay = 0 }) {
                Icon(Icons.Default.ChevronRight, null, tint = Teal10)
            }
        }

        when (val s = state) {
            ApiResult.Loading -> {
                // Loading — mock data bilan ko'rsatamiz
                TeacherScheduleMock(selectedDay, onDaySelect = { selectedDay = it })
            }
            is ApiResult.Success -> {
                val days = s.data
                if (days.isEmpty()) {
                    // Bo'sh javob — mock bilan
                    TeacherScheduleMock(selectedDay, onDaySelect = { selectedDay = it })
                } else {
                    TeacherScheduleContent(
                        days = days,
                        selectedDay = selectedDay,
                        onDaySelect = { selectedDay = it }
                    )
                }
            }
            is ApiResult.Error -> {
                // Xato — mock bilan, xato xabar ko'rsat
                Column {
                    if (error != null) {
                        Row(
                            Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(RedContainer)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.WifiOff, null, tint = Red10, modifier = Modifier.size(16.dp))
                            Text(error ?: "", fontSize = 12.sp, color = Red10, modifier = Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    TeacherScheduleMock(selectedDay, onDaySelect = { selectedDay = it })
                }
            }
        }
    }
}

// Mock ma'lumotlar bilan jadval (loading/xato holatida)
@Composable
private fun TeacherScheduleMock(selectedDay: Int, onDaySelect: (Int) -> Unit) {
    val lessons = MockData.teacherSchedule.find { it.dayIndex == selectedDay }?.lessons ?: emptyList()
    val days = DAY_SHORT.mapIndexed { idx, short ->
        ScheduleDay(idx, DAY_FULL[idx], "—", lessons.map { l ->
            LessonUi(l.period.toString(), l.period, l.time, l.subject, l.className, l.teacher, l.room)
        })
    }
    TeacherScheduleContent(days = days, selectedDay = selectedDay, onDaySelect = onDaySelect)
}

@Composable
private fun TeacherScheduleContent(
    days: List<ScheduleDay>,
    selectedDay: Int,
    onDaySelect: (Int) -> Unit
) {
    val currentDay = days.getOrNull(selectedDay)
    val lessons = currentDay?.lessons ?: emptyList()

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Stat kartochkalar
        item {
            Row(
                Modifier.padding(16.dp, 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WeekStatBox(
                    "Haftalik", "${days.sumOf { it.lessons.size }} ta",
                    Teal10, TealContainer, Modifier.weight(1f)
                )
                WeekStatBox(
                    "Bugungi", "${days.getOrNull(0)?.lessons?.size ?: 0} ta",
                    Blue10, BlueContainer, Modifier.weight(1f)
                )
            }
        }
        // Kun tanlash
        item {
            Card(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.5.dp, Outline),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    days.forEachIndexed { idx, day ->
                        val isSel = selectedDay == idx
                        Surface(
                            onClick = { onDaySelect(idx) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) Teal10 else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    DAY_SHORT.getOrElse(idx) { day.dayName.take(2) },
                                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    "${day.lessons.size}d",
                                    fontSize = 10.sp,
                                    color = if (isSel) Color.White.copy(0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
        // Kun sarlavhasi
        item {
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(currentDay?.dayName ?: "", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        currentDay?.date?.takeIf { it != "—" } ?: "",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    Modifier.clip(RoundedCornerShape(8.dp))
                        .background(TealContainer)
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text("${lessons.size} dars", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Teal10)
                }
            }
        }
        // Darslar
        if (lessons.isEmpty()) {
            item { EmptyDay() }
        } else {
            itemsIndexed(lessons) { _, lesson ->
                val (color, container) = subjectColor(lesson.subject)
                LessonCard(
                    lesson = com.maktab.app.data.Lesson(
                        lesson.period, lesson.time, lesson.subject,
                        lesson.className, lesson.teacher, lesson.room
                    ),
                    isActive = false, color = color, container = container, isTeacher = true
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// STUDENT SCHEDULE — mock (API keyinroq)
// ─────────────────────────────────────────────
@Composable
fun StudentScheduleScreen() {
    val days   = listOf("Dush","Sesh","Chor","Pay","Jum")
    var sel    by remember { mutableStateOf(1) }
    val lessons = MockData.studentSchedule.find { it.dayIndex == sel }?.lessons ?: emptyList()

    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Card(
                Modifier.fillMaxWidth().padding(16.dp, 14.dp, 16.dp, 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.5.dp, Outline),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    days.forEachIndexed { idx, day ->
                        val isSel = sel == idx
                        Surface(
                            onClick = { sel = idx },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) Blue10 else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                day, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
        items(lessons) { lesson ->
            val (color, container) = subjectColor(lesson.subject)
            LessonCard(lesson, false, color, container, isTeacher = false)
        }
    }
}

// ─────────────────────────────────────────────
// SHARED COMPOSABLES
// ─────────────────────────────────────────────

@Composable
fun WeekStatBox(label: String, value: String, color: Color, container: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, Outline),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun EmptyDay() {
    Box(
        Modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.EventBusy, null, tint = Outline, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(8.dp))
            Text("Bu kun dars yo'q", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun LessonCard(
    lesson: com.maktab.app.data.Lesson,
    isActive: Boolean,
    color: Color,
    container: Color,
    isTeacher: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) container.copy(0.5f) else Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (isActive) 1.dp else 0.5.dp, if (isActive) color.copy(0.4f) else Outline),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(container),
                contentAlignment = Alignment.Center
            ) {
                Text("${lesson.period}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(lesson.subject, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    if (isActive) StatusChip("Hozir", color, container)
                }
                Spacer(Modifier.height(2.dp))
                if (isTeacher) {
                    Text(lesson.className, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(lesson.teacher, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Default.MeetingRoom, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(11.dp))
                        Text(lesson.room, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                val (start, end) = lesson.time.split("–").let {
                    it.getOrElse(0) { "" } to it.getOrElse(1) { "" }
                }
                Text(start, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = color)
                Text(end, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}