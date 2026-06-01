package com.maktab.app.ui.screens

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maktab.app.data.JournalEntry
import com.maktab.app.data.MockData
import com.maktab.app.data.Student
import com.maktab.app.ui.theme.*

@Composable
fun JurnalScreen(language: String = "uz") {
    val title     = when(language){ "ru"->"Журнал"; "en"->"Journal"; else->"Jurnal" }
    val chorakLbl = when(language){ "ru"->"Четверть"; "en"->"Quarter"; else->"Chorak" }
    val sinfLbl   = when(language){ "ru"->"Класс"; "en"->"Class"; else->"Sinf" }
    val fanLbl    = when(language){ "ru"->"Предмет"; "en"->"Subject"; else->"Fan" }
    val totalLbl  = when(language){ "ru"->"Всего"; "en"->"Total"; else->"Jami" }
    val avgLbl    = when(language){ "ru"->"Среднее"; "en"->"Average"; else->"O'rtacha" }

    var selectedChorak by remember { mutableStateOf("4-chorak") }
    var selectedSinf   by remember { mutableStateOf("5-A") }
    var selectedFan    by remember { mutableStateOf("Matematika") }
    var selectedStudent by remember { mutableStateOf<Student?>(null) }

    val sessions = MockData.journalSessions
    val students = MockData.students
    val hScroll  = rememberScrollState()

    // Grade color
    @Composable
    fun gradeColor(g: Int?) = when {
        g == null -> MaterialTheme.colorScheme.onSurfaceVariant.copy(0f)
        g >= 5 -> Teal10; g >= 4 -> Blue10; g >= 3 -> Amber10; else -> Red10
    }
    @Composable fun gradeContainer(g: Int?) = when {
        g == null -> MaterialTheme.colorScheme.surfaceVariant.copy(0f)
        g >= 5 -> TealContainer; g >= 4 -> BlueContainer; g >= 3 -> AmberContainer; else -> RedContainer
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // ── Filter row ─────────────────────────────────────────────────
        Card(Modifier.fillMaxWidth().padding(12.dp,12.dp,12.dp,4.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(0.dp)) {
            Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChipJournal(chorakLbl, selectedChorak, Modifier.weight(1f))
                FilterChipJournal(sinfLbl, selectedSinf, Modifier.weight(1f))
                FilterChipJournal(fanLbl, selectedFan, Modifier.weight(1f))
            }
        }

        // ── Stats row ──────────────────────────────────────────────────
        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniStat("$totalLbl: ${students.size}", Teal10, TealContainer, Modifier.weight(1f))
            MiniStat("$avgLbl: 3.8", Blue10, BlueContainer, Modifier.weight(1f))
            MiniStat(when(language){"ru"->"Явка: 88%";"en"->"Attend: 88%";else->"Davomat: 88%"}, Amber10, AmberContainer, Modifier.weight(1f))
        }

        // ── Table header ───────────────────────────────────────────────
        Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(10.dp), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(0.dp)) {

            // Header row
            Row(Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
                // Fixed column header
                Box(Modifier.width(140.dp).padding(8.dp)) {
                    Text(when(language){"ru"->"Учащийся";"en"->"Student";else->"O'quvchi"},
                        fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // Scrollable session headers
                Row(Modifier.horizontalScroll(hScroll)) {
                    sessions.forEach { session ->
                        Column(
                            Modifier.width(72.dp).padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val typeColor = when(session.type){"Test"->Red10;"Quiz"->Purple10;else->Blue10}
                            val typeBg = when(session.type){"Test"->RedContainer;"Quiz"->PurpleContainer;else->BlueContainer}
                            Text(session.shortDate, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.height(2.dp))
                            Box(Modifier.clip(RoundedCornerShape(3.dp)).background(typeBg).padding(horizontal = 4.dp, vertical = 1.dp)) {
                                Text(session.type, fontSize = 9.sp, color = typeColor, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    // Natija column
                    Box(Modifier.width(52.dp).padding(8.dp), Alignment.Center) {
                        Text(when(language){"ru"->"Итог";"en"->"Total";else->"Natija"}, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

            // Student rows
            LazyColumn(modifier = Modifier.heightIn(max = 460.dp)) {
                itemsIndexed(students) { idx, student ->
                    if (idx > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { selectedStudent = student }
                            .background(
                                if (selectedStudent?.id == student.id)
                                    MaterialTheme.colorScheme.primary.copy(0.07f)
                                else Color.Transparent
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Fixed student name
                        Row(Modifier.width(140.dp).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(22.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), Alignment.Center) {
                                Text("${idx+1}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(student.name.split(" ").first(), fontSize = 12.sp, maxLines = 1)
                        }
                        // Scrollable grade cells
                        Row(Modifier.horizontalScroll(hScroll)) {
                            sessions.forEach { session ->
                                val entry = MockData.getEntry(student.id, session.id)
                                GradeCellTable(entry, student.id == 6)
                            }
                            // Quarter average
                            val avg = sessions.mapNotNull { s -> MockData.getEntry(student.id, s.id)?.grade }.let { if (it.isEmpty()) null else it.average().toInt() }
                            Box(Modifier.width(52.dp).padding(4.dp), Alignment.Center) {
                                if (avg != null) {
                                    Box(Modifier.size(28.dp).clip(CircleShape).background(when{avg>=5->TealContainer;avg>=4->BlueContainer;avg>=3->AmberContainer;else->RedContainer}), Alignment.Center) {
                                        Text("$avg", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = when{avg>=5->Teal10;avg>=4->Blue10;avg>=3->Amber10;else->Red10})
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Selected student detail ────────────────────────────────────
        selectedStudent?.let { stu ->
            Spacer(Modifier.height(10.dp))
            StudentJournalDetail(stu, language) { selectedStudent = null }
        }
    }
}

@Composable
private fun GradeCellTable(entry: JournalEntry?, dimmed: Boolean) {
    Box(Modifier.width(72.dp).height(52.dp).padding(3.dp), Alignment.Center) {
        if (entry != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                // Grade
                if (entry.grade != null) {
                    Box(Modifier.size(24.dp).clip(CircleShape).background(when{entry.grade>=5->TealContainer;entry.grade>=4->BlueContainer;entry.grade>=3->AmberContainer;else->RedContainer}), Alignment.Center) {
                        Text("${entry.grade}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = when{entry.grade>=5->Teal10;entry.grade>=4->Blue10;entry.grade>=3->Amber10;else->Red10})
                    }
                } else {
                    Box(Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), Alignment.Center) {
                        Text("–", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                // Attendance + homework mini icons
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    // Attendance dot
                    Box(Modifier.size(7.dp).clip(CircleShape).background(if (entry.attendance == "present") Teal10 else Red10))
                    // Homework dot
                    Box(Modifier.size(7.dp).clip(CircleShape).background(if (entry.homework == "done") Blue10 else MaterialTheme.colorScheme.outline))
                }
            }
        } else {
            Text("–", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StudentJournalDetail(student: Student, language: String, onClose: () -> Unit) {
    val entries = MockData.journalSessions.map { s -> s to MockData.getEntry(student.id, s.id) }
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Teal10.copy(0.4f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(36.dp).clip(CircleShape).background(TealContainer), Alignment.Center) {
                        Text(student.initials, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Teal10)
                    }
                    Column {
                        Text(student.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(when(language){"ru"->"Подробно";"en"->"Details";else->"Batafsil"}, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            entries.forEach { (session, entry) ->
                if (entry != null) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(session.shortDate, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(40.dp))
                        val typeColor = when(session.type){"Test"->Red10;"Quiz"->Purple10;else->Blue10}
                        val typeBg = when(session.type){"Test"->RedContainer;"Quiz"->PurpleContainer;else->BlueContainer}
                        Box(Modifier.clip(RoundedCornerShape(3.dp)).background(typeBg).padding(horizontal = 5.dp, vertical = 1.dp)) {
                            Text(session.type, fontSize = 10.sp, color = typeColor)
                        }
                        // Grade
                        if (entry.grade != null) {
                            Box(Modifier.size(26.dp).clip(CircleShape).background(when{entry.grade>=5->TealContainer;entry.grade>=4->BlueContainer;else->AmberContainer}), Alignment.Center) {
                                Text("${entry.grade}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = when{entry.grade>=5->Teal10;entry.grade>=4->Blue10;else->Amber10})
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        // Attendance
                        val attColor = if (entry.attendance == "present") Teal10 else Red10
                        val attBg = if (entry.attendance == "present") TealContainer else RedContainer
                        val attText = when {
                            entry.attendance == "present" -> when(language){"ru"->"Пришёл";"en"->"Present";else->"Keldi"}
                            else -> when(language){"ru"->"Отсутствовал";"en"->"Absent";else->"Kelmadi"}
                        }
                        Box(Modifier.clip(RoundedCornerShape(4.dp)).background(attBg).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text(attText, fontSize = 10.sp, color = attColor)
                        }
                        // Homework
                        if (entry.homework != "-") {
                            val hwColor = if (entry.homework == "done") Blue10 else Amber10
                            val hwBg = if (entry.homework == "done") BlueContainer else AmberContainer
                            val hwText = if (entry.homework == "done") "✓" else "✗"
                            Box(Modifier.size(20.dp).clip(CircleShape).background(hwBg), Alignment.Center) {
                                Text(hwText, fontSize = 10.sp, color = hwColor)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipJournal(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        }
        Icon(Icons.Default.ExpandMore, null, Modifier.size(14.dp), MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MiniStat(text: String, color: Color, bg: Color, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(8.dp)).background(bg).padding(horizontal = 10.dp, vertical = 6.dp)) {
        Text(text, fontSize = 11.sp, color = color, fontWeight = FontWeight.Medium)
    }
}
