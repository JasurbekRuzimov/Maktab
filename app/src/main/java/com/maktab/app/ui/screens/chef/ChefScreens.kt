package com.maktab.app.ui.screens.chef

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maktab.app.network.ApiResult
import com.maktab.app.network.models.IngredientRequest
import com.maktab.app.network.models.RecipeRequest
import com.maktab.app.network.models.RecipeIngredient
import com.maktab.app.ui.components.*
import com.maktab.app.ui.theme.*
import com.maktab.app.viewmodel.*
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────
// MealSlot — ImageVector borligi uchun bu yerda qoladi
// ─────────────────────────────────────────────

data class MealSlot(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val subtitle: String,
    val meal: String? = null,
    val status: MealStatus = MealStatus.EMPTY
)

private val defaultTodaySlots = listOf(
    MealSlot("nonushta", "Nonushta", Icons.Default.WbSunny, "Kun boshidagi issiq ovqat"),
    MealSlot("tushlik", "Tushlik", Icons.Default.LunchDining, "Asosiy tushlik menyusi"),
    MealSlot("peshinlik", "Peshinlik", Icons.Default.Coffee, "Peshin payti beriladigan ovqat"),
    MealSlot("kechki", "Kechki ovqat", Icons.Default.DinnerDining, "Kechki navbat uchun taom")
)

private val defaultMealTimes = listOf("Nonushta", "Tushlik", "Kechki ovqat", "Kechki tamaddi")

// ─────────────────────────────────────────────
// 1. OSHXONA DASHBOARDI
// ─────────────────────────────────────────────

