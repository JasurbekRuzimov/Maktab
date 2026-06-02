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
import com.maktab.app.ui.components.*
import com.maktab.app.ui.theme.*

// ─────────────────────────────────────────────
// MOCK DATA — HR
// ─────────────────────────────────────────────

private data class Employee(
    val id: Int, val name: String, val initials: String,
    val position: String, val department: String,
    val phone: String, val joinDate: String, val status: String
)

private data class LeaveRequest(
    val id: Int, val employeeName: String, val initials: String,
    val type: String, val from: String, val to: String,
    val days: Int, val reason: String, val status: String
)

private data class SalaryRecord(
    val employeeId: Int, val name: String, val initials: String,
    val position: String, val base: String, val bonus: String,
    val total: String, val month: String, val paid: Boolean
)

private data class HRDocument(
    val id: Int, val title: String, val type: String,
    val employee: String, val date: String, val size: String
)

private object HRMock {
    val employees = listOf(
        Employee(1, "Karimova Nargiza", "KN", "Matematika o'qituvchisi", "O'qituvchilar", "+998 90 123 45 67", "01.09.2020", "Faol"),
        Employee(2, "Tursunov Bobur",   "TB", "Fizika o'qituvchisi",     "O'qituvchilar", "+998 91 234 56 78", "15.08.2019", "Faol"),
        Employee(3, "Alimova Feruza",   "AF", "Ingliz tili o'qituvchisi","O'qituvchilar", "+998 93 345 67 89", "01.09.2021", "Faol"),
        Employee(4, "Toshmatov Sardor", "TS", "Oshpaz",                  "Kafeteriya",    "+998 94 456 78 90", "10.01.2022", "Faol"),
        Employee(5, "Nazarov Alisher",  "NA", "Qorovul",                 "Xavfsizlik",    "+998 95 567 89 01", "01.03.2018", "Faol"),
        Employee(6, "Yusupova Malika",  "YM", "Kutubxonachi",            "Kutubxona",     "+998 97 678 90 12", "15.09.2022", "Ta'tilda"),
    )
    val departments = employees.map { it.department }.distinct()
    val positions   = employees.map { it.position }.distinct()

    val leaveRequests = listOf(
        LeaveRequest(1, "Yusupova Malika",  "YM", "Yillik ta'til", "01.06.2026", "15.06.2026", 15, "Yillik ta'til", "Tasdiqlandi"),
        LeaveRequest(2, "Tursunov Bobur",   "TB", "Kasallik",      "28.05.2026", "30.05.2026",  3, "Kasallik",      "Tasdiqlandi"),
        LeaveRequest(3, "Alimova Feruza",   "AF", "Shaxsiy",       "05.06.2026", "06.06.2026",  2, "Oilaviy sabablar", "Kutilmoqda"),
        LeaveRequest(4, "Karimova Nargiza", "KN", "Yillik ta'til", "20.06.2026", "04.07.2026", 15, "Yillik ta'til", "Kutilmoqda"),
    )

    val salaries = listOf(
        SalaryRecord(1, "Karimova Nargiza", "KN", "Matematika o'qituvchisi", "3 500 000", "350 000", "3 850 000", "May 2026", true),
        SalaryRecord(2, "Tursunov Bobur",   "TB", "Fizika o'qituvchisi",     "3 200 000", "320 000", "3 520 000", "May 2026", true),
        SalaryRecord(3, "Alimova Feruza",   "AF", "Ingliz tili o'qituvchisi","3 200 000", "0",       "3 200 000", "May 2026", false),
        SalaryRecord(4, "Toshmatov Sardor", "TS", "Oshpaz",                  "2 800 000", "280 000", "3 080 000", "May 2026", true),
        SalaryRecord(5, "Nazarov Alisher",  "NA", "Qorovul",                 "2 500 000", "0",       "2 500 000", "May 2026", false),
        SalaryRecord(6, "Yusupova Malika",  "YM", "Kutubxonachi",            "2 800 000", "200 000", "3 000 000", "May 2026", true),
    )

