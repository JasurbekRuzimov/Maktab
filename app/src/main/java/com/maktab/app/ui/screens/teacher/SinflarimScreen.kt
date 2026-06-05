package com.maktab.app.ui.screens.teacher

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
import com.maktab.app.data.MockData
import com.maktab.app.ui.theme.*

@Composable
fun SinflarimScreen(language: String = "uz") {
    val title      = when(language){ "ru"->"Мои классы"; "en"->"My Classes"; else->"Sinflarim" }
    val subtitle   = when(language){ "ru"->"Список классов и учащихся"; "en"->"Classes and students"; else->"Biriktirilgan sinflar va o'quvchilar" }
    val classLbl   = when(language){ "ru"->"Класс"; "en"->"Class"; else->"Sinf" }
    val codeLbl    = when(language){ "ru"->"Код"; "en"->"Code"; else->"Sinf kodi" }
    val yearLbl    = when(language){ "ru"->"Год"; "en"->"Year"; else->"O'quv yili" }
    val studentLbl = when(language){ "ru"->"Учащиеся"; "en"->"Students"; else->"O'quvchilar" }

    var expandedClassId by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        // Stats
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCardSimple(when(language){"ru"->"Классов";"en"->"Classes";else->"Sinflar"}, "${MockData.schoolClasses.size}", Teal10, TealContainer, Modifier.weight(1f))
                StatCardSimple(when(language){"ru"->"Учащихся";"en"->"Students";else->"O'quvchilar"}, "${MockData.classStudents.size}", Blue10, BlueContainer, Modifier.weight(1f))
            }
        }
        // Table header
        item {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(0.dp)) {
                Column {
                    // Header
                    Row(Modifier.background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 14.dp, vertical = 8.dp).fillMaxWidth()) {
                        Text(classLbl, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.2f))
                        Text(codeLbl, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Text(yearLbl, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.2f))
                        Text(studentLbl, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(60.dp), textAlign = TextAlign.End)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                    // Class rows
                    MockData.schoolClasses.forEachIndexed { idx, cls ->
                        if (idx > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                        Column {
                            Row(
                                Modifier.fillMaxWidth().clickable { expandedClassId = if (expandedClassId == cls.id) null else cls.id }.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(Modifier.weight(1.2f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(TealContainer), Alignment.Center) {
                                        Text(cls.name.first().toString(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Teal10)
                                    }
                                    Text(cls.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                }
                                Text(cls.code, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                                Text(cls.year, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.2f))
                                Row(Modifier.width(60.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.clip(RoundedCornerShape(6.dp)).background(BlueContainer).padding(horizontal = 8.dp, vertical = 3.dp)) {
                                        Text("${cls.studentCount}", fontSize = 12.sp, color = Blue10, fontWeight = FontWeight.Medium)
                                    }
                                    Spacer(Modifier.width(4.dp))
                                    Icon(if (expandedClassId == cls.id) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, Modifier.size(16.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            // Expanded student list
                            if (expandedClassId == cls.id) {
                                val clsStudents = MockData.classStudents.filter { it.classId == cls.id }
                                Column(Modifier.background(MaterialTheme.colorScheme.surfaceVariant).padding(vertical = 4.dp)) {
                                    Row(Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                                        Text(when(language){"ru"->"#";"en"->"#";else->"T/r"}, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(28.dp))
                                        Text(when(language){"ru"->"Имя ученика";"en"->"Student Name";else->"O'quvchi ismi"}, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                                        Text("ID", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    clsStudents.forEachIndexed { i, s ->
                                        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(Modifier.width(28.dp).size(20.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outline.copy(0.3f)), Alignment.Center) {
                                                Text("${i+1}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Text(s.name, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                            Text(s.studentId, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        if (i < clsStudents.size - 1) HorizontalDivider(Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outline, thickness = 0.3.dp)
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
private fun StatCardSimple(label: String, value: String, color: Color, bg: Color, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(10.dp)).background(bg).padding(14.dp)) {
        Column { Text(value, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = color); Text(label, fontSize = 11.sp, color = color.copy(0.75f)) }
    }
}
