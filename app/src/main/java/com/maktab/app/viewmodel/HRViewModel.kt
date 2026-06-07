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

data class EmployeeUi(
    val id: String,
    val fullName: String,
    val initials: String,
    val position: String,
    val role: String,
    val phone: String,
    val status: String,
    val salaryType: String,
    val employmentType: String,
    val photoUrl: String?,
    val hasAccount: Boolean
)

data class PositionUi(
    val id: String,
    val name: String,
    val type: String,       // oqituvchi | mamuriyat | texnik
    val typeLabel: String
)

data class HRStatsUi(
    val totalCount: Int,
    val linkedCount: Int,
    val unlinkedCount: Int,
    val activeCount: Int
)

// ─────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────

class HRViewModel : ViewModel() {

    private val api = RetrofitClient.hrService

    private val _employeesState = MutableStateFlow<ApiResult<List<EmployeeUi>>>(ApiResult.Loading)
    val employeesState: StateFlow<ApiResult<List<EmployeeUi>>> = _employeesState

    private val _positionsState = MutableStateFlow<ApiResult<List<PositionUi>>>(ApiResult.Loading)
    val positionsState: StateFlow<ApiResult<List<PositionUi>>> = _positionsState

    private val _statsState = MutableStateFlow<HRStatsUi?>(null)
    val statsState: StateFlow<HRStatsUi?> = _statsState

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg

    // Pagination
    private var currentPage = 1
    private var totalPages  = 1
    private val allEmployees = mutableListOf<EmployeeUi>()

    // ─────────────────────────────────────────
    // Xodimlar — GET /api/users/employees
    // ─────────────────────────────────────────
    fun loadEmployees(search: String? = null, reset: Boolean = true) {
        if (reset) { currentPage = 1; allEmployees.clear() }
        viewModelScope.launch {
            if (reset) _employeesState.value = ApiResult.Loading
            try {
                val response = api.getEmployees(
                    search   = search?.ifBlank { null },
                    page     = currentPage,
                    perPage  = 30
                )
                if (response.isSuccessful) {
                    val body = response.body()?.result?.data
                    @Suppress("UNCHECKED_CAST")
                    val map  = body as? Map<*, *>
                    val list = map?.get("data") as? List<*>
                        ?: body as? List<*>
                        ?: emptyList<Any>()

                    // Pagination
                    val pagination = map?.get("pagination") as? Map<*, *>
                    totalPages = parseNum(pagination?.get("pages")).toInt().coerceAtLeast(1)

                    val employees = list.mapNotNull { parseEmployee(it) }
                    allEmployees.addAll(employees)
                    _employeesState.value = ApiResult.Success(allEmployees.toList())
                } else {
                    _employeesState.value = ApiResult.Error("Xato ${response.code()}")
                }
            } catch (e: Exception) {
                _employeesState.value = ApiResult.Error(e.message ?: "Ulanish xatosi")
            }
        }
    }

    fun loadNextPage(search: String? = null) {
        if (currentPage >= totalPages) return
        currentPage++
        loadEmployees(search, reset = false)
    }

    // ─────────────────────────────────────────
    // Statistika — GET /api/users/employees/stats
    // ─────────────────────────────────────────
    fun loadStats() {
        viewModelScope.launch {
            try {
                val response = api.getEmployeeStats()
                if (response.isSuccessful) {
                    @Suppress("UNCHECKED_CAST")
                    val data = response.body()?.result?.data as? Map<*, *>
                    if (data != null) {
                        _statsState.value = HRStatsUi(
                            totalCount    = parseNum(data["total_count"]).toInt(),
                            linkedCount   = parseNum(data["linked_user_count"]).toInt(),
                            unlinkedCount = parseNum(data["unlinked_user_count"]).toInt(),
                            activeCount   = parseNum(data["active_user_count"]).toInt()
                        )
                    }
                }
            } catch (_: Exception) {}
        }
    }

    // ─────────────────────────────────────────
    // Lavozimlar — GET /api/positions
    // ─────────────────────────────────────────
    fun loadPositions() {
        viewModelScope.launch {
            _positionsState.value = ApiResult.Loading
            try {
                val allPositions = mutableListOf<PositionUi>()
                var page = 1
                var pages = 1

                // Barcha sahifalarni yuklaymiz
                while (page <= pages) {
                    val response = api.getPositions(page = page, perPage = 50)
                    if (response.isSuccessful) {
                        val body = response.body()?.result?.data
                        @Suppress("UNCHECKED_CAST")
                        val map  = body as? Map<*, *>
                        val list = map?.get("data") as? List<*>
                            ?: body as? List<*>
                            ?: emptyList<Any>()

                        val pagination = map?.get("pagination") as? Map<*, *>
                        pages = parseNum(pagination?.get("pages")).toInt().coerceAtLeast(1)

                        list.mapNotNull { parsePosition(it) }.let { allPositions.addAll(it) }
                        page++
                    } else break
                }
                _positionsState.value = ApiResult.Success(allPositions)
            } catch (e: Exception) {
                _positionsState.value = ApiResult.Error(e.message ?: "Ulanish xatosi")
            }
        }
    }

    fun refresh() {
        loadEmployees(reset = true)
        loadStats()
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

    private fun initials(name: String): String =
        name.trim().split(" ").filter { it.isNotEmpty() }.take(2)
            .joinToString("") { it.firstOrNull()?.uppercase() ?: "" }

    @Suppress("UNCHECKED_CAST")
    private fun parseEmployee(item: Any?): EmployeeUi? {
        val m = item as? Map<*, *> ?: return null
        val id = (m["_id"] ?: m["id"])?.toString() ?: return null
        val fullName = m["full_name"]?.toString() ?: m["fullname"]?.toString() ?: ""

        val userMap   = m["user"] as? Map<*, *>
        val photoMap  = m["photo"] as? Map<*, *>

        val salaryLabel = when (m["salary_type"]?.toString()) {
            "fixed"    -> "Belgilangan"
            "hourly"   -> "Soatbay"
            "percent"  -> "Foizli"
            else       -> m["salary_type"]?.toString() ?: ""
        }
        val empLabel = when (m["employment_type"]?.toString()) {
            "full_time"  -> "To'liq"
            "part_time"  -> "Yarim"
            "contract"   -> "Shartnoma"
            else         -> m["employment_type"]?.toString() ?: ""
        }

        return EmployeeUi(
            id             = id,
            fullName       = fullName,
            initials       = initials(fullName),
            position       = m["position"]?.toString() ?: "Lavozim belgilanmagan",
            role           = userMap?.get("role")?.toString() ?: "",
            phone          = m["phone"]?.toString() ?: "",
            status         = userMap?.get("status")?.toString() ?: "active",
            salaryType     = salaryLabel,
            employmentType = empLabel,
            photoUrl       = photoMap?.get("url")?.toString(),
            hasAccount     = (userMap?.get("id")?.toString()?.isNotEmpty() == true) &&
                    (userMap["id"]?.toString() != "null")
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun parsePosition(item: Any?): PositionUi? {
        val m = item as? Map<*, *> ?: return null
        val id   = (m["_id"] ?: m["id"])?.toString() ?: return null
        val name = m["name"]?.toString() ?: return null
        val type = m["type"]?.toString() ?: ""
        val typeLabel = when (type) {
            "oqituvchi" -> "O'qituvchi"
            "mamuriyat" -> "Ma'muriyat"
            "texnik"    -> "Texnik xodim"
            else        -> type
        }
        return PositionUi(id = id, name = name, type = type, typeLabel = typeLabel)
    }
}