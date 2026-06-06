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
    val warehouseAlerts: Int = 0,
    val activeRecipes: Int = 0,
    val totalIngredients: Int = 0
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
                is ApiResult.Error -> {
                    _dashboardState.value = ApiResult.Error(r.message)
                    _errorMsg.value = r.message
                }
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
                is ApiResult.Error -> {
                    _ingredientsState.value = ApiResult.Error(r.message)
                    _errorMsg.value = r.message
                }
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
                    // Boshlang'ich miqdor kiritilgan bo'lsa stock movement yuboramiz
                    if (initialStock > 0) {
                        val raw = repo.jsonToAny(repo.extractData(r.data))
                        val newId = (raw as? Map<*, *>)?.let {
                            it["id"]?.toString()?.takeIf { s -> s.isNotEmpty() }
                                ?: it["_id"]?.toString()
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

    fun addStockMovement(id: String, quantity: Double, type: String = "INCOMING", reason: String = "Qo'lda kiritildi") {
        viewModelScope.launch {
            when (val r = repo.addStockMovement(id, StockMovementRequest(
                movementType = type,
                quantity = quantity,
                reason = reason
            ))) {
                is ApiResult.Success -> loadIngredients()
                is ApiResult.Error -> _errorMsg.value = r.message
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
                is ApiResult.Error -> {
                    _recipesState.value = ApiResult.Error(r.message)
                    _errorMsg.value = r.message
                }
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
                is ApiResult.Success -> {
                    val data = repo.jsonToAny(repo.extractData(r.data))
                    _movementsState.value = ApiResult.Success(parseMovements(data))
                }
                is ApiResult.Error -> {
                    _movementsState.value = ApiResult.Error(r.message)
                    _errorMsg.value = r.message
                }
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
                is ApiResult.Error -> {
                    _menuCalendarState.value = ApiResult.Error(r.message)
                    _errorMsg.value = r.message
                }
                else -> {}
            }
        }
    }

    fun nextWeek()      { weekOffset++; loadMenuCalendar() }
    fun prevWeek()      { weekOffset--; loadMenuCalendar() }
    fun goCurrentWeek() { weekOffset = 0; loadMenuCalendar() }

    fun createMenuEntry(recipeId: String, mealType: String, dateKey: String) {
        viewModelScope.launch {
            when (val r = repo.createMenuEntry(
                MenuEntryRequest(recipeId = recipeId, mealType = mealType, dateKey = dateKey)
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
    // Parse funksiyalari
    // ─────────────────────────────────────────

    private fun parseNumber(v: Any?): Double = when (v) {
        is Number -> v.toDouble()
        is String -> v.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }

    private fun parseBool(v: Any?): Boolean = when (v) {
        is Boolean -> v
        is Number  -> v.toInt() != 0
        is String  -> v.lowercase() == "true" || v == "1" || v == "active"
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

    // id yoki _id — backend ikki xil qaytaradi
    private fun Map<*, *>.resolveId(): String =
        this["id"]?.toString()?.takeIf { it.isNotEmpty() }
            ?: this["_id"]?.toString() ?: ""

    // ─────────────────────────────────────────
    // parseDashboard
    // FIX: today_meals / tomorrow_meals — list, count emas
    //      pending_confirmations_count — to'g'ri kalit
    //      low_stock_count — to'g'ri kalit
    //      active_recipes, total_ingredients qo'shildi
    // ─────────────────────────────────────────
    @Suppress("UNCHECKED_CAST")
    private fun parseDashboard(data: Any?): ChefDashboardUi {
        val m = data as? Map<*, *> ?: return ChefDashboardUi()

        // today_meals — list yoki son bo'lishi mumkin
        val todayMeals = when (val v = m["today_meals"]) {
            is List<*> -> v.size
            is Number  -> v.toInt()
            else       -> parseNumber(m["today_meals_count"]).toInt()
        }

        // tomorrow_meals — list yoki son
        val tomorrowMeals = when (val v = m["tomorrow_meals"]) {
            is List<*> -> v.size
            is Number  -> v.toInt()
            else       -> parseNumber(m["tomorrow_meals_count"]).toInt()
        }

        // pending_confirmations — list yoki son
        val pendingCount = when (val v = m["pending_confirmations"]) {
            is List<*> -> v.size
            is Number  -> v.toInt()
            else       -> parseNumber(m["pending_confirmations_count"] ?: m["pending_count"]).toInt()
        }

        return ChefDashboardUi(
            todayMeals           = todayMeals,
            pendingConfirmations = pendingCount,
            tomorrowMeals        = tomorrowMeals,
            // FIX: low_stock_count — to'g'ri kalit
            warehouseAlerts      = parseNumber(
                m["low_stock_count"] ?: m["warehouse_alerts"] ?: m["alerts_count"]
            ).toInt(),
            activeRecipes        = parseNumber(m["active_recipes"] ?: m["recipes_count"]).toInt(),
            totalIngredients     = parseNumber(m["total_ingredients"] ?: m["ingredients_count"]).toInt()
        )
    }

    // ─────────────────────────────────────────
    // parseIngredients
    // FIX: current_stock (quantity), minimum_stock (minQuantity),
    //      stock_status: "HEALTHY"/"LOW"/"OUT_OF_STOCK",
    //      expiration_date (expiryDate), _id (id)
    // ─────────────────────────────────────────
    @Suppress("UNCHECKED_CAST")
    private fun parseIngredients(data: Any?): List<IngredientItem> {
        if (data == null) return emptyList()
        return try {
            val list: List<*> = when (data) {
                is List<*>   -> data
                is Map<*, *> -> (data["items"] as? List<*>)
                    ?: (data["data"] as? List<*>)
                    ?: (data["ingredients"] as? List<*>)
                    ?: (data["results"] as? List<*>)
                    ?: return emptyList()
                else -> return emptyList()
            }
            list.mapNotNull { item ->
                val m = item as? Map<*, *> ?: return@mapNotNull null

                // FIX: current_stock asosiy, qolganlar fallback
                val qty = parseNumber(
                    m["current_stock"] ?: m["quantity"] ?: m["current_quantity"] ?: m["stock"]
                )
                // FIX: minimum_stock asosiy
                val minQty = parseNumber(
                    m["minimum_stock"] ?: m["min_quantity"] ?: m["minimum_quantity"] ?: m["min_stock"]
                )

                // Miqdor tekshiruvi — backend statusidan ustun turadi
                val stockStatus = m["stock_status"]?.toString()?.uppercase()
                val apiStatus   = m["status"]?.toString()?.lowercase()
                val status = when {
                    // 1. Avval miqdorni tekshiramiz — eng ishonchli
                    qty <= 0                           -> StockStatus.TUGAGAN
                    minQty > 0 && qty < minQty         -> StockStatus.KAM
                    // 2. Keyin backend statusini hisobga olamiz
                    stockStatus == "OUT_OF_STOCK"
                            || apiStatus == "out_of_stock"
                            || apiStatus == "tugagan"  -> StockStatus.TUGAGAN
                    stockStatus == "CRITICAL"           -> StockStatus.KAM
                    stockStatus == "LOW"
                            || apiStatus == "low"
                            || apiStatus == "low_stock"
                            || apiStatus == "kam"      -> StockStatus.KAM
                    else                               -> StockStatus.YETARLI
                }

                IngredientItem(
                    // FIX: _id asosiy
                    id          = m.resolveId(),
                    name        = m["name"]?.toString() ?: "",
                    category    = m["category"]?.toString() ?: "",
                    quantity    = qty,
                    unit        = normalizeUnit(m["unit"]?.toString() ?: "kg"),
                    minQuantity = minQty,
                    // FIX: expiration_date (backend), expiry_date (eski)
                    expiryDate  = (m["expiration_date"] ?: m["expiry_date"])
                        ?.toString()?.substringBefore("T") ?: "",
                    status      = status
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    // ─────────────────────────────────────────
    // parseRecipes
    // FIX: serving_count (portionCount), _id (id),
    //      isActive: is_active=true AND status="ACTIVE"
    // ─────────────────────────────────────────
    @Suppress("UNCHECKED_CAST")
    private fun parseRecipes(data: Any?): List<Recipe> {
        if (data == null) return emptyList()
        return try {
            val list: List<*> = when (data) {
                is List<*>   -> data
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

                // FIX: serving_count asosiy kalit
                val portionCount = parseNumber(
                    m["serving_count"] ?: m["portion_count"] ?: m["portions"] ?: m["serving_size"]
                ).toInt()

                // FIX: is_active bool + status="ACTIVE" ikkalasini tekshirish
                val isActiveBool   = parseBool(m["is_active"] ?: m["active"])
                val statusStr      = m["status"]?.toString()?.uppercase()
                val isActive       = isActiveBool || statusStr == "ACTIVE"

                Recipe(
                    id             = m.resolveId(),
                    name           = m["name"]?.toString() ?: "",
                    category       = m["category"]?.toString() ?: "",
                    portionCount   = portionCount,
                    isActive       = isActive,
                    ingredientCount = ingredients?.size
                        ?: parseNumber(m["ingredient_count"] ?: m["ingredients_count"]).toInt()
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    // ─────────────────────────────────────────
    // parseMovements
    // FIX: movement_type: "INCOMING"/"OUTGOING"
    // ─────────────────────────────────────────
    @Suppress("UNCHECKED_CAST")
    private fun parseMovements(data: Any?): List<StockMovement> {
        if (data == null) return emptyList()
        return try {
            val list: List<*> = when (data) {
                is List<*>   -> data
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
                val createdAt = m["created_at"]?.toString()
                    ?: m["date"]?.toString()
                    ?: m["timestamp"]?.toString() ?: ""
                val datePart = createdAt.substringBefore("T").ifEmpty { createdAt.take(10) }
                val timePart = if (createdAt.contains("T")) createdAt.substringAfter("T").take(5) else ""

                val ingMap  = m["ingredient"] as? Map<*, *>
                val ingName = ingMap?.get("name")?.toString()
                    ?: m["ingredient_name"]?.toString()
                    ?: m["product_name"]?.toString()
                    ?: m["name"]?.toString() ?: ""
                val rawUnit = ingMap?.get("unit")?.toString() ?: m["unit"]?.toString() ?: ""

                val typeStr = m["movement_type"]?.toString()
                    ?: m["type"]?.toString()
                    ?: m["action"]?.toString() ?: ""

                // FIX: "INCOMING"/"OUTGOING" backend dan kelgan qiymatlar
                val type = when (typeStr.uppercase()) {
                    "INCOMING", "IN", "KIRIM",
                    "INCOME", "RECEIPT", "ARRIVAL",
                    "PURCHASE", "INBOUND"            -> MovementType.KIRIM
                    "OUTGOING", "OUT", "CHIQIM",
                    "OUTCOME", "CONSUMPTION",
                    "USAGE", "EXPENSE", "OUTBOUND",
                    "DEDUCTION"                      -> MovementType.CHIQIM
                    else                             -> MovementType.TUZATISH
                }

                StockMovement(
                    date       = datePart,
                    time       = timePart,
                    ingredient = ingName,
                    unit       = normalizeUnit(rawUnit),
                    type       = type,
                    amount     = parseNumber(
                        m["quantity"] ?: m["amount"] ?: m["changed_quantity"] ?: m["quantity_changed"]
                    ),
                    prevQty    = parseNumber(
                        m["before_quantity"] ?: m["previous_quantity"] ?: m["old_quantity"] ?: m["quantity_before"]
                    ),
                    newQty     = parseNumber(
                        m["after_quantity"] ?: m["new_quantity"] ?: m["current_quantity"] ?: m["quantity_after"]
                    ),
                    reason     = m["reason"]?.toString()
                        ?: m["note"]?.toString()
                        ?: m["description"]?.toString()
                        ?: m["comment"]?.toString() ?: ""
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    // ─────────────────────────────────────────
    // parseMenuCalendar — o'zgarishsiz (strukturasi to'g'ri edi)
    // ─────────────────────────────────────────
    @Suppress("UNCHECKED_CAST")
    private fun parseMenuCalendar(data: Any?, weekMonday: LocalDate): List<MenuDayUi> {
        val dayNames = listOf("Dushanba", "Seshanba", "Chorshanba", "Payshanba", "Juma", "Shanba", "Yakshanba")
        val mealLabels = mapOf(
            "breakfast" to "Nonushta",
            "BREAKFAST" to "Nonushta",
            "lunch"     to "Tushlik",
            "LUNCH"     to "Tushlik",
            "dinner"    to "Kechki ovqat",
            "DINNER"    to "Kechki ovqat",
            "snack"     to "Kechki tamaddi",
            "SNACK"     to "Kechki tamaddi"
        )
        val todayStr = LocalDate.now().format(fmt)
        return try {
            val map: Map<String, List<*>> = when (data) {
                is Map<*, *> -> @Suppress("UNCHECKED_CAST") (data as Map<String, List<*>>)
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
                map[dateStr]?.forEach { mealItem ->
                    val mm = mealItem as? Map<*, *> ?: return@forEach
                    val recipe    = mm["recipe"] as? Map<*, *>
                    val mealType  = mm["meal_type"]?.toString() ?: ""
                    val statusStr = mm["status"]?.toString() ?: "assigned"
                    val mealStatus = when (statusStr.lowercase()) {
                        "confirmed" -> MealStatus.CONFIRMED
                        else        -> MealStatus.ASSIGNED
                    }
                    meals.add(MenuMealUi(
                        id            = mm.resolveId(),
                        mealType      = mealType,
                        mealTypeLabel = mealLabels[mealType] ?: mealType,
                        recipeName    = recipe?.get("name")?.toString()
                            ?: mm["recipe_name"]?.toString(),
                        status        = mealStatus
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