package com.maktab.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maktab.app.network.ApiResult
import com.maktab.app.network.models.*
import com.maktab.app.network.repositories.ChefRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ─────────────────────────────────────────────
// UI State modellari
// ─────────────────────────────────────────────

data class ChefDashboardUi(
    val todayMeals: Int = 0,
    val pendingConfirmations: Int = 0,
    val tomorrowMeals: Int = 0,
    val warehouseAlerts: Int = 0
)

enum class StockStatus { YETARLI, KAM, TUGAGAN }

data class IngredientItem(
    val id: String,
    val name: String,
    val category: String,
    val quantity: Double,
    val unit: String,
    val minQuantity: Double,
    val expiryDate: String,
    val status: StockStatus
)

enum class MovementType { KIRIM, CHIQIM, TUZATISH }

data class StockMovement(
    val date: String,
    val time: String,
    val ingredient: String,
    val unit: String,
    val type: MovementType,
    val amount: Double,
    val prevQty: Double,
    val newQty: Double,
    val reason: String
)

data class Recipe(
    val id: String,
    val name: String,
    val category: String,
    val portionCount: Int,
    val isActive: Boolean,
    val ingredientCount: Int
)

enum class MealStatus { EMPTY, ASSIGNED, CONFIRMED }

data class MenuMealUi(
    val id: String,
    val mealType: String,
    val mealTypeLabel: String,
    val recipeName: String?,
    val status: MealStatus
)

data class MenuDayUi(
    val dateKey: String,
    val dayLabel: String,
    val isToday: Boolean,
    val meals: List<MenuMealUi>
)

// ─────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────

class ChefViewModel : ViewModel() {

    private val repo = ChefRepository()
    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val _dashboardState = MutableStateFlow<ApiResult<ChefDashboardUi>>(ApiResult.Loading)
    val dashboardState: StateFlow<ApiResult<ChefDashboardUi>> = _dashboardState

    private val _ingredientsState = MutableStateFlow<ApiResult<List<IngredientItem>>>(ApiResult.Loading)
    val ingredientsState: StateFlow<ApiResult<List<IngredientItem>>> = _ingredientsState

    private val _recipesState = MutableStateFlow<ApiResult<List<Recipe>>>(ApiResult.Loading)
    val recipesState: StateFlow<ApiResult<List<Recipe>>> = _recipesState

    private val _movementsState = MutableStateFlow<ApiResult<List<StockMovement>>>(ApiResult.Loading)
    val movementsState: StateFlow<ApiResult<List<StockMovement>>> = _movementsState

    private val _menuCalendarState = MutableStateFlow<ApiResult<List<MenuDayUi>>>(ApiResult.Loading)
    val menuCalendarState: StateFlow<ApiResult<List<MenuDayUi>>> = _menuCalendarState

    private val _actionResult = MutableStateFlow<ApiResult<Unit>?>(null)
    val actionResult: StateFlow<ApiResult<Unit>?> = _actionResult

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg

    // ─────────────────────────────────────────
    // Load funksiyalari
    // ─────────────────────────────────────────

    fun loadDashboard() {
        viewModelScope.launch {
            _dashboardState.value = ApiResult.Loading
            when (val r = repo.getDashboard()) {
                is ApiResult.Success -> _dashboardState.value = ApiResult.Success(parseDashboard(r.data.result?.data))
                is ApiResult.Error   -> { _dashboardState.value = ApiResult.Error(r.message); _errorMsg.value = r.message }
                else -> {}
            }
        }
    }

    fun loadIngredients(search: String? = null, category: String? = null) {
        viewModelScope.launch {
            _ingredientsState.value = ApiResult.Loading
            when (val r = repo.getIngredients(search = search, category = category)) {
                is ApiResult.Success -> _ingredientsState.value = ApiResult.Success(parseIngredients(r.data.result?.data))
                is ApiResult.Error   -> { _ingredientsState.value = ApiResult.Error(r.message); _errorMsg.value = r.message }
                else -> {}
            }
        }
    }