    val documents = listOf(
        HRDocument(1, "Karimova N. — Mehnat shartnomasi", "Shartnoma",  "Karimova Nargiza",  "01.09.2020", "245 KB"),
        HRDocument(2, "Tursunov B. — Buyruq #45",         "Buyruq",     "Tursunov Bobur",    "15.05.2026", "128 KB"),
        HRDocument(3, "Alimova F. — Ta'til buyrug'i",     "Buyruq",     "Alimova Feruza",    "03.06.2026", "98 KB"),
        HRDocument(4, "Toshmatov S. — Mehnat shartnomasi","Shartnoma",  "Toshmatov Sardor",  "10.01.2022", "230 KB"),
        HRDocument(5, "Umumiy — Ichki qoidalar 2026",     "Nizomlar",   "Barcha xodimlar",   "01.01.2026", "512 KB"),
    )

    // Davomat — xodimlar uchun haftalik (true = keldi)
    val attendance = mapOf(
        1 to listOf(true, true, true, true, true),
        2 to listOf(true, true, false, true, true),
        3 to listOf(true, true, true, true, false),
        4 to listOf(true, false, true, true, true),
        5 to listOf(true, true, true, true, true),
        6 to listOf(false, false, false, false, false),
    )
}

// ─────────────────────────────────────────────
// 1. XODIMLAR RO'YXATI
// ─────────────────────────────────────────────

