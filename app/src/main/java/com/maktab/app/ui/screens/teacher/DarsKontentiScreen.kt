package com.maktab.app.ui.screens.teacher

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maktab.app.data.LessonContent
import com.maktab.app.data.MockData
import com.maktab.app.ui.theme.*

@Composable
fun DarsKontentiScreen(language: String = "uz") {
    val title     = when(language){ "ru"->"Контент урока"; "en"->"Lesson Content"; else->"Dars kontenti" }
    val sinfLbl   = when(language){ "ru"->"Класс"; "en"->"Class"; else->"Sinf" }
    val fanLbl    = when(language){ "ru"->"Предмет"; "en"->"Subject"; else->"Fan" }
    val newBtn    = when(language){ "ru"->"Новый"; "en"->"New"; else->"Yangi dars" }
    val sessLbl   = when(language){ "ru"->"Сессии"; "en"->"Sessions"; else->"Sessiyalar" }
    val topicLbl  = when(language){ "ru"->"Темы"; "en"->"Topics"; else->"Oralama" }
    val doneLabel = when(language){ "ru"->"Завершено"; "en"->"Done"; else->"Yakunlangan" }
    val gradeLabel= when(language){ "ru"->"Оценки"; "en"->"Grades"; else->"Baholashlar" }

    var selectedSinf by remember { mutableStateOf("2-A") }
    var selectedFan  by remember { mutableStateOf("Alifbe") }

    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Teal10), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp), modifier = Modifier.height(34.dp)) {
                    Icon(Icons.Default.Add, null, Modifier.size(15.dp)); Spacer(Modifier.width(4.dp)); Text(newBtn, fontSize = 12.sp)
                }
            }
        }
        // Filter
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterDropdown("$sinfLbl: $selectedSinf", Modifier.weight(1f)) { selectedSinf = it }
                FilterDropdown("$fanLbl: $selectedFan", Modifier.weight(1.2f)) { selectedFan = it }
            }
        }
        // Stats
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(sessLbl to "4", topicLbl to "1", doneLabel to "3", gradeLabel to "2").forEach { (l, v) ->
                    val (color, bg) = when(l) {
                        sessLbl -> Pair(Blue10, BlueContainer)
                        topicLbl -> Pair(Amber10, AmberContainer)
                        doneLabel -> Pair(Teal10, TealContainer)
                        else -> Pair(Purple10, PurpleContainer)
                    }
                    Card(Modifier.weight(1f), colors = CardDefaults.cardColors(bg), shape = RoundedCornerShape(10.dp), elevation = CardDefaults.cardElevation(0.dp)) {
                        Column(Modifier.padding(10.dp)) {
                            Text(v, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = color)
                            Text(l, fontSize = 10.sp, color = color.copy(0.75f))
                        }
                    }
                }
            }
        }
        // Lesson list
        items(MockData.lessonContents) { lc ->
            LessonContentCard(lc, language)
        }
    }
}

@Composable
private fun LessonContentCard(lc: LessonContent, language: String) {
    val typeBg = when(lc.type){ "Test"->RedContainer; "Quiz"->PurpleContainer; else->BlueContainer }
    val typeColor = when(lc.type){ "Test"->Red10; "Quiz"->Purple10; else->Blue10 }
    val statusBg = if (lc.status == "Bog'langan") TealContainer else AmberContainer
    val statusColor = if (lc.status == "Bog'langan") Teal10 else Amber10

    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                // Number badge
                Box(Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant), Alignment.Center) {
                    Text("${lc.id}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(Modifier.weight(1f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(lc.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Box(Modifier.clip(RoundedCornerShape(4.dp)).background(typeBg).padding(horizontal = 6.dp, vertical = 2.dp)) { Text(lc.type, fontSize = 10.sp, color = typeColor) }
                        Box(Modifier.clip(RoundedCornerShape(4.dp)).background(statusBg).padding(horizontal = 6.dp, vertical = 2.dp)) { Text(lc.status, fontSize = 10.sp, color = statusColor) }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("${lc.subject} · ${lc.className}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (lc.journalDate != "-") {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.CalendarToday, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(lc.journalDate, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.AttachFile, null, Modifier.size(11.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${lc.materials} material", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.ViewModule, null, Modifier.size(11.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${lc.blocks} blok", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterDropdown(label: String, modifier: Modifier, onSelect: (String) -> Unit = {}) {
    Row(
        modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 1)
        Icon(Icons.Default.ExpandMore, null, Modifier.size(16.dp), MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
