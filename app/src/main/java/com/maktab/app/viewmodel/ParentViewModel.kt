package com.maktab.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maktab.app.network.ApiResult
import com.maktab.app.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────
// UI modellari
// ─────────────────────────────────────────────

data class ParentProfileUi(
    val id: String,
    val fullname: String,
    val username: String,
    val phone: String,
    val childrenCount: Int
)

data class AttendanceUi(
    val total: Int,
    val present: Int,
    val absent: Int,
    val late: Int,
    val excused: Int,
    val rate: Double?
)

data class GradesUi(
    val completedCount: Int,
    val pendingCount: Int,
    val avgPercent: Double?,
    val recent: List<GradeEntryUi>
)

data class GradeEntryUi(
    val subject: String,
    val grade: Double?,
    val maxGrade: Double?,
    val date: String
)

data class HomeworkUi(
    val total: Int,
    val submitted: Int,
    val pending: Int,
    val late: Int,
    val avgPercent: Double?,
    val recent: List<HomeworkEntryUi>
)

data class HomeworkEntryUi(
    val title: String,
    val subject: String,
    val dueDate: String,
    val status: String,
    val grade: Double?
)

data class PaymentsUi(
    val contractsCount: Int,
    val totalAmount: Double,
    val nextPaymentDate: String?,
    val activeContract: String?
)

data class ExamsUi(
    val attemptsCount: Int,
    val gradedCount: Int,
    val avgPercent: Double?,
    val recent: List<ExamEntryUi>
)

data class ExamEntryUi(
    val name: String,
    val subject: String,
    val date: String,
    val score: Double?,
    val maxScore: Double?,
    val status: String
)

data class ParentChildDetailUi(
    val id: String,
    val fullname: String,
    val initials: String,
    val age: Int,
    val gender: String,
    val className: String,
    val status: String,
    val isActive: Boolean,
    val attendance: AttendanceUi,
    val grades: GradesUi,
    val homework: HomeworkUi,
    val payments: PaymentsUi,
    val exams: ExamsUi
)

// ─────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────

class ParentViewModel : ViewModel() {

    private val api = RetrofitClient.parentService

    // Profile
    private val _profile = MutableStateFlow<ParentProfileUi?>(null)
    val profile: StateFlow<ParentProfileUi?> = _profile

    // Farzand to'liq ma'lumoti
    private val _childState = MutableStateFlow<ApiResult<ParentChildDetailUi>>(ApiResult.Loading)
    val childState: StateFlow<ApiResult<ParentChildDetailUi>> = _childState

    // Xato xabarlari
    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg

    // Yuklangan farzand ID si
    private var loadedChildId: String? = null

    // ─────────────────────────────────────────
    // Dashboard — profil + farzand ID larini olish
    // Keyin birinchi farzandning to'liq ma'lumotini yuklash
    // ─────────────────────────────────────────
    fun loadDashboard() {
        viewModelScope.launch {
            _childState.value = ApiResult.Loading
            try {
                val response = api.getDashboard()
                if (response.isSuccessful) {
                    val map = response.body()?.result?.data as? Map<*, *>

                    // Profil
                    (map?.get("parent") as? Map<*, *>)?.let { p ->
                        _profile.value = ParentProfileUi(
                            id            = p["id"]?.toString() ?: "",
                            fullname      = p["fullname"]?.toString() ?: "",
                            username      = p["username"]?.toString() ?: "",
                            phone         = p["phone"]?.toString() ?: "",
                            childrenCount = parseNum(p["children_count"]).toInt()
                        )
                    }

                    // Birinchi farzand ID sini olib to'liq ma'lumot yuklash
                    val children = map?.get("children") as? List<*>
                    val firstChildId = (children?.firstOrNull() as? Map<*, *>)?.get("id")?.toString()

                    if (firstChildId != null) {
                        loadChild(firstChildId)
                    } else {
                        _childState.value = ApiResult.Error("Farzand ma'lumoti topilmadi")
                    }
                } else {
                    _childState.value = ApiResult.Error("Xato ${response.code()}")
                }
            } catch (e: Exception) {
                _childState.value = ApiResult.Error(e.message ?: "Ulanish xatosi")
            }
        }
    }

