package com.maktab.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maktab.app.network.ApiResult
import com.maktab.app.network.models.JournalCellRequest
import com.maktab.app.network.repositories.TeacherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ─────────────────────────────────────────────
// UI State modellari
// ─────────────────────────────────────────────

data class ScheduleDay(
    val dayIndex: Int,
    val dayName: String,
    val date: String,
    val lessons: List<LessonUi>
)

data class LessonUi(
    val id: String,
    val period: Int,
    val time: String,
    val subject: String,
    val className: String,
    val teacher: String,
    val room: String
)

data class JournalOption(
    val id: String,
    val name: String
)

data class JournalStudentRow(
    val studentId: String,
    val studentName: String,
    val initials: String,
    val entries: Map<String, JournalEntryUi>
)

data class JournalEntryUi(
    val grade: Int?,
    val attendance: String,
    val homework: String
)

data class AttendanceRecord(
    val studentId: String,
    val studentName: String,
    val date: String,
    val status: String
)

// Sinflar ekrani uchun
data class ClassUi(
    val id: String,
    val name: String,
    val classNo: Int,
    val academicYear: String,
    val studentCount: Int
)

// ─────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────

class TeacherViewModel : ViewModel() {

    private val repo = TeacherRepository()
    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // ── Jadval ──────────────────────────────
    private val _scheduleState = MutableStateFlow<ApiResult<List<ScheduleDay>>>(ApiResult.Loading)
    val scheduleState: StateFlow<ApiResult<List<ScheduleDay>>> = _scheduleState

    // ── O'quv yili
    private val _academicYear = MutableStateFlow("")
    val academicYear: StateFlow<String> = _academicYear

    // ── Jurnal ──────────────────────────────
    private val _journalOptionsState = MutableStateFlow<ApiResult<Triple<List<JournalOption>, List<JournalOption>, List<JournalOption>>>>(ApiResult.Loading)
    val journalOptionsState: StateFlow<ApiResult<Triple<List<JournalOption>, List<JournalOption>, List<JournalOption>>>> = _journalOptionsState

    private val _journalState = MutableStateFlow<ApiResult<List<JournalStudentRow>>>(ApiResult.Loading)
    val journalState: StateFlow<ApiResult<List<JournalStudentRow>>> = _journalState

    // ── Davomat ─────────────────────────────
    private val _attendanceState = MutableStateFlow<ApiResult<List<AttendanceRecord>>>(ApiResult.Loading)
    val attendanceState: StateFlow<ApiResult<List<AttendanceRecord>>> = _attendanceState

    // ── Sinflar ─────────────────────────────
    private val _classesState = MutableStateFlow<ApiResult<List<ClassUi>>>(ApiResult.Loading)
    val classesState: StateFlow<ApiResult<List<ClassUi>>> = _classesState

    // ── Umumiy xato ─────────────────────────
    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg

    // ─────────────────────────────────────────
    // Haftalik jadval
    // ─────────────────────────────────────────
    fun loadSchedule(weekOffset: Int = 0) {
        viewModelScope.launch {
            _scheduleState.value = ApiResult.Loading

            val academicYearEnd = LocalDate.of(2026, 5, 31)
            val today = LocalDate.now()
            val baseDate = if (!today.isAfter(academicYearEnd))
                today.plusWeeks(weekOffset.toLong())
            else
                academicYearEnd.plusWeeks(weekOffset.toLong())

            val monday = baseDate.with(java.time.DayOfWeek.MONDAY)
            val friday = monday.plusDays(4)

            when (val result = repo.getSchedule(monday.format(fmt), friday.format(fmt))) {
                is ApiResult.Success -> {
                    parseAcademicYear(result.data.result?.data)
                    val days = parseScheduleResponse(result.data.result?.data, monday)
                    _scheduleState.value = ApiResult.Success(days)
                }
                is ApiResult.Error -> {
                    _scheduleState.value = ApiResult.Error(result.message)
                    _errorMsg.value = result.message
                }
                else -> {}
            }
        }
    }