@Composable
fun HRXodimlarScreen() {
    var search      by remember { mutableStateOf("") }
    var selDept     by remember { mutableStateOf("Barchasi") }
    val depts       = listOf("Barchasi") + HRMock.departments
    val filtered    = HRMock.employees.filter { emp ->
        (selDept == "Barchasi" || emp.department == selDept) &&
                emp.name.contains(search, ignoreCase = true)
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Jami", "${HRMock.employees.size}", Purple10, Modifier.weight(1f))
                StatCard("Faol", "${HRMock.employees.count { it.status == "Faol" }}", Teal10, Modifier.weight(1f))
                StatCard("Ta'tilda", "${HRMock.employees.count { it.status == "Ta'tilda" }}", Amber10, Modifier.weight(1f))
            }
        }
        item {
            OutlinedTextField(
                value = search, onValueChange = { search = it },
                placeholder = { Text("Xodim qidirish...", fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(10.dp),
                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Purple10, unfocusedBorderColor = Outline)
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(depts) { dept ->
                    val sel = selDept == dept
                    FilterChip(
                        selected = sel, onClick = { selDept = dept },
                        label = { Text(dept, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Purple10, selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true, selected = sel,
                            borderColor = Outline, selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }
        }
        items(filtered) { emp -> EmployeeCard(emp) }
    }
}

@Composable
private fun EmployeeCard(emp: Employee) {
    val isActive = emp.status == "Faol"
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, Outline),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AvatarCircle(emp.initials, Purple10, 48.dp)
            Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(emp.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    StatusChip(emp.status, if (isActive) Teal10 else Amber10, if (isActive) TealContainer else AmberContainer)
                }
                Text(emp.position, fontSize = 12.sp, color = Purple10, fontWeight = FontWeight.Medium)
                Text(emp.department, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                        Text(emp.phone, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                        Text(emp.joinDate, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 2. YANGI XODIM QO'SHISH
// ─────────────────────────────────────────────

@Composable
fun HRYangiXodimScreen() {
    var name     by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("") }
    var dept     by remember { mutableStateOf("") }
    var phone    by remember { mutableStateOf("") }
    var date     by remember { mutableStateOf("") }
    var saved    by remember { mutableStateOf(false) }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { SectionHeader("Yangi xodim qo'shish") {} }
        item {
            AppCard {
                Text("SHAXSIY MA'LUMOTLAR", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.8.sp)
                Spacer(Modifier.height(14.dp))
                HRField("To'liq ism", name, { name = it }, Icons.Default.Person, "Masalan: Karimova Nargiza")
                Spacer(Modifier.height(12.dp))
                HRField("Telefon raqami", phone, { phone = it }, Icons.Default.Phone, "+998 XX XXX XX XX")
                Spacer(Modifier.height(12.dp))
                HRField("Ishga kirgan sana", date, { date = it }, Icons.Default.CalendarToday, "DD.MM.YYYY")
            }
        }
        item {
            AppCard {
                Text("LAVOZIM", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.8.sp)
                Spacer(Modifier.height(14.dp))
                HRField("Lavozim", position, { position = it }, Icons.Default.Work, "Masalan: Matematika o'qituvchisi")
                Spacer(Modifier.height(12.dp))
                Text("Bo'lim", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                HRMock.departments.chunked(2).forEach { chunk ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                        chunk.forEach { d ->
                            val sel = dept == d
                            Surface(
                                onClick = { dept = d }, modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                color = if (sel) PurpleContainer else Color.Transparent,
                                border = BorderStroke(0.5.dp, if (sel) Purple10 else Outline)
                            ) {
                                Text(d, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    fontSize = 12.sp, textAlign = TextAlign.Center,
                                    color = if (sel) Purple10 else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (sel) FontWeight.Medium else FontWeight.Normal)
                            }
                        }
                        if (chunk.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        item {
            if (saved) {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(TealContainer).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.CheckCircle, null, tint = Teal10, modifier = Modifier.size(20.dp))
                    Text("Xodim muvaffaqiyatli qo'shildi!", fontSize = 14.sp, color = Teal10)
                }
            } else {
                Button(
                    onClick = { if (name.isNotEmpty() && position.isNotEmpty()) saved = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = name.isNotEmpty() && position.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Purple10),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Xodimni qo'shish", fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun HRField(label: String, value: String, onValueChange: (String) -> Unit,
                    icon: androidx.compose.ui.graphics.vector.ImageVector, placeholder: String) {
    Column {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            shape = RoundedCornerShape(10.dp),
            placeholder = { Text(placeholder, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingIcon = { Icon(icon, null, tint = Purple10, modifier = Modifier.size(18.dp)) },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Purple10, unfocusedBorderColor = Outline)
        )
    }
}

// ─────────────────────────────────────────────
// 3. LAVOZIMLAR
// ─────────────────────────────────────────────

@Composable
fun HRLavozimlarScreen() {
    val deptColors = listOf(
        Pair(Purple10, PurpleContainer), Pair(Blue10, BlueContainer),
        Pair(Teal10, TealContainer), Pair(Amber10, AmberContainer)
    )
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SectionHeader("Lavozimlar va bo'limlar") {} }
        itemsIndexed(HRMock.departments) { idx, dept ->
            val (color, container) = deptColors[idx % deptColors.size]
            val deptEmps = HRMock.employees.filter { it.department == dept }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, Outline),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(container), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Business, null, tint = color, modifier = Modifier.size(20.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Text(dept, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("${deptEmps.size} xodim", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusChip("${deptEmps.size}", color, container)
                    }
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = Outline, thickness = 0.5.dp)
                    Spacer(Modifier.height(10.dp))
                    deptEmps.forEach { emp ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            AvatarCircle(emp.initials, color, 30.dp)
                            Column(Modifier.weight(1f)) {
                                Text(emp.name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(emp.position, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusChip(emp.status, if (emp.status == "Faol") Teal10 else Amber10, if (emp.status == "Faol") TealContainer else AmberContainer)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 4. XODIMLAR DAVOMATI
// ─────────────────────────────────────────────

@Composable
fun HRDavomatScreen() {
    val days = listOf("Du", "Se", "Ch", "Pa", "Ju")
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            SectionHeader("Xodimlar davomati") {}
            Spacer(Modifier.height(4.dp))
            Text("28 May – 1 Iyun 2026 · Haftalik", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            val totalPresent = HRMock.attendance.values.sumOf { it.count { p -> p } }
            val totalDays    = HRMock.employees.size * 5
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Jami kun", "$totalDays", Purple10, Modifier.weight(1f))
                StatCard("Keldi", "$totalPresent", Teal10, Modifier.weight(1f))
                StatCard("Kelmadi", "${totalDays - totalPresent}", Red10, Modifier.weight(1f))
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp), border = BorderStroke(0.5.dp, Outline), elevation = CardDefaults.cardElevation(0.dp)) {
                Column(Modifier.padding(14.dp)) {
                    // Header
                    Row(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                        Text("Xodim", fontSize = 11.sp, fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(2f))
                        days.forEach { d ->
                            Text(d, fontSize = 11.sp, fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        }
                    }
                    HorizontalDivider(color = Outline, thickness = 0.5.dp)
                    HRMock.employees.forEachIndexed { idx, emp ->
                        if (idx > 0) HorizontalDivider(color = Outline, thickness = 0.5.dp)
                        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Row(Modifier.weight(2f), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                AvatarCircle(emp.initials, Purple10, 24.dp)
                                Text(emp.name.split(" ").take(2).joinToString(" "), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            val att = HRMock.attendance[emp.id] ?: emptyList()
                            att.forEach { present ->
                                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    Box(Modifier.size(22.dp).clip(CircleShape)
                                        .background(if (present) TealContainer else RedContainer),
                                        contentAlignment = Alignment.Center) {
                                        Icon(if (present) Icons.Default.Check else Icons.Default.Close,
                                            null, tint = if (present) Teal10 else Red10, modifier = Modifier.size(12.dp))
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
// 5. TA'TIL VA RUXSATLAR
// ─────────────────────────────────────────────

@Composable
fun HRTatilScreen() {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Jami", "${HRMock.leaveRequests.size}", Purple10, Modifier.weight(1f))
                StatCard("Tasdiqlandi", "${HRMock.leaveRequests.count { it.status == "Tasdiqlandi" }}", Teal10, Modifier.weight(1f))
                StatCard("Kutilmoqda", "${HRMock.leaveRequests.count { it.status == "Kutilmoqda" }}", Amber10, Modifier.weight(1f))
            }
        }
        item { Text("So'rovlar", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
        items(HRMock.leaveRequests) { req ->
            val isApproved = req.status == "Tasdiqlandi"
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp), border = BorderStroke(0.5.dp, Outline), elevation = CardDefaults.cardElevation(0.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        AvatarCircle(req.initials, Purple10, 44.dp)
                        Column(Modifier.weight(1f)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(req.employeeName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                StatusChip(req.status, if (isApproved) Teal10 else Amber10, if (isApproved) TealContainer else AmberContainer)
                            }
                            StatusChip(req.type, Purple10, PurpleContainer)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = Outline, thickness = 0.5.dp)
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Boshlanish", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(req.from, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Icon(Icons.Default.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Tugash", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(req.to, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Kunlar", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${req.days} kun", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Purple10)
                        }
                    }
                    if (!isApproved) {
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = {}, modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(0.dp),
                                border = BorderStroke(0.5.dp, Red10)) {
                                Icon(Icons.Default.Close, null, Modifier.size(14.dp), tint = Red10)
                                Spacer(Modifier.width(4.dp))
                                Text("Rad etish", fontSize = 12.sp, color = Red10)
                            }
                            Button(onClick = {}, modifier = Modifier.weight(1f).height(40.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Teal10),
                                shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(0.dp)) {
                                Icon(Icons.Default.Check, null, Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Tasdiqlash", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 6. MAOSH HISOB-KITOBI
// ─────────────────────────────────────────────

@Composable
fun HRMaoshScreen() {
    val totalSalary = "16 150 000 UZS"
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SectionHeader("Maosh hisob-kitobi") {} }
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = PurpleContainer),
                shape = RoundedCornerShape(12.dp), border = BorderStroke(0.5.dp, Purple10.copy(0.3f)),
                elevation = CardDefaults.cardElevation(0.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("May 2026 — Jami maosh fondi", fontSize = 12.sp, color = Purple10.copy(0.8f), fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Text(totalSalary, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Purple10)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text("To'landi", fontSize = 11.sp, color = Purple10.copy(0.7f))
                            Text("${HRMock.salaries.count { it.paid }} xodim", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Teal10)
                        }
                        Column {
                            Text("Kutilmoqda", fontSize = 11.sp, color = Purple10.copy(0.7f))
                            Text("${HRMock.salaries.count { !it.paid }} xodim", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Amber10)
                        }
                    }
                }
            }
        }
        items(HRMock.salaries) { sal ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp), border = BorderStroke(0.5.dp, Outline), elevation = CardDefaults.cardElevation(0.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        AvatarCircle(sal.initials, Purple10, 44.dp)
                        Column(Modifier.weight(1f)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(sal.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                StatusChip(if (sal.paid) "To'landi" else "Kutilmoqda",
                                    if (sal.paid) Teal10 else Amber10, if (sal.paid) TealContainer else AmberContainer)
                            }
                            Text(sal.position, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = Outline, thickness = 0.5.dp)
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Asosiy", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${sal.base} UZS", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Bonus", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${sal.bonus} UZS", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Teal10)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Jami", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${sal.total} UZS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Purple10)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 7. HR ANALITIKA
// ─────────────────────────────────────────────

@Composable
fun HRAnalitika() {
    val deptCounts = HRMock.departments.map { dept ->
        dept to HRMock.employees.count { it.department == dept }
    }
    val maxCount = deptCounts.maxOf { it.second }.toFloat()
    val deptColors = listOf(Purple10, Blue10, Teal10, Amber10, Red10)

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SectionHeader("HR Analitika") {} }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Jami xodim", "${HRMock.employees.size}", Purple10, Modifier.weight(1f))
                StatCard("Bu oyda qabul", "1", Teal10, Modifier.weight(1f))
                StatCard("Ta'tilda", "${HRMock.employees.count { it.status == "Ta'tilda" }}", Amber10, Modifier.weight(1f))
            }
        }
        // Bo'lim bo'yicha
        item {
            AppCard {
                Text("Bo'limlar bo'yicha xodimlar", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(14.dp))
                deptCounts.forEachIndexed { idx, (dept, count) ->
                    val color = deptColors[idx % deptColors.size]
                    Column(Modifier.padding(bottom = 10.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(dept, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("$count xodim", fontSize = 12.sp, color = color, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(4.dp))
                        Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                            Box(Modifier.fillMaxWidth(count / maxCount).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(color))
                        }
                    }
                }
            }
        }
        // Davomat statistikasi
        item {
            AppCard {
                Text("Haftalik davomat statistikasi", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(14.dp))
                val days = listOf("Dush", "Sesh", "Chor", "Pay", "Jum")
                val dayPresent = (0..4).map { dayIdx ->
                    HRMock.attendance.values.count { it.getOrElse(dayIdx) { false } }
                }
                Row(Modifier.fillMaxWidth().height(100.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                    dayPresent.zip(days).forEach { (count, day) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                            Text("$count", fontSize = 11.sp, color = Purple10, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Box(Modifier.width(32.dp).height((count * 12).dp).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(PurpleContainer.copy(if (count == dayPresent.max()) 1f else 0.6f)))
                            Spacer(Modifier.height(4.dp))
                            Text(day, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        // Maosh umumiy
        item {
            AppCard {
                Text("Maosh taqsimoti", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf(
                        Triple("Jami fond", "16.15M", Purple10),
                        Triple("O'rtacha", "2.69M", Blue10),
                        Triple("Eng yuqori", "3.85M", Teal10)
                    ).forEach { (label, value, color) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
                            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 8. HUJJATLAR
// ─────────────────────────────────────────────

@Composable
fun HRHujjatlarScreen() {
    var search by remember { mutableStateOf("") }
    val filtered = HRMock.documents.filter { it.title.contains(search, ignoreCase = true) }
    val typeColors = mapOf("Shartnoma" to Pair(Blue10, BlueContainer), "Buyruq" to Pair(Purple10, PurpleContainer), "Nizomlar" to Pair(Teal10, TealContainer))

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Hujjatlar", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Purple10),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp), modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Yangi", fontSize = 12.sp)
                }
            }
        }
        item {
            OutlinedTextField(
                value = search, onValueChange = { search = it },
                placeholder = { Text("Hujjat qidirish...", fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(10.dp),
                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Purple10, unfocusedBorderColor = Outline)
            )
        }
        items(filtered) { doc ->
            val (color, container) = typeColors[doc.type] ?: Pair(Purple10, PurpleContainer)
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp), border = BorderStroke(0.5.dp, Outline), elevation = CardDefaults.cardElevation(0.dp)) {
                Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(container), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Description, null, tint = color, modifier = Modifier.size(22.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(doc.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, lineHeight = 17.sp)
                        Spacer(Modifier.height(3.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusChip(doc.type, color, container)
                            Text(doc.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(doc.size, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Download, null, tint = Purple10, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}