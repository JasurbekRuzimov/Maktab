package com.maktab.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import com.maktab.app.data.MockData
import com.maktab.app.ui.theme.*

@Composable
fun BaholashScreen(language: String = "uz") {
    val title    = when(language){ "ru"->"Оценивание"; "en"->"Assessment"; else->"Baholash markazi" }
    val groupLbl = when(language){ "ru"->"Группы"; "en"->"Groups"; else->"Guruhlar" }
    val subLbl   = when(language){ "ru"->"Сдано"; "en"->"Submitted"; else->"Topshirgan" }
    val notSubLbl= when(language){ "ru"->"Не сдано"; "en"->"Not Submitted"; else->"Topshirmagan" }
    val gradedLbl= when(language){ "ru"->"Оценено"; "en"->"Graded"; else->"Baholangan" }

    val expandedIds = remember { mutableStateListOf<Int>() }
    var selectedSinf by remember { mutableStateOf("2-A") }

    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = {}) { Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        // Filter + Stats
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(Modifier.weight(1f), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(10.dp), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline), elevation = CardDefaults.cardElevation(0.dp)) {
                    Row(Modifier.padding(10.dp, 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(when(language){"ru"->"Класс";"en"->"Class";else->"Sinf"}, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(selectedSinf, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Icon(Icons.Default.ExpandMore, null, Modifier.size(16.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                StatBox(groupLbl, "${MockData.homeworkGroups.size}", Teal10, TealContainer, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val totalSub = MockData.homeworkGroups.sumOf { it.submitted }
                val totalNot = MockData.homeworkGroups.sumOf { it.totalStudents - it.submitted }
                StatBox(subLbl, "$totalSub", Green10, GreenContainer, Modifier.weight(1f))
                StatBox(notSubLbl, "$totalNot", Red10, RedContainer, Modifier.weight(1f))
                StatBox(gradedLbl, "${MockData.homeworkGroups.sumOf { it.graded }}", Blue10, BlueContainer, Modifier.weight(1f))
            }
        }
        // Homework groups
        items(MockData.homeworkGroups) { group ->
            val isExpanded = group.id in expandedIds
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column {
                    // Header
                    Column(
                        Modifier.fillMaxWidth().clickable { if (isExpanded) expandedIds.remove(group.id) else expandedIds.add(group.id) }.padding(14.dp)
                    ) {
                        // Tags
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(Modifier.clip(RoundedCornerShape(4.dp)).background(BlueContainer).padding(horizontal = 6.dp, vertical = 2.dp)) { Text(group.type, fontSize = 10.sp, color = Blue10) }
                            Box(Modifier.clip(RoundedCornerShape(4.dp)).background(TealContainer).padding(horizontal = 6.dp, vertical = 2.dp)) { Text(selectedSinf, fontSize = 10.sp, color = Teal10) }
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                            Text(group.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                            Icon(if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, Modifier.size(18.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("${group.subject} · ${group.date}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MiniCountChip("${group.totalStudents} o'quvchi", MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
                            MiniCountChip("${group.submitted} topshirgan", Green10, GreenContainer)
                            MiniCountChip("${group.totalStudents - group.submitted} topshirmagan", Red10, RedContainer)
                            if (group.graded > 0) MiniCountChip("${group.graded} baholangan", Blue10, BlueContainer)
                        }
                    }
                    // Student list (expandable)
                    AnimatedVisibility(isExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                        Column(Modifier.background(MaterialTheme.colorScheme.surfaceVariant).padding(vertical = 4.dp)) {
                            group.submissions.forEachIndexed { idx, sub ->
                                if (idx > 0) HorizontalDivider(Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outline, thickness = 0.3.dp)
                                Row(
                                    Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface), Alignment.Center) {
                                        Text("${idx+1}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(sub.studentName, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                    when (sub.status) {
                                        "submitted" -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Box(Modifier.clip(RoundedCornerShape(4.dp)).background(GreenContainer).padding(horizontal = 8.dp, vertical = 3.dp)) {
                                                Text(when(language){"ru"->"Сдал";"en"->"Submitted";else->"Topshirdi"}, fontSize = 11.sp, color = Green10, fontWeight = FontWeight.Medium)
                                            }
                                        }
                                        "graded" -> Box(Modifier.clip(RoundedCornerShape(4.dp)).background(BlueContainer).padding(horizontal = 8.dp, vertical = 3.dp)) {
                                            Text(when(language){"ru"->"Оценено";"en"->"Graded";else->"Baholangan"}, fontSize = 11.sp, color = Blue10)
                                        }
                                        else -> Box(Modifier.clip(RoundedCornerShape(4.dp)).background(RedContainer).padding(horizontal = 8.dp, vertical = 3.dp)) {
                                            Text(when(language){"ru"->"Не сдал";"en"->"Not submitted";else->"Topshirmagan"}, fontSize = 11.sp, color = Red10)
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

@Composable
private fun StatBox(label: String, value: String, color: Color, bg: Color, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(10.dp)).background(bg).padding(10.dp)) {
        Column { Text(value, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = color); Text(label, fontSize = 10.sp, color = color.copy(0.75f)) }
    }
}

@Composable
private fun MiniCountChip(text: String, color: Color, bg: Color) {
    Box(Modifier.clip(RoundedCornerShape(4.dp)).background(bg).padding(horizontal = 6.dp, vertical = 2.dp)) {
        Text(text, fontSize = 10.sp, color = color)
    }
}