    // ─────────────────────────────────────────
    // Jurnal options — parallel 3 endpoint:
    // 1) journal/options  (teacher ga biriktirilgan)
    // 2) teacher/classes  (barcha sinflar — fallback)
    // 3) teacher/subjects (barcha fanlar   — fallback)
    // 4) academic-quarters (choraklar)
    // ─────────────────────────────────────────
    fun loadJournalOptions() {
        viewModelScope.launch {
            val results = repo.getJournalOptionsParallel()
            // results[0]=journalOpts, [1]=classes, [2]=subjects, [3]=quarters
            val journalOptsResult = results[0]
            val classesResult     = results[1]
            val subjectsResult    = results[2]
            val quartersResult    = results[3]

            // journal/options dan olingan sinflar va fanlar (teacher ga biriktirilgan)
            val (jClasses, jSubjects, jQuarters) = if (journalOptsResult is ApiResult.Success) {
                parseJournalOptions(journalOptsResult.data.result?.data)
            } else Triple(emptyList(), emptyList(), emptyList())

            // Agar journal/options bo'sh — classes/subjects/quarters dan olamiz (fallback)
            val finalClasses = jClasses.ifEmpty {
                if (classesResult is ApiResult.Success)
                    parseClassesList(classesResult.data.result?.data)
                else emptyList()
            }

            val finalSubjects = jSubjects.ifEmpty {
                if (subjectsResult is ApiResult.Success)
                    parseSimpleList(subjectsResult.data.result?.data)
                else emptyList()
            }

            val finalQuarters = jQuarters.ifEmpty {
                if (quartersResult is ApiResult.Success)
                    parseSimpleList(quartersResult.data.result?.data)
                else emptyList()
            }

            if (finalClasses.isEmpty() && finalSubjects.isEmpty() && finalQuarters.isEmpty()) {
                // Hech narsa yo'q — xato yoki bo'sh
                val errMsg = (journalOptsResult as? ApiResult.Error)?.message
                    ?: "O'qituvchiga sinf va fan biriktirilmagan"
                _journalOptionsState.value = ApiResult.Error(errMsg)
            } else {
                _journalOptionsState.value = ApiResult.Success(
                    Triple(finalClasses, finalSubjects, finalQuarters)
                )
            }
        }
    }

    // ─────────────────────────────────────────
    // Jurnal
    // ─────────────────────────────────────────
    fun loadJournal(classId: String, quarterId: String, subjectId: String) {
        viewModelScope.launch {
            _journalState.value = ApiResult.Loading
            when (val result = repo.getJournal(classId, quarterId, subjectId)) {
                is ApiResult.Success -> {
                    _journalState.value = ApiResult.Success(
                        parseJournalData(result.data.result?.data)
                    )
                }
                is ApiResult.Error -> _journalState.value = ApiResult.Error(result.message)
                else -> {}
            }
        }
    }

    // ─────────────────────────────────────────
    // Jurnal katakchasini yangilash
    // ─────────────────────────────────────────
    fun updateCell(studentId: String, sessionId: String, grade: Int?, attendance: String?, homework: String?) {
        viewModelScope.launch {
            val request = JournalCellRequest(studentId, sessionId, grade, attendance, homework)
            when (val result = repo.updateJournalCell(request)) {
                is ApiResult.Error -> _errorMsg.value = result.message
                else -> {}
            }
        }
    }

    // ─────────────────────────────────────────
    // Davomat — api/teacher/attendance/lessons
    // Response: {data: [{id, date, class_id, class_name, students:[{...}]}]}
    // ─────────────────────────────────────────
    fun loadAttendance(classId: String? = null) {
        viewModelScope.launch {
            _attendanceState.value = ApiResult.Loading
            val today   = LocalDate.now()
            val weekAgo = today.minusWeeks(1)
            when (val result = repo.getAttendance(classId, weekAgo.format(fmt), today.format(fmt))) {
                is ApiResult.Success -> {
                    _attendanceState.value = ApiResult.Success(
                        parseAttendanceData(result.data.result?.data)
                    )
                }
                is ApiResult.Error -> {
                    _attendanceState.value = ApiResult.Error(result.message)
                    _errorMsg.value = result.message
                }
                else -> {}
            }
        }
    }