    // ─────────────────────────────────────────
    // Farzand to'liq ma'lumoti — GET /api/parent/children/{id}
    // ─────────────────────────────────────────
    fun loadChild(childId: String) {
        if (loadedChildId == childId && _childState.value is ApiResult.Success) return
        viewModelScope.launch {
            _childState.value = ApiResult.Loading
            try {
                val response = api.getChild(childId)
                if (response.isSuccessful) {
                    val map  = response.body()?.result?.data as? Map<*, *>
                    val child = map?.get("child") as? Map<*, *>
                        ?: map?.get("data") as? Map<*, *>
                        ?: map

                    if (child != null) {
                        loadedChildId = childId
                        _childState.value = ApiResult.Success(parseChildDetail(child))
                    } else {
                        _childState.value = ApiResult.Error("Ma'lumot formati noto'g'ri")
                    }
                } else {
                    _childState.value = ApiResult.Error("Xato ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                _childState.value = ApiResult.Error(e.message ?: "Ulanish xatosi")
            }
        }
    }

    fun refresh() {
        loadedChildId = null
        loadDashboard()
    }

    fun clearError() { _errorMsg.value = null }

    // ─────────────────────────────────────────
    // Parse yordamchilari
    // ─────────────────────────────────────────

    private fun parseNum(v: Any?): Double = when (v) {
        is Number -> v.toDouble()
        is String -> v.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }

    private fun parseNullableDouble(v: Any?): Double? = when (v) {
        null, is Unit -> null
        is Number -> v.toDouble().takeIf { it > 0 }
        is String -> v.toDoubleOrNull()?.takeIf { it > 0 }
        else -> null
    }

    private fun initials(name: String): String =
        name.trim().split(" ").take(2)
            .joinToString("") { it.firstOrNull()?.uppercase() ?: "" }

    @Suppress("UNCHECKED_CAST")
    private fun parseChildDetail(m: Map<*, *>): ParentChildDetailUi {
        val genderMap  = m["gender"] as? Map<*, *>
        val classMap   = m["class"]  as? Map<*, *>
        val statusMap  = m["status"] as? Map<*, *>
        val fullname   = m["fullname"]?.toString() ?: ""

        // ── Davomat ──────────────────────────
        val attMap    = m["attendance"] as? Map<*, *>
        val schoolAtt = attMap?.get("school") as? Map<*, *>
        val lessAtt   = attMap?.get("lessons") as? Map<*, *>
        val att = schoolAtt ?: lessAtt
        val attendance = AttendanceUi(
            total   = parseNum(att?.get("total")).toInt(),
            present = parseNum(att?.get("present")).toInt(),
            absent  = parseNum(att?.get("absent")).toInt(),
            late    = parseNum(att?.get("late")).toInt(),
            excused = parseNum(att?.get("excused")).toInt(),
            rate    = parseNullableDouble(att?.get("rate"))
        )

        // ── Baholar ──────────────────────────
        val grMap = m["grades"] as? Map<*, *>
        val grRecent = (grMap?.get("recent") as? List<*>)?.mapNotNull { item ->
            val g = item as? Map<*, *> ?: return@mapNotNull null
            GradeEntryUi(
                subject  = (g["subject"] as? Map<*, *>)?.get("name")?.toString()
                    ?: g["subject_name"]?.toString() ?: "",
                grade    = parseNullableDouble(g["score"] ?: g["grade"] ?: g["percentage"]),
                maxGrade = parseNullableDouble(g["max_score"] ?: g["max_grade"]),
                date     = g["date"]?.toString()?.substringBefore("T") ?: ""
            )
        } ?: emptyList()
        val grades = GradesUi(
            completedCount = parseNum(grMap?.get("completed_count")).toInt(),
            pendingCount   = parseNum(grMap?.get("pending_count")).toInt(),
            avgPercent     = parseNullableDouble(grMap?.get("average_percentage")),
            recent         = grRecent
        )

        // ── Uyga vazifalar ───────────────────
        val hwMap = m["homework"] as? Map<*, *>
        val hwRecent = (hwMap?.get("recent") as? List<*>)?.mapNotNull { item ->
            val h = item as? Map<*, *> ?: return@mapNotNull null
            HomeworkEntryUi(
                title   = h["title"]?.toString() ?: "",
                subject = (h["subject"] as? Map<*, *>)?.get("name")?.toString()
                    ?: h["subject_name"]?.toString() ?: "",
                dueDate = h["due_date"]?.toString()?.substringBefore("T") ?: "",
                status  = h["status"]?.toString() ?: "",
                grade   = parseNullableDouble(h["grade"] ?: h["score"])
            )
        } ?: emptyList()
        val homework = HomeworkUi(
            total      = parseNum(hwMap?.get("total_assignments")).toInt(),
            submitted  = parseNum(hwMap?.get("submitted_count")).toInt(),
            pending    = parseNum(hwMap?.get("pending_count")).toInt(),
            late       = parseNum(hwMap?.get("late_count")).toInt(),
            avgPercent = parseNullableDouble(hwMap?.get("average_percentage")),
            recent     = hwRecent
        )

        // ── To'lovlar ────────────────────────
        val payMap = m["payments"] as? Map<*, *>
        val activeContract = (payMap?.get("active_contract") as? Map<*, *>)
            ?.get("name")?.toString()
        val payments = PaymentsUi(
            contractsCount  = parseNum(payMap?.get("contracts_count")).toInt(),
            totalAmount     = parseNum(payMap?.get("total_amount")),
            nextPaymentDate = payMap?.get("next_payment_date")?.toString()
                ?.substringBefore("T")?.ifEmpty { null },
            activeContract  = activeContract
        )

        // ── Imtihonlar ───────────────────────
        val exMap = m["exams"] as? Map<*, *>
        val exRecent = (exMap?.get("recent") as? List<*>)?.mapNotNull { item ->
            val e = item as? Map<*, *> ?: return@mapNotNull null
            ExamEntryUi(
                name     = e["name"]?.toString() ?: e["title"]?.toString() ?: "",
                subject  = (e["subject"] as? Map<*, *>)?.get("name")?.toString()
                    ?: e["subject_name"]?.toString() ?: "",
                date     = e["date"]?.toString()?.substringBefore("T") ?: "",
                score    = parseNullableDouble(e["score"] ?: e["percentage"]),
                maxScore = parseNullableDouble(e["max_score"]),
                status   = e["status"]?.toString() ?: ""
            )
        } ?: emptyList()
        val exams = ExamsUi(
            attemptsCount = parseNum(exMap?.get("attempts_count")).toInt(),
            gradedCount   = parseNum(exMap?.get("graded_count")).toInt(),
            avgPercent    = parseNullableDouble(exMap?.get("average_percentage")),
            recent        = exRecent
        )

        return ParentChildDetailUi(
            id         = m["id"]?.toString() ?: "",
            fullname   = fullname,
            initials   = initials(fullname),
            age        = parseNum(m["age"]).toInt(),
            gender     = genderMap?.get("name")?.toString() ?: "",
            className  = classMap?.get("name")?.toString() ?: "Sinf biriktirilmagan",
            status     = statusMap?.get("name")?.toString() ?: "",
            isActive   = m["is_active"] as? Boolean ?: true,
            attendance = attendance,
            grades     = grades,
            homework   = homework,
            payments   = payments,
            exams      = exams
        )
    }
}