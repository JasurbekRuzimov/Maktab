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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maktab.app.network.ApiResult
import com.maktab.app.viewmodel.ClassUi
import com.maktab.app.viewmodel.TeacherViewModel
import com.maktab.app.ui.theme.*

@Composable
fun SinflarimScreen(language: String = "uz", vm: TeacherViewModel = viewModel()) {

    val classesState by vm.classesState.collectAsState()

    LaunchedEffect(Unit) { vm.loadClasses() }

    val title     = when(language){ "ru"->"Мои классы"; "en"->"My Classes"; else->"Sinflarim" }
    val subtitle  = when(language){ "ru"->"Список классов"; "en"->"Assigned classes"; else->"Biriktirilgan sinflar" }
    val classLbl  = when(language){ "ru"->"Класс"; "en"->"Class"; else->"Sinf" }
    val yearLbl   = when(language){ "ru"->"Год"; "en"->"Year"; else->"O'quv yili" }
    val noLbl     = when(language){ "ru"->"Нет классов"; "en"->"No classes"; else->"Sinflar yo'q" }

    var expandedId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Sarlavha
        item {
            Column {
                Text(title,    fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        when (val state = classesState) {

            ApiResult.Loading -> item {
                Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator(color = Teal10, strokeWidth = 2.dp)
                        Text("Sinflar yuklanmoqda...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            is ApiResult.Error -> item {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                        .background(RedContainer).padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.WifiOff, null, tint = Red10, modifier = Modifier.size(16.dp))
                    Text(state.message, fontSize = 13.sp, color = Red10, modifier = Modifier.weight(1f))
                    TextButton(onClick = { vm.loadClasses() }) {
                        Text("Qayta", fontSize = 12.sp, color = Red10)
                    }
                }
            }

            is ApiResult.Success -> {
                val classes = state.data

                // Statistika
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCardSimple(
                            when(language){"ru"->"Классов";"en"->"Classes";else->"Sinflar"},
                            "${classes.size}", Teal10, TealContainer, Modifier.weight(1f)
                        )
                        StatCardSimple(
                            when(language){"ru"->"Уч. год";"en"->"Year";else->"O'quv yili"},
                            classes.firstOrNull()?.academicYear ?: "–", Blue10, BlueContainer, Modifier.weight(1f)
                        )
                    }
                }

                if (classes.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.School, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
                                Text(noLbl, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Backend da o'qituvchiga sinf biriktirilmagan", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    // Sinflarni tartib raqami bo'yicha guruhlaymiz
                    val grouped = classes.groupBy { it.classNo }.toSortedMap()

                    grouped.forEach { (gradeNo, gradeClasses) ->
                        // Guruh sarlavhasi
                        item {
                            Row(
                                Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (gradeNo == 0) "0-SINF" else "$gradeNo-sinf",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Sinf qatorlari
                        items(gradeClasses, key = { it.id }) { cls ->
                            ClassRow(
                                cls        = cls,
                                language   = language,
                                isExpanded = expandedId == cls.id,
                                onToggle   = { expandedId = if (expandedId == cls.id) null else cls.id }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassRow(
    cls: ClassUi,
    language: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, Outline),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sinf belgisi
                Box(
                    Modifier.size(36.dp).clip(RoundedCornerShape(9.dp)).background(TealContainer),
                    Alignment.Center
                ) {
                    Text(
                        cls.name.firstOrNull()?.toString() ?: "?",
                        fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Teal10
                    )
                }
                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(cls.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    if (cls.academicYear.isNotEmpty()) {
                        Text(cls.academicYear, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (cls.studentCount > 0) {
                    Box(
                        Modifier.clip(RoundedCornerShape(6.dp)).background(BlueContainer)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("${cls.studentCount}", fontSize = 12.sp, color = Blue10, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.width(8.dp))
                }

                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, Modifier.size(18.dp), MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Kengaytirilganda — sinf haqida qo'shimcha ma'lumot
            if (isExpanded) {
                HorizontalDivider(color = Outline, thickness = 0.5.dp)
                Column(
                    Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    InfoRow("Sinf raqami", if (cls.classNo == 0) "0 (tayyorlov)" else "${cls.classNo}")
                    InfoRow("O'quv yili", cls.academicYear.ifEmpty { "–" })
                    InfoRow("ID", cls.id)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatCardSimple(label: String, value: String, color: Color, bg: Color, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(10.dp)).background(bg).padding(14.dp)) {
        Column {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = color)
            Text(label, fontSize = 11.sp, color = color.copy(0.75f))
        }
    }
}