    // ─────────────────────────────────────────
    // Sinflar — api/teacher/classes
    // ─────────────────────────────────────────
    fun loadClasses() {
        viewModelScope.launch {
            _classesState.value = ApiResult.Loading
            when (val result = repo.getClasses()) {
                is ApiResult.Success -> {
                    _classesState.value = ApiResult.Success(
                        parseClassesUi(result.data.result?.data)
                    )
                }
                is ApiResult.Error -> {
                    _classesState.value = ApiResult.Error(result.message)
                    _errorMsg.value = result.message
                }
                else -> {}
            }
        }
    }

    fun clearError() { _errorMsg.value = null }

    // ─────────────────────────────────────────
    // PARSE FUNKSIYALARI
    // ─────────────────────────────────────────

    @Suppress("UNCHECKED_CAST")
    private fun parseAcademicYear(data: Any?) {
        try {
            val map = data as? Map<*, *> ?: return
            val selected = map["selected"] as? Map<*, *> ?: return
            val ay = selected["academic_year"] as? Map<*, *> ?: return
            val name = ay["name"]?.toString() ?: return
            if (name.isNotEmpty()) _academicYear.value = name
        } catch (_: Exception) {}
    }

    // id yoki _id — backend ikki xil qaytaradi
    private fun Map<*, *>.resolveId(): String =
        this["id"]?.toString()?.takeIf { it.isNotEmpty() }
            ?: this["_id"]?.toString() ?: ""

    @Suppress("UNCHECKED_CAST")
    private fun parseScheduleResponse(
        data: Any?,
        weekMonday: LocalDate = LocalDate.now()
    ): List<ScheduleDay> {
        val dayNames = listOf("Dushanba", "Seshanba", "Chorshanba", "Payshanba", "Juma", "Shanba")
        if (data == null) return emptySchedule(dayNames, weekMonday)

        return try {
            val map = data as? Map<*, *> ?: return emptySchedule(dayNames, weekMonday)
            val builder = map["builder"] as? Map<*, *>
            val scheduledLessons = builder?.get("scheduled_lessons") as? List<*>

            if (scheduledLessons.isNullOrEmpty()) return emptySchedule(dayNames, weekMonday)

            val lessonsByDay = mutableMapOf<Int, MutableList<LessonUi>>()
            (0..5).forEach { lessonsByDay[it] = mutableListOf() }

            scheduledLessons.forEach { item ->
                val l = item as? Map<*, *> ?: return@forEach
                val dateStr = l["lesson_date"]?.toString() ?: return@forEach
                val lessonDate = try {
                    LocalDate.parse(dateStr.substringBefore("T"))
                } catch (_: Exception) { return@forEach }

                val dayIdx = when (lessonDate.dayOfWeek) {
                    java.time.DayOfWeek.MONDAY    -> 0
                    java.time.DayOfWeek.TUESDAY   -> 1
                    java.time.DayOfWeek.WEDNESDAY -> 2
                    java.time.DayOfWeek.THURSDAY  -> 3
                    java.time.DayOfWeek.FRIDAY    -> 4
                    java.time.DayOfWeek.SATURDAY  -> 5
                    else -> return@forEach
                }

                val slot      = l["lesson_slot"] as? Map<*, *>
                val subject   = (l["subject"] as? Map<*, *>)?.get("name")?.toString()
                    ?: l["subject_name"]?.toString() ?: ""
                val className = (l["class"] as? Map<*, *>)?.get("name")?.toString()
                    ?: l["class_name"]?.toString() ?: ""
                val room      = (l["classroom"] as? Map<*, *>)?.get("name")?.toString()
                    ?: l["classroom_name"]?.toString() ?: ""
                val startTime = slot?.get("start_time")?.toString() ?: l["start_time"]?.toString() ?: ""
                val endTime   = slot?.get("end_time")?.toString()   ?: l["end_time"]?.toString()   ?: ""
                val lessonNum = (slot?.get("lesson_number") as? Number)?.toInt()
                    ?: (l["lesson_number"] as? Number)?.toInt()
                    ?: (lessonsByDay[dayIdx]!!.size + 1)

                lessonsByDay[dayIdx]!!.add(
                    LessonUi(
                        id        = l.resolveId(),
                        period    = lessonNum,
                        time      = if (startTime.isNotEmpty() && endTime.isNotEmpty()) "$startTime–$endTime" else "",
                        subject   = subject,
                        className = className,
                        teacher   = "",
                        room      = room
                    )
                )
            }

            (0..5).map { idx ->
                ScheduleDay(
                    dayIndex = idx,
                    dayName  = dayNames[idx],
                    date     = weekMonday.plusDays(idx.toLong()).format(fmt),
                    lessons  = lessonsByDay[idx]!!.sortedBy { it.period }
                )
            }
        } catch (_: Exception) {
            emptySchedule(dayNames, weekMonday)
        }
    }