    fun updateIngredient(id: String, request: IngredientRequest) {
        viewModelScope.launch {
            _actionResult.value = ApiResult.Loading
            when (val r = repo.updateIngredient(id, request)) {
                is ApiResult.Success -> { _actionResult.value = ApiResult.Success(Unit); loadIngredients() }
                is ApiResult.Error   -> { _actionResult.value = ApiResult.Error(r.message); _errorMsg.value = r.message }
                else -> {}
            }
        }
    }

    fun createIngredient(request: IngredientRequest) {
        viewModelScope.launch {
            _actionResult.value = ApiResult.Loading
            when (val r = repo.createIngredient(request)) {
                is ApiResult.Success -> { _actionResult.value = ApiResult.Success(Unit); loadIngredients() }
                is ApiResult.Error   -> { _actionResult.value = ApiResult.Error(r.message); _errorMsg.value = r.message }
                else -> {}
            }
        }
    }

    fun loadRecipes(search: String? = null, isActive: Boolean? = null) {
        viewModelScope.launch {
            _recipesState.value = ApiResult.Loading
            when (val r = repo.getRecipes(search = search, isActive = isActive)) {
                is ApiResult.Success -> _recipesState.value = ApiResult.Success(parseRecipes(r.data.result?.data))
                is ApiResult.Error   -> { _recipesState.value = ApiResult.Error(r.message); _errorMsg.value = r.message }
                else -> {}
            }
        }
    }

    fun loadMovements(movementType: String? = null) {
        viewModelScope.launch {
            _movementsState.value = ApiResult.Loading
            val today = LocalDate.now()
            val monthAgo = today.minusMonths(1)
            when (val r = repo.getMovements(
                from = monthAgo.format(fmt),
                to = today.format(fmt),
                movementType = movementType
            )) {
                is ApiResult.Success -> _movementsState.value = ApiResult.Success(parseMovements(r.data.result?.data))
                is ApiResult.Error   -> { _movementsState.value = ApiResult.Error(r.message); _errorMsg.value = r.message }
                else -> {}
            }
        }
    }

    fun loadMenuCalendar() {
        viewModelScope.launch {
            _menuCalendarState.value = ApiResult.Loading
            val today = LocalDate.now()
            val monday = today.with(DayOfWeek.MONDAY)
            val sunday = monday.plusDays(6)
            when (val r = repo.getMenuCalendar(from = monday.format(fmt), to = sunday.format(fmt))) {
                is ApiResult.Success -> _menuCalendarState.value = ApiResult.Success(parseMenuCalendar(r.data.result?.data, monday))
                is ApiResult.Error   -> { _menuCalendarState.value = ApiResult.Error(r.message); _errorMsg.value = r.message }
                else -> {}
            }
        }
    }

    fun clearError() { _errorMsg.value = null }
    fun clearActionResult() { _actionResult.value = null }

    // ─────────────────────────────────────────
    // Parse funksiyalari
    // ─────────────────────────────────────────

    @Suppress("UNCHECKED_CAST")
    private fun parseDashboard(data: Any?): ChefDashboardUi {
        return try {
            val m = data as? Map<*, *> ?: return ChefDashboardUi()
            ChefDashboardUi(
                todayMeals = (m["today_meals_count"] as? Number)?.toInt()
                    ?: (m["today_meals"] as? Number)?.toInt() ?: 0,
                pendingConfirmations = (m["pending_count"] as? Number)?.toInt()
                    ?: (m["pending_confirmations"] as? Number)?.toInt() ?: 0,
                tomorrowMeals = (m["tomorrow_meals_count"] as? Number)?.toInt()
                    ?: (m["tomorrow_meals"] as? Number)?.toInt() ?: 0,
                warehouseAlerts = (m["low_stock_count"] as? Number)?.toInt()
                    ?: (m["warehouse_alerts"] as? Number)?.toInt() ?: 0
            )
        } catch (e: Exception) { ChefDashboardUi() }
    }

    private fun parseNumber(v: Any?): Double = when (v) {
        is Number -> v.toDouble()
        is String -> v.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }

