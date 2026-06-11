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
// UI modellari
// ─────────────────────────────────────────────

data class ChefDashboardUi(
    val todayMeals: Int = 0,
    val pendingConfirmations: Int = 0,
    val tomorrowMeals: Int = 0,
    val totalIngredients: Int = 0,
    val activeRecipes: Int = 0,
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
    val status: StockStatus,
    val imageUrl: String? = null
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
    val status: MealStatus,
    val plannedServings: Int = 0,
    val servingCount: Int = 0
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

    // State flows
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

    private val _weekLabel = MutableStateFlow("")
    val weekLabel: StateFlow<String> = _weekLabel

    private var weekOffset = 0

    // ─────────────────────────────────────────
    // Load funksiyalari
    // ─────────────────────────────────────────

    fun loadDashboard() {
        viewModelScope.launch {
            _dashboardState.value = ApiResult.Loading
            when (val r = repo.getDashboard()) {
                is ApiResult.Success -> {
                    val data = repo.jsonToAny(repo.extractData(r.data))
                    _dashboardState.value = ApiResult.Success(parseDashboard(data))
                }
                is ApiResult.Error -> { _dashboardState.value = ApiResult.Error(r.message); _errorMsg.value = r.message }
                else -> {}
            }
        }
    }

    fun loadIngredients(search: String? = null, category: String? = null) {
        viewModelScope.launch {
            _ingredientsState.value = ApiResult.Loading
            when (val r = repo.getIngredients(search = search, category = category)) {
                is ApiResult.Success -> {
                    val data = repo.jsonToAny(repo.extractData(r.data))
                    _ingredientsState.value = ApiResult.Success(parseIngredients(data))
                }
                is ApiResult.Error -> { _ingredientsState.value = ApiResult.Error(r.message); _errorMsg.value = r.message }
                else -> {}
            }
        }
    }

    fun createIngredient(request: IngredientRequest, initialStock: Double = 0.0) {
        viewModelScope.launch {
            _actionResult.value = ApiResult.Loading
            when (val r = repo.createIngredient(request)) {
                is ApiResult.Success -> {
                    _actionResult.value = ApiResult.Success(Unit)
                    // Boshlang'ich zaxira bo'lsa stock movement yuboramiz
                    if (initialStock > 0) {
                        val raw = repo.jsonToAny(repo.extractData(r.data))
                        val newId = (raw as? Map<*, *>)?.let {
                            (it["_id"] ?: it["id"])?.toString()?.takeIf { s -> s.isNotEmpty() }
                        }
                        if (newId != null) {
                            repo.addStockMovement(newId, StockMovementRequest(
                                movementType = "INCOMING",
                                quantity = initialStock,
                                reason = "Boshlang'ich zaxira"
                            ))
                        }
                    }
                    loadIngredients()
                }
                is ApiResult.Error -> { _actionResult.value = ApiResult.Error(r.message); _errorMsg.value = r.message }
                else -> {}
            }
        }
    }

    fun addStockMovement(id: String, quantity: Double, type: String = "INCOMING", reason: String = "Qo'lda kiritildi") {
        viewModelScope.launch {
            when (val r = repo.addStockMovement(id, StockMovementRequest(
                movementType = type, quantity = quantity, reason = reason
            ))) {
                is ApiResult.Success -> loadIngredients()
                is ApiResult.Error   -> _errorMsg.value = r.message
                else -> {}
            }
        }
    }

    fun uploadIngredientImage(id: String, file: java.io.File) {
        viewModelScope.launch {
            when (val r = repo.uploadIngredientImage(id, file)) {
                is ApiResult.Success -> loadIngredients()
                is ApiResult.Error   -> _errorMsg.value = r.message
                else -> {}
            }
        }
    }

    fun createRecipe(request: RecipeRequest) {
        viewModelScope.launch {
            _actionResult.value = ApiResult.Loading
            when (val r = repo.createRecipe(request)) {
                is ApiResult.Success -> { _actionResult.value = ApiResult.Success(Unit); loadRecipes() }
                is ApiResult.Error   -> { _actionResult.value = ApiResult.Error(r.message); _errorMsg.value = r.message }
                else -> {}
            }
        }
    }

    fun deleteRecipe(id: String) {
        viewModelScope.launch {
            when (val r = repo.deleteRecipe(id)) {
                is ApiResult.Success -> loadRecipes()
                is ApiResult.Error   -> _errorMsg.value = r.message
                else -> {}
            }
        }
    }

    fun updateIngredient(id: String, request: IngredientRequest) {
        viewModelScope.launch {
            _actionResult.value = ApiResult.Loading
            when (val r = repo.updateIngredient(id, request)) {
                is ApiResult.Success -> { _actionResult.value = ApiResult.Success(Unit); loadIngredients() }
                is ApiResult.Error -> { _actionResult.value = ApiResult.Error(r.message); _errorMsg.value = r.message }
                else -> {}
            }
        }
    }

    fun deleteIngredient(id: String) {
        viewModelScope.launch {
            when (val r = repo.deleteIngredient(id)) {
                is ApiResult.Success -> loadIngredients()
                is ApiResult.Error -> _errorMsg.value = r.message
                else -> {}
            }
        }
    }

    fun loadRecipes(search: String? = null, isActive: Boolean? = null) {
        viewModelScope.launch {
            _recipesState.value = ApiResult.Loading
            when (val r = repo.getRecipes(search = search, isActive = isActive)) {
                is ApiResult.Success -> {
                    val data = repo.jsonToAny(repo.extractData(r.data))
                    _recipesState.value = ApiResult.Success(parseRecipes(data))
                }
                is ApiResult.Error -> { _recipesState.value = ApiResult.Error(r.message); _errorMsg.value = r.message }
                else -> {}
            }
        }
    }

    fun loadMovements(movementType: String? = null) {
        viewModelScope.launch {
            _movementsState.value = ApiResult.Loading
            val today = LocalDate.now()
            val yearAgo = today.minusYears(1)
            // to = ertaga, shunda bugungi harakatlar ham kiradi
            val tomorrow = today.plusDays(1)
            when (val r = repo.getMovements(
                from = yearAgo.format(fmt),
                to = tomorrow.format(fmt),
                movementType = movementType
            )) {
                is ApiResult.Success -> {
                    val data = repo.jsonToAny(repo.extractData(r.data))
                    _movementsState.value = ApiResult.Success(parseMovements(data))
                }
                is ApiResult.Error -> { _movementsState.value = ApiResult.Error(r.message); _errorMsg.value = r.message }
                else -> {}
            }
        }
    }

    fun loadMenuCalendar() {
        viewModelScope.launch {
            _menuCalendarState.value = ApiResult.Loading
            val today = LocalDate.now()
            val thisMonday = today.with(DayOfWeek.MONDAY)
            val monday = thisMonday.plusWeeks(weekOffset.toLong())
            val sunday = monday.plusDays(6)
            val labelFmt = DateTimeFormatter.ofPattern("d-MMM")
            _weekLabel.value = "${monday.format(labelFmt)} – ${sunday.format(labelFmt)}"
            when (val r = repo.getMenuCalendar(from = monday.format(fmt), to = sunday.format(fmt))) {
                is ApiResult.Success -> {
                    val data = repo.jsonToAny(repo.extractData(r.data))
                    _menuCalendarState.value = ApiResult.Success(parseMenuCalendar(data, monday))
                }
                is ApiResult.Error -> { _menuCalendarState.value = ApiResult.Error(r.message); _errorMsg.value = r.message }
                else -> {}
            }
        }
    }

    fun nextWeek()    { weekOffset++; loadMenuCalendar() }
    fun prevWeek()    { weekOffset--; loadMenuCalendar() }
    fun goCurrentWeek() { weekOffset = 0; loadMenuCalendar() }

    fun createMenuEntry(recipeId: String, mealType: String, dateKey: String, plannedServings: Int? = null) {
        viewModelScope.launch {
            when (val r = repo.createMenuEntry(
                MenuEntryRequest(recipeId = recipeId, mealType = mealType, dateKey = dateKey, plannedServings = plannedServings)
            )) {
                is ApiResult.Success -> loadMenuCalendar()
                is ApiResult.Error -> _errorMsg.value = r.message
                else -> {}
            }
        }
    }

    fun deleteMenuEntry(id: String) {
        viewModelScope.launch {
            when (val r = repo.deleteMenuEntry(id)) {
                is ApiResult.Success -> loadMenuCalendar()
                is ApiResult.Error -> _errorMsg.value = r.message
                else -> {}
            }
        }
    }

    fun clearError() { _errorMsg.value = null }
    fun clearActionResult() { _actionResult.value = null }

    // ─────────────────────────────────────────
    // Yordamchi parse funksiyalari
    // ─────────────────────────────────────────

    private fun parseNumber(v: Any?): Double = when (v) {
        is Number -> v.toDouble()
        is String -> v.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }

    private fun parseStatus(apiStatus: String?): StockStatus = when (apiStatus?.uppercase()) {
        "OUT_OF_STOCK", "TUGAGAN" -> StockStatus.TUGAGAN
        "CRITICAL", "LOW", "KAM" -> StockStatus.KAM
        "HEALTHY", "OK", "YETARLI", "IN_STOCK", "AVAILABLE" -> StockStatus.YETARLI
        else -> StockStatus.YETARLI
    }

    private fun parseBool(v: Any?): Boolean = when (v) {
        is Boolean -> v
        is Number  -> v.toInt() != 0
        is String  -> v == "true" || v == "1"
        else       -> false
    }

    private fun normalizeUnit(unit: String): String = when (unit.lowercase().trim()) {
        "liter", "litre", "litres", "liters" -> "litr"
        "kilogram", "kilograms", "kilo"       -> "kg"
        "gram", "grams"                       -> "g"
        "milliliter", "milliliters",
        "millilitre", "millilitres"           -> "ml"
        "piece", "pieces", "pcs",
        "unit", "units"                       -> "dona"
        else -> unit
    }

    // ─────────────────────────────────────────
    // Parse funksiyalari
    // ─────────────────────────────────────────

    @Suppress("UNCHECKED_CAST")
    private fun parseDashboard(data: Any?): ChefDashboardUi {
        val m = data as? Map<*, *> ?: return ChefDashboardUi()
        return ChefDashboardUi(
            todayMeals          = (m["today_meals"] as? List<*>)?.size ?: parseNumber(m["today_meals_count"]).toInt(),
            pendingConfirmations = (m["pending_confirmations"] as? List<*>)?.size ?: parseNumber(m["pending_count"]).toInt(),
            tomorrowMeals       = (m["tomorrow_meals"] as? List<*>)?.size ?: parseNumber(m["tomorrow_meals_count"]).toInt(),
            totalIngredients    = parseNumber(m["total_ingredients"]).toInt(),
            activeRecipes       = parseNumber(m["active_recipes"]).toInt(),
            warehouseAlerts     = parseNumber(m["low_stock_count"] ?: m["warehouse_alerts"]).toInt()
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseIngredients(data: Any?): List<IngredientItem> {
        if (data == null) return emptyList()
        return try {
            val list: List<*> = when (data) {
                is List<*>  -> data
                is Map<*, *> -> (data["items"] as? List<*>)
                    ?: (data["data"] as? List<*>)
                    ?: (data["ingredients"] as? List<*>)
                    ?: (data["results"] as? List<*>)
                    ?: return emptyList()
                else -> return emptyList()
            }
            list.mapNotNull { item ->
                val m   = item as? Map<*, *> ?: return@mapNotNull null
                val id  = (m["_id"] ?: m["id"])?.toString() ?: return@mapNotNull null
                // Backend: current_stock (asosiy), quantity (fallback)
                val qty    = parseNumber(m["current_stock"] ?: m["quantity"] ?: m["stock"] ?: m["amount"])
                // Backend: minimum_stock (asosiy)
                val minQty = parseNumber(m["minimum_stock"] ?: m["min_quantity"] ?: m["minimum"])
                // Backend: stock_status (HEALTHY/LOW/CRITICAL/OUT_OF_STOCK)
                val status = parseStatus(m["stock_status"]?.toString())
                IngredientItem(
                    id          = id,
                    name        = m["name"]?.toString() ?: "",
                    category    = m["category"]?.toString() ?: "",
                    quantity    = qty,
                    unit        = normalizeUnit(m["unit"]?.toString() ?: "kg"),
                    minQuantity = minQty,
                    expiryDate  = m["expiration_date"]?.toString()?.substringBefore("T")
                        ?: m["expiry_date"]?.toString()?.substringBefore("T") ?: "",
                    status      = status,
                    imageUrl    = (m["image"] as? Map<*, *>)?.get("url")?.toString()
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseRecipes(data: Any?): List<Recipe> {
        if (data == null) return emptyList()
        return try {
            val list: List<*> = when (data) {
                is List<*>  -> data
                is Map<*, *> -> (data["items"] as? List<*>)
                    ?: (data["data"] as? List<*>)
                    ?: (data["recipes"] as? List<*>)
                    ?: (data["results"] as? List<*>)
                    ?: return emptyList()
                else -> return emptyList()
            }
            list.mapNotNull { item ->
                val m = item as? Map<*, *> ?: return@mapNotNull null
                val ingredients = m["ingredients"] as? List<*>
                Recipe(
                    id             = (m["_id"] ?: m["id"])?.toString() ?: "",
                    name           = m["name"]?.toString() ?: "",
                    category       = m["category"]?.toString() ?: "",
                    portionCount   = parseNumber(m["serving_count"] ?: m["portion_count"] ?: m["serving_size"]).toInt(),
                    isActive       = parseBool(m["is_active"] ?: m["active"] ?: m["status"]),
                    ingredientCount = ingredients?.size
                        ?: parseNumber(m["ingredient_count"] ?: m["ingredients_count"]).toInt()
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseMovements(data: Any?): List<StockMovement> {
        if (data == null) return emptyList()
        return try {
            val list: List<*> = when (data) {
                is List<*>  -> data
                is Map<*, *> -> (data["items"] as? List<*>)
                    ?: (data["data"] as? List<*>)
                    ?: (data["movements"] as? List<*>)
                    ?: (data["stock_movements"] as? List<*>)
                    ?: (data["records"] as? List<*>)
                    ?: (data["results"] as? List<*>)
                    ?: return emptyList()
                else -> return emptyList()
            }
            list.mapNotNull { item ->
                val m = item as? Map<*, *> ?: return@mapNotNull null
                val createdAt = m["created_at"]?.toString() ?: m["date"]?.toString() ?: m["timestamp"]?.toString() ?: ""
                val datePart  = createdAt.substringBefore("T").ifEmpty { createdAt.take(10) }
                val timePart  = if (createdAt.contains("T")) createdAt.substringAfter("T").take(5) else ""
                val ingMap    = m["ingredient"] as? Map<*, *>
                val ingName   = ingMap?.get("name")?.toString()
                    ?: m["ingredient_name"]?.toString()
                    ?: m["product_name"]?.toString()
                    ?: m["name"]?.toString() ?: ""
                val rawUnit   = ingMap?.get("unit")?.toString() ?: m["unit"]?.toString() ?: ""
                val typeStr   = m["movement_type"]?.toString() ?: m["type"]?.toString()
                ?: m["action"]?.toString() ?: m["direction"]?.toString() ?: ""
                val type = when (typeStr.uppercase()) {
                    "INCOMING", "IN", "KIRIM", "INCOME", "RECEIPT",
                    "ARRIVAL", "PURCHASE", "INBOUND"     -> MovementType.KIRIM
                    "OUTGOING", "OUT", "CHIQIM", "OUTCOME",
                    "CONSUMPTION", "USAGE", "EXPENSE",
                    "OUTBOUND", "DEDUCTION"              -> MovementType.CHIQIM
                    else                                 -> MovementType.TUZATISH
                }
                StockMovement(
                    date       = datePart,
                    time       = timePart,
                    ingredient = ingName,
                    unit       = normalizeUnit(rawUnit),
                    type       = type,
                    amount     = parseNumber(m["quantity"] ?: m["amount"]),
                    prevQty    = parseNumber(m["previous_stock"] ?: m["before_quantity"] ?: m["previous_quantity"]),
                    newQty     = parseNumber(m["new_stock"] ?: m["after_quantity"] ?: m["new_quantity"]),
                    reason     = m["reason"]?.toString() ?: m["note"]?.toString()
                    ?: m["description"]?.toString() ?: ""
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseMenuCalendar(data: Any?, weekMonday: LocalDate): List<MenuDayUi> {
        val dayNames = listOf("Dushanba", "Seshanba", "Chorshanba", "Payshanba", "Juma", "Shanba", "Yakshanba")
        val mealLabels = mapOf(
            "BREAKFAST" to "Nonushta",
            "breakfast" to "Nonushta",
            "LUNCH"     to "Tushlik",
            "lunch"     to "Tushlik",
            "DINNER"    to "Kechki ovqat",
            "dinner"    to "Kechki ovqat",
            "snack"     to "Kechki tamaddi"
        )
        val todayStr = LocalDate.now().format(fmt)
        return try {
            val map: Map<String, List<*>> = when (data) {
                is Map<*, *> -> data as Map<String, List<*>>
                is List<*>   -> {
                    val grouped = mutableMapOf<String, MutableList<Any?>>()
                    data.forEach { entry ->
                        val mm = entry as? Map<*, *> ?: return@forEach
                        val key = mm["date_key"]?.toString()
                            ?: mm["date"]?.toString()?.substringBefore("T")
                            ?: return@forEach
                        grouped.getOrPut(key) { mutableListOf() }.add(entry)
                    }
                    grouped
                }
                else -> emptyMap()
            }
            (0..6).map { idx ->
                val date    = weekMonday.plusDays(idx.toLong())
                val dateStr = date.format(fmt)
                val meals   = mutableListOf<MenuMealUi>()
                (map[dateStr])?.forEach { mealItem ->
                    val mm = mealItem as? Map<*, *> ?: return@forEach
                    val recipeSnap = mm["assigned_recipe_snapshot"] as? Map<*, *>
                    val recipe     = mm["recipe"] as? Map<*, *>
                    val mealType   = mm["meal_type"]?.toString() ?: ""
                    val statusStr  = mm["status"]?.toString()?.uppercase() ?: "ASSIGNED"
                    val mealStatus = when (statusStr) {
                        "CONFIRMED" -> MealStatus.CONFIRMED
                        else        -> MealStatus.ASSIGNED
                    }
                    meals.add(MenuMealUi(
                        id            = (mm["_id"] ?: mm["id"])?.toString() ?: "",
                        mealType      = mealType,
                        mealTypeLabel = mealLabels[mealType] ?: mealType,
                        recipeName    = recipeSnap?.get("name")?.toString()
                            ?: recipe?.get("name")?.toString()
                            ?: mm["recipe_name"]?.toString(),
                        status        = mealStatus,
                        plannedServings = parseNumber(mm["planned_servings"]).toInt(),
                        servingCount  = parseNumber(recipeSnap?.get("serving_count")).toInt()
                    ))
                }
                MenuDayUi(
                    dateKey  = dateStr,
                    dayLabel = "${dayNames[idx]} ${date.dayOfMonth}",
                    isToday  = dateStr == todayStr,
                    meals    = meals
                )
            }
        } catch (_: Exception) {
            (0..6).map { idx ->
                val date = weekMonday.plusDays(idx.toLong())
                MenuDayUi(
                    dateKey  = date.format(fmt),
                    dayLabel = "${dayNames[idx]} ${date.dayOfMonth}",
                    isToday  = date.format(fmt) == todayStr,
                    meals    = emptyList()
                )
            }
        }
    }
}