    private fun emptySchedule(dayNames: List<String>, weekMonday: LocalDate) =
        (0..5).map { idx ->
            ScheduleDay(idx, dayNames[idx], weekMonday.plusDays(idx.toLong()).format(fmt), emptyList())
        }

    // journal/options dan: {classes:[], subjects:[], quarters:[]}
    @Suppress("UNCHECKED_CAST")
    private fun parseJournalOptions(data: Any?): Triple<List<JournalOption>, List<JournalOption>, List<JournalOption>> {
        if (data == null) return Triple(emptyList(), emptyList(), emptyList())
        return try {
            val map = data as? Map<*, *> ?: return Triple(emptyList(), emptyList(), emptyList())
            fun parseList(key: String) = (map[key] as? List<*>)?.mapNotNull { item ->
                val m = item as? Map<*, *> ?: return@mapNotNull null
                JournalOption(m.resolveId(), m["name"]?.toString() ?: "")
            } ?: emptyList()
            Triple(parseList("classes"), parseList("subjects"), parseList("quarters"))
        } catch (_: Exception) { Triple(emptyList(), emptyList(), emptyList()) }
    }

    // Umumiy list parse — id/name juftligi
    @Suppress("UNCHECKED_CAST")
    private fun parseSimpleList(data: Any?): List<JournalOption> {
        if (data == null) return emptyList()
        return try {
            val list: List<*> = when (data) {
                is List<*>   -> data
                is Map<*, *> -> (data["items"] as? List<*>)
                    ?: (data["data"] as? List<*>)
                    ?: return emptyList()
                else -> return emptyList()
            }
            list.mapNotNull { item ->
                val m = item as? Map<*, *> ?: return@mapNotNull null
                val id   = m.resolveId().takeIf { it.isNotEmpty() } ?: return@mapNotNull null
                val name = m["name"]?.toString() ?: return@mapNotNull null
                JournalOption(id, name)
            }
        } catch (_: Exception) { emptyList() }
    }

    // api/teacher/classes → JournalOption list (id, name)
    @Suppress("UNCHECKED_CAST")
    private fun parseClassesList(data: Any?): List<JournalOption> {
        if (data == null) return emptyList()
        return try {
            val list: List<*> = when (data) {
                is List<*>   -> data
                is Map<*, *> -> (data["data"] as? List<*>) ?: return emptyList()
                else -> return emptyList()
            }
            list.mapNotNull { item ->
                val m = item as? Map<*, *> ?: return@mapNotNull null
                val id   = m.resolveId().takeIf { it.isNotEmpty() } ?: return@mapNotNull null
                val name = m["name"]?.toString() ?: m["class_name"]?.toString() ?: return@mapNotNull null
                JournalOption(id, name)
            }
        } catch (_: Exception) { emptyList() }
    }