@Composable
fun ChefDashboardScreen(vm: ChefViewModel, onNavigateToIngredients: () -> Unit = {}) {
    val dashboardState      by vm.dashboardState.collectAsState()
    val menuCalendarState   by vm.menuCalendarState.collectAsState()

    LaunchedEffect(Unit) {
        vm.loadDashboard()
        vm.loadMenuCalendar()
    }

    val mealLabels = listOf("Nonushta", "Tushlik", "Kechki ovqat", "Kechki tamaddi")
    val tabs = listOf("Bugun", "Ertaga", "Kecha")
    var selectedTab by remember { mutableStateOf(0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Column(Modifier.padding(16.dp)) {
                SectionHeader("Oshpaz bosh paneli") {}
                Text(
                    "Bugungi taomlarni tasdiqlang, kechagi yakun va ertangi tayyorgarlikni bir joyda ko'ring.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(14.dp))

                // Stat kartochkalar — Loading / Success holatlari
                when (val state = dashboardState) {
                    ApiResult.Loading -> {
                        Box(
                            Modifier.fillMaxWidth().height(110.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Amber10, strokeWidth = 2.dp)
                        }
                    }
                    is ApiResult.Error -> {
                        Row(
                            Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(RedContainer)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.WifiOff, null, tint = Red10, modifier = Modifier.size(16.dp))
                            Text(state.message, fontSize = 12.sp, color = Red10, modifier = Modifier.weight(1f))
                            TextButton(onClick = { vm.loadDashboard() }) {
                                Text("Qayta", fontSize = 12.sp, color = Red10)
                            }
                        }
                    }
                    is ApiResult.Success -> {
                        val d = state.data
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ChefStatCard(
                                "Bugungi taomlar", "${d.todayMeals}", "Bugun rejalashtirilgan",
                                Blue10, BlueContainer, Modifier.weight(1f)
                            )
                            ChefStatCard(
                                "Tasdiq navbati", "${d.pendingConfirmations}", "Tasdiq kutilmoqda",
                                Amber10, AmberContainer, Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            ChefStatCard(
                                "Ingredientlar", "${d.totalIngredients}", "Omborda jami",
                                Teal10, TealContainer, Modifier.weight(1f)
                            )
                            ChefStatCard(
                                "Faol retseptlar", "${d.activeRecipes}", "Tayyor retseptlar",
                                Teal10, TealContainer, Modifier.weight(1f)
                            )
                        }
                        if (d.warehouseAlerts > 0) {
                            Spacer(Modifier.height(10.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onNavigateToIngredients() },
                                colors = CardDefaults.cardColors(containerColor = RedContainer),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(0.5.dp, Red10.copy(0.3f)),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                                            .background(Red10.copy(0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) { Icon(Icons.Default.Warning, null, tint = Red10, modifier = Modifier.size(20.dp)) }
                                    Column(Modifier.weight(1f)) {
                                        Text("Ombor ogohlantirishlari", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Red10)
                                        Text("${d.warehouseAlerts} ta mahsulot kam qolgan", fontSize = 12.sp, color = Red10.copy(0.7f))
                                    }
                                    Icon(Icons.Default.ArrowForward, null, tint = Red10, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tab qatorlari
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.padding(horizontal = 16.dp),
                containerColor = Color.Transparent,
                edgePadding = 0.dp,
                indicator = {},
                divider = {}
            ) {
                tabs.forEachIndexed { idx, title ->
                    val selected = selectedTab == idx
                    FilterChip(
                        selected = selected,
                        onClick = { selectedTab = idx },
                        label = { Text(title, fontSize = 13.sp) },
                        modifier = Modifier.padding(end = 8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Teal10,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true, selected = selected,
                            borderColor = Outline, selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Menyu kalendardan bugungi / ertangi / kechagi taomlar
        item {
            val dayLabel = when (selectedTab) {
                0 -> "Bugungi taomlar"
                1 -> "Ertangi tayyorgarlik"
                else -> "Kechagi taomlar"
            }
            Text(dayLabel, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(10.dp))
        }

        when (val calState = menuCalendarState) {
            ApiResult.Loading -> item {
                Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), Alignment.Center) {
                    CircularProgressIndicator(color = Amber10, strokeWidth = 2.dp)
                }
            }
            is ApiResult.Success -> {
                val today = java.time.LocalDate.now()
                val targetDate = when (selectedTab) {
                    1    -> today.plusDays(1)
                    2    -> today.minusDays(1)
                    else -> today
                }
                val fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val targetKey = targetDate.format(fmt)
                val dayUi = calState.data.find { it.dateKey == targetKey }
                val meals = dayUi?.meals ?: emptyList()

                if (meals.isEmpty()) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(vertical = 20.dp),
                            Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.RestaurantMenu, null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(32.dp))
                                Text("Bu kun uchun taom belgilanmagan",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    items(meals, key = { it.id }) { meal ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 5.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(0.5.dp, Outline),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
                                        .background(AmberContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Restaurant, null,
                                        tint = Amber10, modifier = Modifier.size(22.dp))
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(meal.mealTypeLabel, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(4.dp))
                                    if (meal.recipeName != null) {
                                        StatusChip(meal.recipeName, Teal10, TealContainer)
                                    } else {
                                        Text(
                                            "Taom belgilanmagan",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .border(0.5.dp, Outline, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                                if (meal.status == MealStatus.CONFIRMED) {
                                    Icon(Icons.Default.CheckCircle, null,
                                        tint = Teal10, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
            is ApiResult.Error -> item {
                Text(calState.message, fontSize = 12.sp, color = Red10,
                    modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
private fun ChefStatCard(
    label: String, value: String, subtitle: String,
    color: Color, container: Color, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, Outline),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 15.sp)
        }
    }
}

@Composable
fun ChefOmborScreen(vm: ChefViewModel) {
    val ingredientsState by vm.ingredientsState.collectAsState()
    val movementsState by vm.movementsState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.loadIngredients()
        vm.loadMovements()
    }

    if (showAddSheet) {
        AddIngredientSheet(onDismiss = { showAddSheet = false }, onSave = { req, initialStock ->
            vm.createIngredient(req, initialStock)
            showAddSheet = false
        })
    }

    val allIngredients = (ingredientsState as? ApiResult.Success)?.data ?: emptyList()
    val allMovements = (movementsState as? ApiResult.Success)?.data ?: emptyList()

    val filtered = allIngredients.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true)
    }
    val yetarli = filtered.count { it.status == StockStatus.YETARLI }
    val kam = filtered.count { it.status == StockStatus.KAM }
    val tugagan = filtered.count { it.status == StockStatus.TUGAGAN }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Column(Modifier.padding(16.dp)) {
                SectionHeader("Oziq-ovqat ombori") {}
                Text(
                    "Ombor ko'rsatkichlari, kam qolgan mahsulotlar, tez kirim-chiqim.",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp
                )
                Spacer(Modifier.height(14.dp))

                when (ingredientsState) {
                    is ApiResult.Loading -> {
                        Box(Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Amber10, modifier = Modifier.size(24.dp))
                        }
                    }
                    is ApiResult.Error -> {
                        val msg = (ingredientsState as ApiResult.Error).message
                        Text(msg, color = Red10, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { vm.loadIngredients() }) { Text("Qayta urinish", color = Amber10) }
                    }
                    else -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ChefStatCard("Ingredientlar", "${filtered.size}", "Jami", Blue10, BlueContainer, Modifier.weight(1f))
                            ChefStatCard("Ombor ogoh.", "$kam", "Kam qolgan", Amber10, AmberContainer, Modifier.weight(1f))
                            ChefStatCard("Tugagan", "$tugagan", "Zaxira yo'q", Red10, RedContainer, Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Ingredient katalogi", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Button(
                        onClick = { showAddSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Teal10),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Yangi", fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Ingredient yoki kategoriya qidirish", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Teal10, unfocusedBorderColor = Outline)
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip("$yetarli Yetarli", Teal10, TealContainer)
                    StatusChip("$kam Kam qolgan", Amber10, AmberContainer)
                    StatusChip("$tugagan Tugagan", Red10, RedContainer)
                }
                Spacer(Modifier.height(12.dp))
            }
        }

        items(filtered) { ing ->
            IngredientRow(
                ing = ing,
                onSave = { req -> vm.updateIngredient(ing.id, req) },
                onUploadImage = { file -> vm.uploadIngredientImage(ing.id, file) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        item {
            Spacer(Modifier.height(16.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("So'nggi ombor harakatlari", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                when (movementsState) {
                    is ApiResult.Loading -> {
                        Box(Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Amber10, modifier = Modifier.size(24.dp))
                        }
                    }
                    is ApiResult.Error -> {
                        Text((movementsState as ApiResult.Error).message, color = Red10, fontSize = 13.sp)
                    }
                    else -> {}
                }
            }
        }

        items(allMovements.take(3)) { mv ->
            MovementRow(mv, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientRow(
    ing: IngredientItem,
    onSave: (IngredientRequest) -> Unit,
    onUploadImage: (java.io.File) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val (color, container) = when (ing.status) {
        StockStatus.YETARLI -> Pair(Teal10, TealContainer)
        StockStatus.KAM     -> Pair(Amber10, AmberContainer)
        StockStatus.TUGAGAN -> Pair(Red10, RedContainer)
    }

    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var editName     by remember(ing.id) { mutableStateOf(ing.name) }
    var editCategory by remember(ing.id) { mutableStateOf(ing.category) }
    var editQuantity by remember(ing.id) { mutableStateOf(ing.quantity.toLong().toString()) }
    var editMin      by remember(ing.id) { mutableStateOf(ing.minQuantity.toLong().toString()) }
    var editUnit     by remember(ing.id) { mutableStateOf(ing.unit) }
    var editExpiry   by remember(ing.id) { mutableStateOf(ing.expiryDate) }
    var saved        by remember { mutableStateOf(false) }
    var imageUri     by remember { mutableStateOf<android.net.Uri?>(null) }
    val scope        = rememberCoroutineScope()
    val context      = androidx.compose.ui.platform.LocalContext.current

    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            imageUri = uri
            // Uri dan File yaratamiz va upload qilamiz
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val ext = context.contentResolver.getType(uri)?.substringAfterLast("/") ?: "jpg"
                val tempFile = java.io.File(context.cacheDir, "img_${System.currentTimeMillis()}.$ext")
                tempFile.outputStream().use { out -> inputStream?.copyTo(out) }
                onUploadImage(tempFile)
            } catch (e: Exception) {
                android.util.Log.e("ImageUpload", e.message ?: "Xato")
            }
        }
    }

    val units = listOf("kg", "g", "l", "ml", "dona", "litr")
    val categories = listOf("Sabzavot", "Don mahsulot", "Go'sht mahsulot", "Sut mahsulot", "Meva", "Boshqa")

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, Outline),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)).background(AmberContainer),
                contentAlignment = Alignment.Center
            ) {
                if (ing.imageUrl != null) {
                    coil.compose.AsyncImage(
                        model = ing.imageUrl,
                        contentDescription = ing.name,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Inventory, null, tint = Amber10, modifier = Modifier.size(20.dp))
                }
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(ing.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    StatusChip(ing.status.name, color, container)
                }
                Spacer(Modifier.height(2.dp))
                Text("${ing.category} · min: ${ing.minQuantity.toLong()} ${ing.unit}",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (ing.expiryDate.isNotEmpty()) {
                    Text("Tugash: ${
                        ing.expiryDate.let { d ->
                            if (d.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                                val p = d.split("-"); "${p[2]}.${p[1]}.${p[0]}"
                            } else d
                        }
                    }",
                        fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text("${ing.quantity.toLong()} ${ing.unit}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
                Box(
                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
                        .clickable { showSheet = true }.background(Blue10.copy(0.09f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, "Tahrirlash", tint = Blue10, modifier = Modifier.size(20.dp))
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            windowInsets = WindowInsets.navigationBars
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp).padding(bottom = 32.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(AmberContainer),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Inventory, null, tint = Amber10, modifier = Modifier.size(22.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Ingredientni tahrirlash", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(ing.name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { showSheet = false }) {
                        Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = Outline, thickness = 0.5.dp)
                Spacer(Modifier.height(20.dp))

                SheetField("Ingredient nomi", editName, { editName = it }, Icons.Default.Label, "Masalan: Kartoshka")
                Spacer(Modifier.height(14.dp))

                Text("Kategoriya", fontSize = 13.sp, fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSel = editCategory == cat
                        FilterChip(selected = isSel, onClick = { editCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Amber10, selectedLabelColor = Color.White),
                            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = isSel, borderColor = Outline, selectedBorderColor = Color.Transparent))
                    }
                }
                Spacer(Modifier.height(14.dp))

                Text("O'lchov birligi", fontSize = 13.sp, fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    units.forEach { u ->
                        val isSel = editUnit == u
                        FilterChip(selected = isSel, onClick = { editUnit = u },
                            label = { Text(u, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Amber10, selectedLabelColor = Color.White),
                            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = isSel, borderColor = Outline, selectedBorderColor = Color.Transparent))
                    }
                }
                Spacer(Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        SheetField("Joriy miqdor", editQuantity,
                            { editQuantity = it.filter { c -> c.isDigit() } },
                            Icons.Default.Inventory, "0", KeyboardType.Number)
                    }
                    Column(Modifier.weight(1f)) {
                        SheetField("Minimal miqdor", editMin,
                            { editMin = it.filter { c -> c.isDigit() } },
                            Icons.Default.Warning, "0", KeyboardType.Number)
                    }
                }
                Spacer(Modifier.height(14.dp))

                // ── Rasm bo'limi ──
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)).background(AmberContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        val displayUri = imageUri ?: ing.imageUrl?.let { android.net.Uri.parse(it) }
                        if (displayUri != null) {
                            coil.compose.AsyncImage(
                                model = displayUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Image, null, tint = Amber10, modifier = Modifier.size(26.dp))
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Ingredient rasmi", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(
                            if (ing.imageUrl != null) "Rasm mavjud · yangilash mumkin"
                            else "Rasm yuklanmagan",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        border = BorderStroke(0.5.dp, Amber10)
                    ) {
                        Icon(Icons.Default.Upload, null, modifier = Modifier.size(14.dp), tint = Amber10)
                        Spacer(Modifier.width(4.dp))
                        Text("Yuklash", fontSize = 12.sp, color = Amber10)
                    }
                }

                DatePickerField("Yaroqlilik muddati", editExpiry, { editExpiry = it })

                Spacer(Modifier.height(24.dp))

                if (saved) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(TealContainer).padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Teal10, modifier = Modifier.size(20.dp))
                        Text("Muvaffaqiyatli saqlandi!", fontSize = 14.sp, color = Teal10, fontWeight = FontWeight.Medium)
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false } },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(0.5.dp, Outline)
                        ) { Text("Bekor qilish", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface) }
                        Button(
                            onClick = {
                                onSave(IngredientRequest(
                                    name = editName,
                                    category = editCategory.ifEmpty { null },
                                    unit = editUnit,
                                    quantity = editQuantity.toDoubleOrNull() ?: ing.quantity,
                                    minQuantity = editMin.toDoubleOrNull() ?: ing.minQuantity,
                                    expiryDate = editExpiry.ifEmpty { null }
                                ))
                                saved = true
                                scope.launch {
                                    kotlinx.coroutines.delay(800)
                                    sheetState.hide()
                                    showSheet = false
                                    saved = false
                                }
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Amber10),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Saqlash", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetField(
    label: String, value: String, onValueChange: (String) -> Unit,
    icon: ImageVector, placeholder: String, keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            shape = RoundedCornerShape(10.dp),
            placeholder = { Text(placeholder, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingIcon = { Icon(icon, null, tint = Amber10, modifier = Modifier.size(18.dp)) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Amber10, unfocusedBorderColor = Outline)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    value: String,          // "YYYY-MM-DD" formatida saqlash
    onValueChange: (String) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }

    // Ko'rsatish uchun DD.MM.YYYY formatga o'giramiz
    val displayValue = if (value.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
        val parts = value.split("-")
        "${parts[2]}.${parts[1]}.${parts[0]}"
    } else value

    Column {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = displayValue,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            readOnly = true,
            shape = RoundedCornerShape(10.dp),
            placeholder = { Text("KK.OO.YYYY", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingIcon = { Icon(Icons.Default.CalendarToday, null, tint = Amber10, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Amber10, unfocusedBorderColor = Outline),
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }.also { src ->
                val pressed by src.collectIsPressedAsState()
                if (pressed) showPicker = true
            }
        )
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (value.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                LocalDate.parse(value).atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
            } else null
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        onValueChange(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    }
                    showPicker = false
                }) { Text("OK", color = Amber10) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Bekor", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        ) {
            DatePicker(
                state = pickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Amber10,
                    todayDateBorderColor = Amber10,
                    selectedYearContainerColor = Amber10
                )
            )
        }
    }
}

// ─────────────────────────────────────────────
// 3. INGREDIENTLAR BOSHQARUVI
// ─────────────────────────────────────────────

@Composable
fun ChefIngredientsScreen(vm: ChefViewModel, showLowStock: Boolean = false) {
    val ingredientsState by vm.ingredientsState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Barchasi") }
    var showOnlyLowStock by remember { mutableStateOf(showLowStock) }

    LaunchedEffect(showLowStock) { if (showLowStock) showOnlyLowStock = true }
    val filters = listOf("Barchasi", "Sabzavot", "Don mahsulot", "Et mahsulot", "Sut mahsulot")
    var showAddSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.loadIngredients() }

    val allIngredients = (ingredientsState as? ApiResult.Success)?.data ?: emptyList()
    val filtered = allIngredients.filter { ing ->
        (selectedFilter == "Barchasi" || ing.category.equals(selectedFilter, ignoreCase = true)) &&
                ing.name.contains(searchQuery, ignoreCase = true) &&
                (!showOnlyLowStock || ing.status in listOf(StockStatus.KAM, StockStatus.TUGAGAN))
    }

    if (showAddSheet) {
        AddIngredientSheet(onDismiss = { showAddSheet = false }, onSave = { req, initialStock ->
            vm.createIngredient(req, initialStock)
            showAddSheet = false
        })
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Column(Modifier.padding(16.dp)) {
                SectionHeader("Ingredient boshqaruvi") {
                    Button(
                        onClick = { showAddSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Teal10),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Yangi ingredient", fontSize = 12.sp)
                    }
                }
                Text(
                    "Ingredient kartalari, o'lchov birliklari, minimal qoldiq va yaroqlilik muddatlarini yuriting.",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp
                )
                Spacer(Modifier.height(12.dp))

                when (ingredientsState) {
                    is ApiResult.Loading -> {
                        Box(Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Amber10, modifier = Modifier.size(24.dp))
                        }
                        return@Column
                    }
                    is ApiResult.Error -> {
                        Text((ingredientsState as ApiResult.Error).message, color = Red10, fontSize = 13.sp)
                        TextButton(onClick = { vm.loadIngredients() }) { Text("Qayta urinish", color = Amber10) }
                        return@Column
                    }
                    else -> {}
                }

                OutlinedTextField(
                    value = searchQuery, onValueChange = { searchQuery = it },
                    placeholder = { Text("Ingredient yoki kategoriya qidirish", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Teal10, unfocusedBorderColor = Outline)
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip("${allIngredients.size} Ingredientlar", Blue10, BlueContainer)
                    StatusChip("${allIngredients.count { it.status == StockStatus.KAM }} Kam qolgan", Amber10, AmberContainer)
                    StatusChip("${allIngredients.count { it.status == StockStatus.TUGAGAN }} Tugagan", Red10, RedContainer)
                }
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = showOnlyLowStock,
                            onClick = { showOnlyLowStock = !showOnlyLowStock },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Warning, null, modifier = Modifier.size(12.dp))
                                    Text("Kam qolgan", fontSize = 12.sp)
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Red10, selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = showOnlyLowStock, borderColor = Outline, selectedBorderColor = Color.Transparent)
                        )
                    }
                    items(filters) { f ->
                        FilterChip(
                            selected = selectedFilter == f, onClick = { selectedFilter = f; if (f != "Barchasi") showOnlyLowStock = false },
                            label = { Text(f, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Teal10, selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selectedFilter == f, borderColor = Outline, selectedBorderColor = Color.Transparent)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }

        if (ingredientsState is ApiResult.Success) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ingredient", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.8f))
                    Text("Min.qoldiq", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text("Qoldiq", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text("Holat", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }
            }

            itemsIndexed(filtered) { idx, ing ->
                val (color, container) = when (ing.status) {
                    StockStatus.YETARLI -> Pair(Teal10, TealContainer)
                    StockStatus.KAM -> Pair(Amber10, AmberContainer)
                    StockStatus.TUGAGAN -> Pair(Red10, RedContainer)
                }
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        .let { if (idx == filtered.lastIndex) it.clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)) else it }
                        .background(Color.White).border(0.5.dp, Outline)
                ) {
                    if (idx > 0) HorizontalDivider(color = Outline, thickness = 0.5.dp)
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1.8f)) {
                            Text(ing.name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(ing.category, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${ing.minQuantity.toLong()} ${ing.unit}", fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${ing.quantity.toLong()} ${ing.unit}", fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = color)
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                            StatusChip(ing.status.name.lowercase().replaceFirstChar { it.uppercase() }, color, container)
                        }
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inventory, null, tint = Outline, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Ingredientlar topilmadi", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 4. TAOM RETSEPTLARI
// ─────────────────────────────────────────────

@Composable
fun ChefRecipesScreen(vm: ChefViewModel) {
    val recipesState     by vm.recipesState.collectAsState()
    val ingredientsState by vm.ingredientsState.collectAsState()
    var searchQuery  by remember { mutableStateOf("") }
    var filterActive by remember { mutableStateOf<Boolean?>(null) }
    var showAddSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.loadRecipes(); vm.loadIngredients() }

    val allIngredients = (ingredientsState as? ApiResult.Success)?.data ?: emptyList()

    if (showAddSheet) {
        AddRecipeSheet(
            allIngredients = allIngredients,
            onDismiss = { showAddSheet = false },
            onSave = { req ->
                vm.createRecipe(req)
                showAddSheet = false
            }
        )
    }

    val allRecipes = (recipesState as? ApiResult.Success)?.data ?: emptyList()
    val filtered = allRecipes.filter { r ->
        r.name.contains(searchQuery, ignoreCase = true) &&
                (filterActive == null || r.isActive == filterActive)
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Column(Modifier.padding(16.dp)) {
                SectionHeader("Taom retseptlari") {
                    Button(
                        onClick = { showAddSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Teal10),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Yangi retsept", fontSize = 12.sp)
                    }
                }
                Text(
                    "Palov, sho'rva, shovla va boshqa maktab taomlari uchun ingredient miqdorlari va porsiya hisobini boshqaring.",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp
                )
                Spacer(Modifier.height(12.dp))

                when (recipesState) {
                    is ApiResult.Loading -> {
                        Box(Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Amber10, modifier = Modifier.size(24.dp))
                        }
                        return@Column
                    }
                    is ApiResult.Error -> {
                        Text((recipesState as ApiResult.Error).message, color = Red10, fontSize = 13.sp)
                        TextButton(onClick = { vm.loadRecipes() }) { Text("Qayta urinish", color = Amber10) }
                        return@Column
                    }
                    else -> {}
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery, onValueChange = { searchQuery = it },
                        placeholder = { Text("Qidirish", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f), singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Teal10, unfocusedBorderColor = Outline)
                    )
                    FilterChip(
                        selected = filterActive == true, onClick = { filterActive = if (filterActive == true) null else true },
                        label = { Text("Faol", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(14.dp)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TealContainer, selectedLabelColor = Teal10),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = filterActive == true, borderColor = Outline, selectedBorderColor = Teal10)
                    )
                    FilterChip(
                        selected = filterActive == false, onClick = { filterActive = if (filterActive == false) null else false },
                        label = { Text("Nofaol", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Cancel, null, modifier = Modifier.size(14.dp)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedContainer, selectedLabelColor = Red10),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = filterActive == false, borderColor = Outline, selectedBorderColor = Red10)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text("${filtered.size} / ${allRecipes.size}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
            }
        }

        if (filtered.isEmpty() && recipesState is ApiResult.Success) {
            item {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.MenuBook, null, tint = Outline, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Retseptlar topilmadi", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(filtered) { recipe ->
                RecipeCard(recipe, modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddRecipeSheet(
    allIngredients: List<IngredientItem>,
    onDismiss: () -> Unit,
    onSave: (RecipeRequest) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var name        by remember { mutableStateOf("") }
    var servingCount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isActive    by remember { mutableStateOf(true) }
    var error       by remember { mutableStateOf("") }

    // Tanlangan ingredientlar: ingredient -> quantity
    val selectedIngredients = remember { mutableStateMapOf<IngredientItem, String>() }
    var showIngPicker by remember { mutableStateOf(false) }
    var searchIng    by remember { mutableStateOf("") }

    val filteredIngs = allIngredients.filter { ing ->
        ing.name.contains(searchIng, ignoreCase = true) && ing !in selectedIngredients.keys
    }

    fun doSave() {
        error = ""
        if (name.isBlank()) { error = "Retsept nomini kiriting"; return }
        if (selectedIngredients.isEmpty()) { error = "Kamida 1 ta ingredient tanlang"; return }
        val invalidQty = selectedIngredients.any { (_, q) -> q.toDoubleOrNull() == null || q.toDoubleOrNull()!! <= 0 }
        if (invalidQty) { error = "Barcha ingredientlar miqdorini kiriting"; return }

        val req = RecipeRequest(
            name = name.trim(),
            portionCount = servingCount.toIntOrNull(),
            isActive = isActive,
            ingredients = selectedIngredients.entries.map { (ing, qty) ->
                RecipeIngredient(
                    ingredientId = ing.id,
                    quantity = qty.toDouble(),
                    unit = ing.unit
                )
            }
        )
        onSave(req)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Yangi retsept", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = { scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() } }) {
                    Icon(Icons.Default.Close, null)
                }
            }
            HorizontalDivider(color = Outline)

            // Retsept nomi
            SheetField("Retsept nomi *", name, { name = it }, Icons.Default.RestaurantMenu, "Palov, sho'rva...")

            // Porsiya soni
            SheetField("Porsiya soni", servingCount, { servingCount = it.filter { c -> c.isDigit() } }, Icons.Default.People, "10", KeyboardType.Number)

            // Holat
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CheckCircle, null, tint = if (isActive) Teal10 else Outline, modifier = Modifier.size(18.dp))
                    Text("Faol retsept", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Switch(checked = isActive, onCheckedChange = { isActive = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Teal10))
            }

            HorizontalDivider(color = Outline)

            // Ingredientlar bo'limi
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Ingredientlar *", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = { showIngPicker = true; searchIng = "" }) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = Teal10)
                    Spacer(Modifier.width(4.dp))
                    Text("Qo'shish", fontSize = 13.sp, color = Teal10)
                }
            }

            if (selectedIngredients.isEmpty()) {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp),
                    contentAlignment = Alignment.Center) {
                    Text("Ingredient tanlanmagan", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val ingList = selectedIngredients.entries.toList()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ingList.forEach { entry ->
                        val ing = entry.key
                        val qty = entry.value
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant).padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(ing.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(ing.unit, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            OutlinedTextField(
                                value = qty,
                                onValueChange = { v -> selectedIngredients[ing] = v.filter { c -> c.isDigit() || c == '.' } },
                                modifier = Modifier.width(90.dp),
                                singleLine = true,
                                placeholder = { Text("0.0", fontSize = 12.sp) },
                                suffix = { Text(ing.unit, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Teal10, unfocusedBorderColor = Outline)
                            )
                            IconButton(onClick = { selectedIngredients.remove(ing) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Close, null, tint = Red10, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Xato xabar
            if (error.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.ErrorOutline, null, tint = Red10, modifier = Modifier.size(14.dp))
                    Text(error, fontSize = 12.sp, color = Red10)
                }
            }

            // Saqlash tugmasi
            Button(onClick = { doSave() }, modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal10),
                shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Saqlash", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    // Ingredient tanlash dialog
    if (showIngPicker) {
        AlertDialog(
            onDismissRequest = { showIngPicker = false },
            title = { Text("Ingredient tanlash", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchIng, onValueChange = { searchIng = it },
                        placeholder = { Text("Qidirish...", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Teal10)
                    )
                    if (filteredIngs.isEmpty()) {
                        Text("Ingredientlar topilmadi", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 300.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(filteredIngs) { ing ->
                                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedIngredients[ing] = ""; showIngPicker = false; searchIng = "" }
                                    .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text(ing.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                        Text("${ing.quantity.toLong()} ${ing.unit}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Icon(Icons.Default.Add, null, tint = Teal10, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showIngPicker = false }) { Text("Yopish") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeCard(recipe: Recipe, modifier: Modifier = Modifier) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Card(
        modifier = modifier.fillMaxWidth().clickable { showSheet = true },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, Outline),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
                    .background(if (recipe.isActive) TealContainer else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.RestaurantMenu, null,
                    tint = if (recipe.isActive) Teal10 else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(recipe.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    StatusChip(
                        if (recipe.isActive) "Faol" else "Nofaol",
                        if (recipe.isActive) Teal10 else MaterialTheme.colorScheme.onSurfaceVariant,
                        if (recipe.isActive) TealContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                }
                Text(recipe.category, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.People, null, tint = Blue10, modifier = Modifier.size(14.dp))
                        Text("${recipe.portionCount} porsiya", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Inventory, null, tint = Amber10, modifier = Modifier.size(14.dp))
                        Text("${recipe.ingredientCount} ingredient", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            windowInsets = WindowInsets.navigationBars
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp).padding(bottom = 32.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                            .background(if (recipe.isActive) TealContainer else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.RestaurantMenu, null,
                            tint = if (recipe.isActive) Teal10 else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(recipe.name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Text(recipe.category, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false } }) {
                        Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = Outline, thickness = 0.5.dp)
                Spacer(Modifier.height(20.dp))

                StatusChip(
                    if (recipe.isActive) "Faol retsept" else "Nofaol retsept",
                    if (recipe.isActive) Teal10 else MaterialTheme.colorScheme.onSurfaceVariant,
                    if (recipe.isActive) TealContainer else MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = BlueContainer),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.5.dp, Blue10.copy(0.2f)),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.People, null, tint = Blue10, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.height(6.dp))
                            Text("${recipe.portionCount}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Blue10)
                            Text("Porsiya", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = AmberContainer),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.5.dp, Amber10.copy(0.2f)),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inventory, null, tint = Amber10, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.height(6.dp))
                            Text("${recipe.ingredientCount}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Amber10)
                            Text("Ingredient", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))

                Text("Kategoriya", fontSize = 13.sp, fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Category, null, tint = Teal10, modifier = Modifier.size(18.dp))
                    Text(recipe.category.ifEmpty { "Belgilanmagan" }, fontSize = 14.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 5. MENYU KALENDARI
// ─────────────────────────────────────────────

@Composable
fun ChefMenuCalendarScreen(vm: ChefViewModel) {
    val menuState    by vm.menuCalendarState.collectAsState()
    val recipesState by vm.recipesState.collectAsState()
    val weekLabel    by vm.weekLabel.collectAsState()

    // dateKey + mealType → taom tayinlash uchun
    var selectedSlot by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(Unit) {
        vm.loadMenuCalendar()
        vm.loadRecipes()
    }

    val days       = (menuState as? ApiResult.Success)?.data ?: emptyList()
    val allRecipes = (recipesState as? ApiResult.Success)?.data ?: emptyList()

    // Retsept tanlash bottom sheet
    val activeRecipes = allRecipes.filter { it.isActive }
    if (selectedSlot != null) {
        RecipePickerSheet(
            recipes   = activeRecipes,
            onDismiss = { selectedSlot = null },
            onSelect  = { recipe ->
                val (dateKey, mealType) = selectedSlot!!
                vm.createMenuEntry(recipe.id, mealType, dateKey)
                selectedSlot = null
            }
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Menyu kalendari", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                // Hafta navigatsiyasi
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = { vm.prevWeek() }) {
                        Icon(Icons.Default.ChevronLeft, null, tint = Teal10)
                    }
                    Text(
                        weekLabel.ifEmpty { "Joriy hafta" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    TextButton(onClick = { vm.goCurrentWeek() }) { Text("Bugun", color = Teal10, fontSize = 12.sp) }
                    IconButton(onClick = { vm.nextWeek() }) {
                        Icon(Icons.Default.ChevronRight, null, tint = Teal10)
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        }

        when (menuState) {
            is ApiResult.Loading -> {
                item {
                    Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Teal10)
                    }
                }
            }
            is ApiResult.Error -> {
                item {
                    Column(Modifier.padding(16.dp)) {
                        Text((menuState as ApiResult.Error).message, color = Red10, fontSize = 13.sp)
                        TextButton(onClick = { vm.loadMenuCalendar() }) { Text("Qayta urinish", color = Teal10) }
                    }
                }
            }
            else -> {
                items(days) { day ->
                    WeekDayMenuCard(
                        day      = day,
                        onSlotClick = { mealType -> selectedSlot = Pair(day.dateKey, mealType) },
                        onDeleteMeal = { mealId -> vm.deleteMenuEntry(mealId) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekDayMenuCard(
    day: MenuDayUi,
    onSlotClick: (mealType: String) -> Unit,
    onDeleteMeal: (mealId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val mealSlots = listOf(
        "BREAKFAST" to "Nonushta",
        "LUNCH"     to "Tushlik",
        "DINNER"    to "Kechki ovqat",
        "SNACK"     to "Kechki tamaddi"
    )
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (day.isToday) TealContainer else Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (day.isToday) 1.dp else 0.5.dp, if (day.isToday) Teal10.copy(0.5f) else Outline),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    day.dayLabel, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = if (day.isToday) Teal10 else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (day.isToday) StatusChip("Bugun", Teal10, TealContainer)
            }
            Spacer(Modifier.height(8.dp))
            mealSlots.forEach { (mealType, label) ->
                val meal = day.meals.find { it.mealType == mealType }
                DayMealSlot(
                    mealName   = label,
                    recipeName = meal?.recipeName,
                    mealId     = meal?.id,
                    onClick    = { onSlotClick(mealType) },
                    onDelete   = { mealId -> onDeleteMeal(mealId) }
                )
            }
        }
    }
}

@Composable
private fun DayMealSlot(
    mealName: String,
    recipeName: String?,
    mealId: String?,
    onClick: () -> Unit,
    onDelete: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(0.5.dp, if (recipeName != null) Teal10.copy(0.3f) else Outline, RoundedCornerShape(8.dp))
            .background(if (recipeName != null) TealContainer.copy(0.4f) else Color.Transparent)
            .clickable { if (recipeName == null) onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (recipeName != null) {
            Icon(Icons.Default.CheckCircle, null, tint = Teal10, modifier = Modifier.size(16.dp))
            Column(Modifier.weight(1f)) {
                Text(recipeName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Teal10)
                Text(mealName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (mealId != null) {
                IconButton(
                    onClick = { onDelete(mealId) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Close, null, tint = Red10, modifier = Modifier.size(14.dp))
                }
            }
        } else {
            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Text(mealName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text("Belgilash", fontSize = 11.sp, color = Teal10)
        }
    }
}

// ─────────────────────────────────────────────
// 6. STOCK HARAKATLARI
// ─────────────────────────────────────────────

@Composable
fun ChefStockMovementsScreen(vm: ChefViewModel) {
    val movementsState by vm.movementsState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<MovementType?>(null) }

    LaunchedEffect(Unit) { vm.loadMovements() }

    val allMovements = (movementsState as? ApiResult.Success)?.data ?: emptyList()
    val filtered = allMovements.filter { mv ->
        mv.ingredient.contains(searchQuery, ignoreCase = true) &&
                (selectedType == null || mv.type == selectedType)
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Column(Modifier.padding(16.dp)) {
                SectionHeader("Ombor harakatlari") {}
                Text(
                    "Har bir kirim, chiqim, iste'mol va qo'lda tuzatish jumalini audit izi sifatida ko'ring.",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp
                )
                Spacer(Modifier.height(12.dp))

                when (movementsState) {
                    is ApiResult.Loading -> {
                        Box(Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Amber10, modifier = Modifier.size(24.dp))
                        }
                        return@Column
                    }
                    is ApiResult.Error -> {
                        Text((movementsState as ApiResult.Error).message, color = Red10, fontSize = 13.sp)
                        TextButton(onClick = { vm.loadMovements() }) { Text("Qayta urinish", color = Amber10) }
                        return@Column
                    }
                    else -> {}
                }

                OutlinedTextField(
                    value = searchQuery, onValueChange = { searchQuery = it },
                    placeholder = { Text("Harakat, sabab yoki ingredient qidirish", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Teal10, unfocusedBorderColor = Outline)
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(null to "Barchasi", MovementType.KIRIM to "Kirim",
                        MovementType.CHIQIM to "Chiqim", MovementType.TUZATISH to "Tuzatish")
                        .forEach { (type, label) ->
                            val sel = selectedType == type
                            FilterChip(
                                selected = sel, onClick = { selectedType = type },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (type) {
                                        MovementType.KIRIM -> TealContainer; MovementType.CHIQIM -> RedContainer
                                        MovementType.TUZATISH -> AmberContainer; null -> MaterialTheme.colorScheme.primary
                                    },
                                    selectedLabelColor = when (type) {
                                        MovementType.KIRIM -> Teal10; MovementType.CHIQIM -> Red10
                                        MovementType.TUZATISH -> Amber10; null -> MaterialTheme.colorScheme.onPrimary
                                    }
                                ),
                                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = sel, borderColor = Outline, selectedBorderColor = Color.Transparent)
                            )
                        }
                }
                Spacer(Modifier.height(8.dp))
                Text("${filtered.size} Harakatlar", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
            }
        }

        if (movementsState is ApiResult.Success) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("Sana", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.2f))
                    Text("Ingredient", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.5f))
                    Text("Tur", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.8f))
                    Text("Miqdor", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }
            }

            itemsIndexed(filtered) { idx, mv ->
                val (color, container) = when (mv.type) {
                    MovementType.KIRIM -> Pair(Teal10, TealContainer)
                    MovementType.CHIQIM -> Pair(Red10, RedContainer)
                    MovementType.TUZATISH -> Pair(Amber10, AmberContainer)
                }
                val amountText = when (mv.type) {
                    MovementType.KIRIM -> "+${mv.amount.toLong()} ${mv.unit}"
                    MovementType.CHIQIM -> "-${mv.amount.toLong()} ${mv.unit}"
                    MovementType.TUZATISH -> "~${mv.amount.toLong()} ${mv.unit}"
                }
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        .let { if (idx == filtered.lastIndex) it.clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)) else it }
                        .background(Color.White).border(0.5.dp, Outline)
                ) {
                    if (idx > 0) HorizontalDivider(color = Outline, thickness = 0.5.dp)
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1.2f)) {
                            Text(mv.date, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text(mv.time, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(Modifier.weight(1.5f)) {
                            Text(mv.ingredient, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(mv.unit, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Box(Modifier.weight(0.8f)) { StatusChip(mv.type.name, color, container) }
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text(amountText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = color)
                            Text("${mv.prevQty.toLong()} → ${mv.newQty.toLong()} ${mv.unit}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SwapVert, null, tint = Outline, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Harakatlar topilmadi", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 7. KAFETERIYA ANALITIKASI
// ─────────────────────────────────────────────

@Composable
fun ChefAnalyticsScreen() {
    val topIngredients = listOf(
        Triple("Kartoshka", "250 kg", 85),
        Triple("Guruch", "180 kg", 61),
        Triple("Piyoz", "120 kg", 41),
        Triple("Go'sht", "95 kg", 32),
        Triple("Qovoq", "60 kg", 20)
    )

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Column(Modifier.padding(16.dp)) {
                SectionHeader("Oshxona analitikasi") {}
                Text(
                    "Eng ko'p ishlatilaigan ingredientlar, sarf trendlari, ombor ishlatilishi va tasdiq navbatini tahlil qiling.",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp
                )
                Spacer(Modifier.height(14.dp))
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, Outline),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Sarf trendi", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text("Oxirgi 7 kun", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    val days = listOf("Dush", "Sesh", "Chor", "Pay", "Jum", "Shan", "Yak")
                    val values = listOf(0.6f, 0.8f, 0.5f, 0.9f, 0.7f, 0.4f, 0.3f)
                    Row(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        days.zip(values).forEach { (day, value) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(modifier = Modifier.width(28.dp).height((100 * value).dp)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(Teal10.copy(if (day == "Pay") 1f else 0.5f)))
                                Spacer(Modifier.height(4.dp))
                                Text(day, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(12.dp)) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, Outline),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Ombor ishlatilishi", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                            Box(Modifier.size(120.dp).clip(CircleShape).background(Teal10))
                            Box(Modifier.size(70.dp).clip(CircleShape).background(Color.White))
                            Text("75%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Teal10)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            LegendItem("Kirim", "75%", Teal10)
                            LegendItem("Chiqim", "20%", Red10)
                            LegendItem("Qoldiq", "5%", Amber10)
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(12.dp)) }

        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Eng ko'p ishlatilgan ingredientlar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, Outline),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column {
                    Row(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 8.dp).fillMaxWidth()) {
                        Text("Ingredient", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Text("Miqdor", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                        Text("Harakatlar", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    }
                    topIngredients.forEachIndexed { idx, (name, qty, pct) ->
                        if (idx > 0) HorizontalDivider(color = Outline, thickness = 0.5.dp)
                        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(name, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                Text(qty, fontSize = 13.sp, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$pct harakatlar", fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(6.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                                Box(modifier = Modifier.fillMaxWidth(pct / 100f).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(Teal10))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// SHARED HELPERS
// ─────────────────────────────────────────────

@Composable
private fun MovementRow(mv: StockMovement, modifier: Modifier = Modifier) {
    val (color, container) = when (mv.type) {
        MovementType.KIRIM -> Pair(Teal10, TealContainer)
        MovementType.CHIQIM -> Pair(Red10, RedContainer)
        MovementType.TUZATISH -> Pair(Amber10, AmberContainer)
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, Outline),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(container),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (mv.type) { MovementType.KIRIM -> Icons.Default.ArrowDownward; MovementType.CHIQIM -> Icons.Default.ArrowUpward; else -> Icons.Default.SwapVert },
                    null, tint = color, modifier = Modifier.size(18.dp)
                )
            }
            Column(Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(mv.ingredient, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    StatusChip(mv.type.name, color, container)
                }
                Text("${mv.date} · ${mv.time} · ${mv.reason}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                val sign = when (mv.type) { MovementType.KIRIM -> "+"; MovementType.CHIQIM -> "-"; else -> "~" }
                Text("$sign${mv.amount.toLong()} ${mv.unit}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = color)
                Text("${mv.prevQty.toLong()} → ${mv.newQty.toLong()}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(60.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ─────────────────────────────────────────────
// YANGI INGREDIENT SHEET
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientSheet(
    onDismiss: () -> Unit,
    onSave: (com.maktab.app.network.models.IngredientRequest, Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var name     by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var minQty   by remember { mutableStateOf("") }
    var unit     by remember { mutableStateOf("kg") }
    var expiry   by remember { mutableStateOf("") }
    var error    by remember { mutableStateOf("") }

    val units      = listOf("kg", "g", "litr", "ml", "dona")
    val categories = listOf("Sabzavot", "Don mahsulot", "Go'sht mahsulot", "Sut mahsulot", "Meva", "Boshqa")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        windowInsets = WindowInsets.navigationBars
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(bottom = 32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(TealContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Add, null, tint = Teal10, modifier = Modifier.size(22.dp))
                }
                Text("Yangi ingredient qo'shish", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Outline, thickness = 0.5.dp)
            Spacer(Modifier.height(16.dp))

            SheetField("Ingredient nomi *", name, { name = it }, Icons.Default.Label, "Masalan: Kartoshka")
            Spacer(Modifier.height(12.dp))

            Text("Kategoriya", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(selected = category == cat, onClick = { category = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Teal10, selectedLabelColor = Color.White),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = category == cat, borderColor = Outline, selectedBorderColor = Color.Transparent))
                }
            }
            Spacer(Modifier.height(12.dp))

            Text("O'lchov birligi", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                units.forEach { u ->
                    FilterChip(selected = unit == u, onClick = { unit = u },
                        label = { Text(u, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Teal10, selectedLabelColor = Color.White),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = unit == u, borderColor = Outline, selectedBorderColor = Color.Transparent))
                }
            }
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    SheetField("Boshlang'ich miqdor", quantity, { quantity = it.filter { c -> c.isDigit() || c == '.' } }, Icons.Default.Inventory, "0 (ixtiyoriy)", KeyboardType.Number)
                }
                Column(Modifier.weight(1f)) {
                    SheetField("Min. miqdor", minQty, { minQty = it.filter { c -> c.isDigit() || c == '.' } }, Icons.Default.Warning, "0", KeyboardType.Number)
                }
            }
            Spacer(Modifier.height(12.dp))
            DatePickerField("Yaroqlilik muddati", expiry, { expiry = it })

            if (error.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(error, color = Red10, fontSize = 13.sp)
            }
            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onDismiss, modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp), border = BorderStroke(0.5.dp, Outline)
                ) { Text("Bekor qilish") }
                Button(
                    onClick = {
                        if (name.isBlank()) { error = "Ingredient nomi majburiy!"; return@Button }
                        error = ""
                        val initialStock = quantity.toDoubleOrNull() ?: 0.0
                        onSave(com.maktab.app.network.models.IngredientRequest(
                            name       = name.trim(),
                            category   = category.ifEmpty { null },
                            unit       = unit,
                            quantity   = 0.0,      // backend har doim 0 dan boshlaydi
                            minQuantity = minQty.toDoubleOrNull(),
                            expiryDate = expiry.ifEmpty { null }
                        ), initialStock)
                        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal10),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Saqlash")
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// RETSEPT TANLASH SHEET (menyu kalendarida)
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipePickerSheet(
    recipes: List<com.maktab.app.viewmodel.Recipe>,
    onDismiss: () -> Unit,
    onSelect: (com.maktab.app.viewmodel.Recipe) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var search by remember { mutableStateOf("") }
    val filtered = recipes.filter { it.name.contains(search, ignoreCase = true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        windowInsets = WindowInsets.navigationBars
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 32.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(TealContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.RestaurantMenu, null, tint = Teal10, modifier = Modifier.size(20.dp))
                }
                Text("Taom tanlang", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = search, onValueChange = { search = it },
                placeholder = { Text("Retsept qidirish", fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(10.dp),
                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Teal10, unfocusedBorderColor = Outline)
            )
            Spacer(Modifier.height(8.dp))
            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    Text("Retseptlar topilmadi", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 4.dp)) {
                    items(filtered) { recipe ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onSelect(recipe) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(TealContainer), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.RestaurantMenu, null, tint = Teal10, modifier = Modifier.size(20.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(recipe.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text("${recipe.category} · ${recipe.portionCount} porsiya", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        HorizontalDivider(color = Outline, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}