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

// ─────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────

class TeacherViewModel : ViewModel() {

    private val repo = TeacherRepository()
    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // ── Jadval ──────────────────────────────
    private val _scheduleState = MutableStateFlow<ApiResult<List<ScheduleDay>>>(ApiResult.Loading)
    val scheduleState: StateFlow<ApiResult<List<ScheduleDay>>> = _scheduleState

    // ── O'quv yili va filial (jadval API dan keladi) ──
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

    // ── Umumiy xato xabari ──────────────────
    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg

    // ─────────────────────────────────────────
    // Haftalik jadval yuklash
    // weekOffset: 0 = joriy/so'nggi o'quv haftasi
    // ─────────────────────────────────────────
    fun loadSchedule(weekOffset: Int = 0) {
        viewModelScope.launch {
            _scheduleState.value = ApiResult.Loading

            // O'quv yili: 2025-09-01 dan 2026-05-31 gacha
            // Bugun (2026-06-04) o'quv yilidan tashqarida,
            // shuning uchun o'quv yilining so'nggi haftasini ishlatamiz
            val academicYearEnd = java.time.LocalDate.of(2026, 5, 31)
            val today = java.time.LocalDate.now()

            // Asosiy sana — o'quv yili ichida bo'lsa bugun, bo'lmasa oxirgi kun
            val basDate = if (today.isBefore(academicYearEnd) || today == academicYearEnd) {
                today.plusWeeks(weekOffset.toLong())
            } else {
                academicYearEnd.plusWeeks(weekOffset.toLong())
            }

            val monday = basDate.with(java.time.DayOfWeek.MONDAY)
            val friday = monday.plusDays(4)

            when (val result = repo.getSchedule(monday.format(fmt), friday.format(fmt))) {
                is ApiResult.Success -> {
                    val days = parseScheduleResponse(result.data.result?.data, monday)
                    _scheduleState.value = ApiResult.Success(days)
                    // O'quv yilini API javobidan olamiz
                    parseAcademicYear(result.data.result?.data)
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
    // Jurnal options (sinflar, fanlar, choraklar)
    // ─────────────────────────────────────────
    fun loadJournalOptions() {
        viewModelScope.launch {
            when (val result = repo.getJournalOptions()) {
                is ApiResult.Success -> {
                    val options = parseJournalOptions(result.data.result?.data)
                    _journalOptionsState.value = ApiResult.Success(options)
                }
                is ApiResult.Error -> {
                    _journalOptionsState.value = ApiResult.Error(result.message)
                }
                else -> {}
            }
        }
    }

    // ─────────────────────────────────────────
    // Jurnal ma'lumotlari
    // ─────────────────────────────────────────
    fun loadJournal(classId: String, quarterId: String, subjectId: String) {
        viewModelScope.launch {
            _journalState.value = ApiResult.Loading
            when (val result = repo.getJournal(classId, quarterId, subjectId)) {
                is ApiResult.Success -> {
                    val rows = parseJournalData(result.data.result?.data)
                    _journalState.value = ApiResult.Success(rows)
                }
                is ApiResult.Error -> {
                    _journalState.value = ApiResult.Error(result.message)
                }
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
    // Davomat
    // ─────────────────────────────────────────
    fun loadAttendance(classId: String? = null) {
        viewModelScope.launch {
            _attendanceState.value = ApiResult.Loading
            val today = LocalDate.now()
            val weekAgo = today.minusWeeks(1)
            when (val result = repo.getAttendance(classId, weekAgo.format(fmt), today.format(fmt))) {
                is ApiResult.Success -> {
                    val records = parseAttendanceData(result.data.result?.data)
                    _attendanceState.value = ApiResult.Success(records)
                }
                is ApiResult.Error -> {
                    _attendanceState.value = ApiResult.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun clearError() { _errorMsg.value = null }

    @Suppress("UNCHECKED_CAST")
    private fun parseAcademicYear(data: Any?) {
        try {
            val map = data as? Map<*, *> ?: return
            val selected = map["selected"] as? Map<*, *> ?: return
            val ay = selected["academic_year"] as? Map<*, *> ?: return
            val name = ay["name"]?.toString() ?: return
            if (name.isNotEmpty()) _academicYear.value = name
        } catch (e: Exception) { }
    }

    // ─────────────────────────────────────────
    // PARSE FUNKSIYALARI
    // Backend dan kelgan Map<*, *> ni UI modellariga o'tkazish
    // ─────────────────────────────────────────

    @Suppress("UNCHECKED_CAST")
    private fun parseScheduleResponse(data: Any?, weekMonday: java.time.LocalDate = java.time.LocalDate.now()): List<ScheduleDay> {
        val dayNames = listOf("Dushanba", "Seshanba", "Chorshanba", "Payshanba", "Juma", "Shanba")
        if (data == null) return emptyList()

        return try {
            val map = data as? Map<*, *> ?: return emptyList()
            val builder = map["builder"] as? Map<*, *>
            val scheduledLessons = builder?.get("scheduled_lessons") as? List<*>
            val lessonSlots = builder?.get("lesson_slots") as? List<*>

            // Agar dars yo'q bo'lsa — bo'sh jadval qaytaramiz (6 kun)
            if (scheduledLessons.isNullOrEmpty()) {
                return (0..5).map { idx ->
                    ScheduleDay(
                        dayIndex = idx,
                        dayName = dayNames[idx],
                        date = weekMonday.plusDays(idx.toLong()).format(fmt),
                        lessons = emptyList()
                    )
                }
            }

            // Darslarni kun bo'yicha guruhlash
            val lessonsByDay = mutableMapOf<Int, MutableList<LessonUi>>()
            (0..5).forEach { lessonsByDay[it] = mutableListOf() }

            scheduledLessons.forEach { item ->
                val l = item as? Map<*, *> ?: return@forEach
                val dateStr = l["lesson_date"]?.toString() ?: return@forEach
                val lessonDate = try {
                    java.time.LocalDate.parse(dateStr.substringBefore("T"))
                } catch (e: Exception) { return@forEach }

                val dayIdx = when (lessonDate.dayOfWeek) {
                    java.time.DayOfWeek.MONDAY    -> 0
                    java.time.DayOfWeek.TUESDAY   -> 1
                    java.time.DayOfWeek.WEDNESDAY -> 2
                    java.time.DayOfWeek.THURSDAY  -> 3
                    java.time.DayOfWeek.FRIDAY    -> 4
                    java.time.DayOfWeek.SATURDAY  -> 5
                    else -> return@forEach
                }

                val slot = l["lesson_slot"] as? Map<*, *>
                val subject = (l["subject"] as? Map<*, *>)?.get("name")?.toString()
                    ?: l["subject_name"]?.toString() ?: ""
                val className = (l["class"] as? Map<*, *>)?.get("name")?.toString()
                    ?: l["class_name"]?.toString() ?: ""
                val room = (l["classroom"] as? Map<*, *>)?.get("name")?.toString()
                    ?: l["classroom_name"]?.toString() ?: ""
                val startTime = slot?.get("start_time")?.toString() ?: l["start_time"]?.toString() ?: ""
                val endTime = slot?.get("end_time")?.toString() ?: l["end_time"]?.toString() ?: ""
                val lessonNum = (slot?.get("lesson_number") as? Number)?.toInt()
                    ?: (l["lesson_number"] as? Number)?.toInt() ?: (lessonsByDay[dayIdx]!!.size + 1)

                lessonsByDay[dayIdx]!!.add(
                    LessonUi(
                        id = l["id"]?.toString() ?: "",
                        period = lessonNum,
                        time = if (startTime.isNotEmpty() && endTime.isNotEmpty())
                            "$startTime–$endTime" else "",
                        subject = subject,
                        className = className,
                        teacher = "",
                        room = room
                    )
                )
            }

            (0..5).map { idx ->
                ScheduleDay(
                    dayIndex = idx,
                    dayName = dayNames[idx],
                    date = weekMonday.plusDays(idx.toLong()).format(fmt),
                    lessons = lessonsByDay[idx]!!.sortedBy { it.period }
                )
            }
        } catch (e: Exception) {
            (0..5).map { idx ->
                ScheduleDay(idx, dayNames[idx], weekMonday.plusDays(idx.toLong()).format(fmt), emptyList())
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseJournalOptions(data: Any?): Triple<List<JournalOption>, List<JournalOption>, List<JournalOption>> {
        if (data == null) return Triple(emptyList(), emptyList(), emptyList())
        return try {
            val map = data as? Map<*, *> ?: return Triple(emptyList(), emptyList(), emptyList())
            fun parseList(key: String) = (map[key] as? List<*>)?.mapNotNull { item ->
                val m = item as? Map<*, *> ?: return@mapNotNull null
                JournalOption(m["id"]?.toString() ?: "", m["name"]?.toString() ?: "")
            } ?: emptyList()
            Triple(parseList("classes"), parseList("subjects"), parseList("quarters"))
        } catch (e: Exception) { Triple(emptyList(), emptyList(), emptyList()) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseJournalData(data: Any?): List<JournalStudentRow> {
        if (data == null) return emptyList()
        return try {
            val map = data as? Map<*, *> ?: return emptyList()
            val students = (map["students"] as? List<*>) ?: return emptyList()
            students.mapNotNull { item ->
                val m = item as? Map<*, *> ?: return@mapNotNull null
                val name = m["name"]?.toString() ?: return@mapNotNull null
                val parts = name.trim().split(" ")
                val initials = parts.take(2).joinToString("") { it.firstOrNull()?.uppercaseChar()?.toString() ?: "" }
                val entries = (m["entries"] as? Map<*, *>)?.mapNotNull { (k, v) ->
                    val em = v as? Map<*, *> ?: return@mapNotNull null
                    k.toString() to JournalEntryUi(
                        grade = (em["grade"] as? Number)?.toInt(),
                        attendance = em["attendance"]?.toString() ?: "present",
                        homework = em["homework"]?.toString() ?: "-"
                    )
                }?.toMap() ?: emptyMap()
                JournalStudentRow(
                    studentId = m["id"]?.toString() ?: "",
                    studentName = name,
                    initials = initials,
                    entries = entries
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseAttendanceData(data: Any?): List<AttendanceRecord> {
        if (data == null) return emptyList()
        return try {
            val list = data as? List<*> ?: return emptyList()
            list.mapNotNull { item ->
                val m = item as? Map<*, *> ?: return@mapNotNull null
                AttendanceRecord(
                    studentId = m["student_id"]?.toString() ?: "",
                    studentName = m["student_name"]?.toString() ?: "",
                    date = m["date"]?.toString() ?: "",
                    status = m["status"]?.toString() ?: "present"
                )
            }
        } catch (e: Exception) { emptyList() }
    }
}