    private fun normalizeUnit(unit: String): String = when (unit.lowercase().trim()) {
        "liter", "litre", "litres", "liters" -> "litr"
        "kilogram", "kilograms", "kilo" -> "kg"
        "gram", "grams" -> "g"
        "milliliter", "milliliters", "millilitre", "millilitres" -> "ml"
        "piece", "pieces", "pcs", "unit", "units" -> "dona"
        else -> unit
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseIngredients(data: Any?): List<IngredientItem> {
        if (data == null) return emptyList()
        return try {
            val list: List<*> = when (data) {
                is List<*> -> data
                is Map<*, *> -> (data["items"] as? List<*>)
                    ?: (data["data"] as? List<*>)
                    ?: (data["ingredients"] as? List<*>)
                    ?: return emptyList()
                else -> return emptyList()
            }
            list.mapNotNull { item ->
                val m = item as? Map<*, *> ?: return@mapNotNull null
                val qty = parseNumber(m["quantity"] ?: m["current_quantity"] ?: m["stock"])
                val minQty = parseNumber(m["min_quantity"] ?: m["minimum_quantity"] ?: m["min_stock"])
                val apiStatus = m["status"]?.toString()?.lowercase()
                val status = when {
                    apiStatus == "out_of_stock" || apiStatus == "tugagan" -> StockStatus.TUGAGAN
                    apiStatus == "low" || apiStatus == "low_stock" || apiStatus == "kam" -> StockStatus.KAM
                    apiStatus == "ok" || apiStatus == "available" || apiStatus == "yetarli" -> StockStatus.YETARLI
                    qty <= 0 -> StockStatus.TUGAGAN
                    minQty > 0 && qty < minQty -> StockStatus.KAM
                    else -> StockStatus.YETARLI
                }
                IngredientItem(
                    id = m["id"]?.toString() ?: "",
                    name = m["name"]?.toString() ?: "",
                    category = m["category"]?.toString() ?: "",
                    quantity = qty,
                    unit = normalizeUnit(m["unit"]?.toString() ?: "kg"),
                    minQuantity = minQty,
                    expiryDate = m["expiry_date"]?.toString()?.substringBefore("T") ?: "",
                    status = status
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseRecipes(data: Any?): List<Recipe> {
        if (data == null) return emptyList()
        return try {
            val list: List<*> = when (data) {
                is List<*> -> data
                is Map<*, *> -> (data["items"] as? List<*>) ?: (data["data"] as? List<*>) ?: return emptyList()
                else -> return emptyList()
            }
            list.mapNotNull { item ->
                val m = item as? Map<*, *> ?: return@mapNotNull null
                val ingredients = m["ingredients"] as? List<*>
                Recipe(
                    id = m["id"]?.toString() ?: "",
                    name = m["name"]?.toString() ?: "",
                    category = m["category"]?.toString() ?: "",
                    portionCount = (m["portion_count"] as? Number)?.toInt() ?: 0,
                    isActive = m["is_active"] as? Boolean ?: true,
                    ingredientCount = ingredients?.size ?: (m["ingredient_count"] as? Number)?.toInt() ?: 0
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseMovements(data: Any?): List<StockMovement> {
        if (data == null) return emptyList()
        return try {
            val list: List<*> = when (data) {
                is List<*> -> data
                is Map<*, *> -> (data["items"] as? List<*>)
                    ?: (data["data"] as? List<*>)
                    ?: (data["movements"] as? List<*>)
                    ?: (data["stock_movements"] as? List<*>)
                    ?: (data["records"] as? List<*>)
                    ?: return emptyList()
                else -> return emptyList()
            }
            list.mapNotNull { item ->
                val m = item as? Map<*, *> ?: return@mapNotNull null
                val createdAt = m["created_at"]?.toString() ?: ""
                val datePart = createdAt.substringBefore("T")
                val timePart = createdAt.substringAfter("T", "").take(5)
                val ingMap = m["ingredient"] as? Map<*, *>
                val ingName = ingMap?.get("name")?.toString()
                    ?: m["ingredient_name"]?.toString()
                    ?: m["product_name"]?.toString() ?: ""
                val rawUnit = ingMap?.get("unit")?.toString() ?: m["unit"]?.toString() ?: ""
                val unit = normalizeUnit(rawUnit)
                val typeStr = m["movement_type"]?.toString() ?: m["type"]?.toString()
                    ?: m["action"]?.toString() ?: ""
                val type = when (typeStr.lowercase()) {
                    "in", "kirim", "income", "receipt" -> MovementType.KIRIM
                    "out", "chiqim", "outcome", "consumption" -> MovementType.CHIQIM
                    else -> MovementType.TUZATISH
                }
                StockMovement(
                    date = datePart,
                    time = timePart,
                    ingredient = ingName,
                    unit = unit,
                    type = type,
                    amount = parseNumber(m["quantity"] ?: m["amount"] ?: m["changed_quantity"]),
                    prevQty = parseNumber(m["before_quantity"] ?: m["previous_quantity"] ?: m["old_quantity"]),
                    newQty = parseNumber(m["after_quantity"] ?: m["new_quantity"] ?: m["current_quantity"]),
                    reason = m["reason"]?.toString() ?: m["note"]?.toString() ?: m["description"]?.toString() ?: ""
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseMenuCalendar(data: Any?, weekMonday: LocalDate): List<MenuDayUi> {
        val dayNames = listOf("Dushanba", "Seshanba", "Chorshanba", "Payshanba", "Juma", "Shanba", "Yakshanba")
        val mealTypeLabels = mapOf(
            "breakfast" to "Nonushta",
            "lunch" to "Tushlik",
            "dinner" to "Kechki ovqat",
            "snack" to "Kechki tamaddi"
        )
        val todayStr = LocalDate.now().format(fmt)
        return try {
            // API may return a Map keyed by date OR a flat List of meal entries
            val map: Map<String, List<*>> = when (data) {
                is Map<*, *> -> @Suppress("UNCHECKED_CAST") (data as Map<String, List<*>>)
                is List<*> -> {
                    // Flat list: group entries by date key
                    val grouped = mutableMapOf<String, MutableList<Any?>>()
                    data.forEach { entry ->
                        val m = entry as? Map<*, *> ?: return@forEach
                        val key = m["date_key"]?.toString()
                            ?: m["date"]?.toString()?.substringBefore("T")
                            ?: return@forEach
                        grouped.getOrPut(key) { mutableListOf() }.add(entry)
                    }
                    grouped
                }
                else -> emptyMap()
            }
            (0..6).map { idx ->
                val date = weekMonday.plusDays(idx.toLong())
                val dateStr = date.format(fmt)
                val dayMeals = mutableListOf<MenuMealUi>()
                (map[dateStr])?.forEach { mealItem ->
                    val mm = mealItem as? Map<*, *> ?: return@forEach
                    val recipe = mm["recipe"] as? Map<*, *>
                    val mealType = mm["meal_type"]?.toString() ?: ""
                    val statusStr = mm["status"]?.toString() ?: ""
                    val mealStatus = when (statusStr.lowercase()) {
                        "confirmed" -> MealStatus.CONFIRMED
                        "assigned", "planned" -> MealStatus.ASSIGNED
                        else -> MealStatus.EMPTY
                    }
                    dayMeals.add(MenuMealUi(
                        id = mm["id"]?.toString() ?: "",
                        mealType = mealType,
                        mealTypeLabel = mealTypeLabels[mealType] ?: mealType,
                        recipeName = recipe?.get("name")?.toString(),
                        status = mealStatus
                    ))
                }
                MenuDayUi(
                    dateKey = dateStr,
                    dayLabel = "${dayNames[idx]} ${date.dayOfMonth}",
                    isToday = dateStr == todayStr,
                    meals = dayMeals
                )
            }
        } catch (e: Exception) {
            (0..6).map { idx ->
                val date = weekMonday.plusDays(idx.toLong())
                MenuDayUi(
                    dateKey = date.format(fmt),
                    dayLabel = "${dayNames[idx]} ${date.dayOfMonth}",
                    isToday = date.format(fmt) == todayStr,
                    meals = emptyList()
                )
            }
        }
    }
}