    // api/teacher/classes → ClassUi list (sinflarim ekrani uchun)
    @Suppress("UNCHECKED_CAST")
    private fun parseClassesUi(data: Any?): List<ClassUi> {
        if (data == null) return emptyList()
        return try {
            val list: List<*> = when (data) {
                is List<*>   -> data
                is Map<*, *> -> (data["data"] as? List<*>) ?: return emptyList()
                else -> return emptyList()
            }
            list.mapNotNull { item ->
                val m = item as? Map<*, *> ?: return@mapNotNull null
                val id   = m.resolveId().takeIf { it.isNotEmpty() } ?: return@mapNotNull null
                val name = m["name"]?.toString() ?: m["class_name"]?.toString() ?: return@mapNotNull null
                ClassUi(
                    id           = id,
                    name         = name,
                    classNo      = (m["class"] as? Number)?.toInt() ?: 0,
                    academicYear = m["academic_year"]?.toString() ?: "",
                    studentCount = (m["capacity"] as? Number)?.toInt() ?: 0
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseJournalData(data: Any?): List<JournalStudentRow> {
        if (data == null) return emptyList()
        return try {
            val map = data as? Map<*, *> ?: return emptyList()
            val students = (map["students"] as? List<*>) ?: return emptyList()
            students.mapNotNull { item ->
                val m    = item as? Map<*, *> ?: return@mapNotNull null
                val name = m["name"]?.toString() ?: return@mapNotNull null
                val parts    = name.trim().split(" ")
                val initials = parts.take(2).joinToString("") { it.firstOrNull()?.uppercaseChar()?.toString() ?: "" }
                val entries  = (m["entries"] as? Map<*, *>)?.mapNotNull { (k, v) ->
                    val em = v as? Map<*, *> ?: return@mapNotNull null
                    k.toString() to JournalEntryUi(
                        grade      = (em["grade"] as? Number)?.toInt(),
                        attendance = em["attendance"]?.toString() ?: "present",
                        homework   = em["homework"]?.toString() ?: "-"
                    )
                }?.toMap() ?: emptyMap()
                JournalStudentRow(
                    studentId   = m.resolveId(),
                    studentName = name,
                    initials    = initials,
                    entries     = entries
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    // attendance/lessons response:
    // data: [{id, date, class_name, students:[{student_id, student_name, status, ...}]}]
    // Yoki flat list: [{student_id, student_name, date, status}]
    @Suppress("UNCHECKED_CAST")
    private fun parseAttendanceData(data: Any?): List<AttendanceRecord> {
        if (data == null) return emptyList()
        return try {
            val rawList: List<*> = when (data) {
                is List<*>   -> data
                is Map<*, *> -> (data["data"] as? List<*>)
                    ?: (data["records"] as? List<*>)
                    ?: return emptyList()
                else -> return emptyList()
            }
            if (rawList.isEmpty()) return emptyList()

            // Birinchi elementga qarab formatni aniqlaymiz
            val first = rawList.firstOrNull() as? Map<*, *>
            val isNestedFormat = first?.containsKey("students") == true

            if (isNestedFormat) {
                // Nested format: [{date, class_name, students:[{student_id, student_name, status}]}]
                val records = mutableListOf<AttendanceRecord>()
                rawList.forEach { lessonItem ->
                    val lesson   = lessonItem as? Map<*, *> ?: return@forEach
                    val date     = lesson["date"]?.toString()?.substringBefore("T") ?: ""
                    val students = lesson["students"] as? List<*> ?: return@forEach
                    students.forEach { studentItem ->
                        val s = studentItem as? Map<*, *> ?: return@forEach
                        records.add(AttendanceRecord(
                            studentId   = s["student_id"]?.toString() ?: s.resolveId(),
                            studentName = s["student_name"]?.toString()
                                ?: s["name"]?.toString()
                                ?: s["fullname"]?.toString() ?: "",
                            date        = date,
                            status      = s["status"]?.toString() ?: "present"
                        ))
                    }
                }
                records
            } else {
                // Flat format: [{student_id, student_name, date, status}]
                rawList.mapNotNull { item ->
                    val m = item as? Map<*, *> ?: return@mapNotNull null
                    AttendanceRecord(
                        studentId   = m["student_id"]?.toString() ?: m.resolveId(),
                        studentName = m["student_name"]?.toString()
                            ?: m["name"]?.toString()
                            ?: m["fullname"]?.toString() ?: "",
                        date        = m["date"]?.toString()?.substringBefore("T") ?: "",
                        status      = m["status"]?.toString() ?: "present"
                    )
                }
            }
        } catch (_: Exception) { emptyList() }
    }
}