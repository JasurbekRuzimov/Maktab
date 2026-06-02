package com.maktab.app.ui.screens

import androidx.compose.foundation.*
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
import com.maktab.app.ui.components.*
import com.maktab.app.ui.theme.*
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────
// DATA MODELS
// ─────────────────────────────────────────────

data class MealSlot(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val subtitle: String,
    val meal: String? = null,
    val status: MealStatus = MealStatus.EMPTY
)

enum class MealStatus { EMPTY, ASSIGNED, CONFIRMED }

data class IngredientItem(
    val id: Int,
    val name: String,
    val category: String,
    val quantity: Double,
    val unit: String,
    val minQuantity: Double,
    val expiryDate: String,
    val status: StockStatus
)

enum class StockStatus { YETARLI, KAM, TUGAGAN }

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

enum class MovementType { KIRIM, CHIQIM, TUZATISH }

data class Recipe(
    val id: Int,
    val name: String,
    val category: String,
    val portionCount: Int,
    val isActive: Boolean,
    val ingredientCount: Int
)

// ─────────────────────────────────────────────
// MOCK DATA
// ─────────────────────────────────────────────

private object ChefMock {
    val todaySlots = listOf(
        MealSlot("nonushta", "Nonushta", Icons.Default.WbSunny, "Kun boshidagi issiq ovqat", null, MealStatus.EMPTY),
        MealSlot("tushlik", "Tushlik", Icons.Default.LunchDining, "Asosiy tushlik menyusi", null, MealStatus.EMPTY),
        MealSlot("peshinlik", "Peshinlik", Icons.Default.Coffee, "Peshin payti beriladigan ovqat", null, MealStatus.EMPTY),
        MealSlot("kechki", "Kechki ovqat", Icons.Default.DinnerDining, "Kechki navbat uchun taom", null, MealStatus.EMPTY)
    )
    val weekDays = listOf("Dush 1", "Sesh 2", "Chor 3", "Pay 4", "Jum 5", "Shan 6", "Yak 7")
    val mealTimes = listOf("Nonushta", "Tushlik", "Kechki ovqat", "Kechki tamaddi")

    val ingredients = listOf(
        IngredientItem(1, "Kartoshka", "Sabzavot", 100.0, "kg", 10.0, "29.05.2026", StockStatus.YETARLI),
        IngredientItem(2, "Piyoz", "Sabzavot", 5.0, "kg", 8.0, "01.06.2026", StockStatus.KAM),
        IngredientItem(3, "Guruch", "Don mahsulot", 50.0, "kg", 20.0, "31.12.2026", StockStatus.YETARLI),
        IngredientItem(4, "Go'sht", "Et mahsulot", 0.0, "kg", 15.0, "30.05.2026", StockStatus.TUGAGAN),
        IngredientItem(5, "Qovoq", "Sabzavot", 30.0, "kg", 5.0, "03.06.2026", StockStatus.YETARLI),
        IngredientItem(6, "Sut", "Sut mahsulot", 3.0, "l", 10.0, "31.05.2026", StockStatus.KAM)
    )

    val movements = listOf(
        StockMovement("May 29, 2026", "7:17 PM", "Kartoshka", "KG", MovementType.KIRIM, 100.0, 0.0, 100.0, "Keldi"),
        StockMovement("May 29, 2026", "8:00 AM", "Piyoz", "KG", MovementType.CHIQIM, 3.0, 8.0, 5.0, "Ishlatildi"),
        StockMovement("May 28, 2026", "6:30 PM", "Guruch", "KG", MovementType.KIRIM, 50.0, 0.0, 50.0, "Keldi"),
        StockMovement("May 28, 2026", "12:00 PM", "Go'sht", "KG", MovementType.CHIQIM, 15.0, 15.0, 0.0, "Tushlik uchun"),
        StockMovement("May 27, 2026", "9:00 AM", "Sut", "L", MovementType.TUZATISH, 3.0, 0.0, 3.0, "Inventarizatsiya")
    )

    val recipes = listOf(
        Recipe(1, "Osh (Palov)", "Asosiy taom", 100, true, 7),
        Recipe(2, "Sho'rva", "Sho'rva", 80, true, 6),
        Recipe(3, "Moshxo'rda", "Sho'rva", 60, false, 5),
        Recipe(4, "Qovoq bo'g'irsoq", "Xamirli", 50, true, 4),
        Recipe(5, "Mastava", "Sho'rva", 70, false, 8)
    )
}

// ─────────────────────────────────────────────
// 1. OSHXONA DASHBOARDI
// ─────────────────────────────────────────────

@Composable
fun ChefDashboardScreen() {
    val tabs = listOf("Bugun", "Ertaga", "Kecha")
    var selectedTab by remember { mutableStateOf(0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Summary stat cards
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
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ChefStatCard("Bugungi taomlar", "0", "Bugun rejalashtirilgan", Blue10, BlueContainer, Modifier.weight(1f))
                    ChefStatCard("Tasdiq navbati", "0", "Bugun tasdiq kutilmoqda", Amber10, AmberContainer, Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ChefStatCard("Ertangi taomlar", "0", "Ertaga rejalashtirilgan", Teal10, TealContainer, Modifier.weight(1f))
                    ChefStatCard("Ombor eslatmalari", "0", "Kam qolgan mahsulotlar", Red10, RedContainer, Modifier.weight(1f))
                }
            }
        }

        // Tab selector
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
                            enabled = true,
                            selected = selected,
                            borderColor = Outline,
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Meal slots
        item {
            val dayLabel = when (selectedTab) {
                0 -> "Bugungi tayyorlangan taomlar"
                1 -> "Ertangi tayyorgarlik"
                else -> "Kechagi taomlar"
            }
            Text(
                dayLabel,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(10.dp))
        }

        items(ChefMock.todaySlots) { slot ->
            MealSlotCard(slot, modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp))
        }

        // Warehouse alerts
        item {
            Spacer(Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
                        Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Red10.copy(0.15f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Warning, null, tint = Red10, modifier = Modifier.size(20.dp)) }
                    Column(Modifier.weight(1f)) {
                        Text("Ombor ogohlantirishlari", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Red10)
                        Text("2 ta mahsulot kam qolgan", fontSize = 12.sp, color = Red10.copy(0.7f))
                    }
                    Icon(Icons.Default.ArrowForward, null, tint = Red10, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun MealSlotCard(slot: MealSlot, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(AmberContainer),
                contentAlignment = Alignment.Center
            ) { Icon(slot.icon, null, tint = Amber10, modifier = Modifier.size(22.dp)) }
            Column(Modifier.weight(1f)) {
                Text(slot.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(slot.subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                if (slot.meal != null) {
                    StatusChip(slot.meal, Teal10, TealContainer)
                } else {
                    Text(
                        "Taom belgilanmagan",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .border(0.5.dp, Outline, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
            Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 15.sp)
        }
    }
}

// ─────────────────────────────────────────────
// 2. OZIQ-OVQAT OMBORI
// ─────────────────────────────────────────────

@Composable
fun ChefOmborScreen() {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = ChefMock.ingredients.filter {
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChefStatCard("Ingredientlar", "${filtered.size}", "Jami", Blue10, BlueContainer, Modifier.weight(1f))
                    ChefStatCard("Ombor ogoh.", "$kam", "Kam qolgan", Amber10, AmberContainer, Modifier.weight(1f))
                    ChefStatCard("Tugagan", "$tugagan", "Zaxira yo'q", Red10, RedContainer, Modifier.weight(1f))
                }
            }
        }

        // Ingredient katalogi
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Ingredient katalogi", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Button(
                        onClick = {},
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
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Teal10,
                        unfocusedBorderColor = Outline
                    )
                )
                Spacer(Modifier.height(8.dp))
                // Filter chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip("$yetarli Yetarli", Teal10, TealContainer)
                    StatusChip("$kam Kam qolgan", Amber10, AmberContainer)
                    StatusChip("$tugagan Tugagan", Red10, RedContainer)
                }
                Spacer(Modifier.height(12.dp))
            }
        }

        // Ingredient list
        items(filtered) { ing ->
            IngredientRow(ing, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }

        // So'nggi harakatlar
        item {
            Spacer(Modifier.height(16.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("So'nggi ombor harakatlari", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
            }
        }

        items(ChefMock.movements.take(3)) { mv ->
            MovementRow(mv, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientRow(ing: IngredientItem, modifier: Modifier = Modifier) {
    val (color, container) = when (ing.status) {
        StockStatus.YETARLI -> Pair(Teal10, TealContainer)
        StockStatus.KAM     -> Pair(Amber10, AmberContainer)
        StockStatus.TUGAGAN -> Pair(Red10, RedContainer)
    }

    // Edit sheet holati
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Tahrirlash maydonlari
    var editName     by remember { mutableStateOf(ing.name) }
    var editCategory by remember { mutableStateOf(ing.category) }
    var editQuantity by remember { mutableStateOf(ing.quantity.toInt().toString()) }
    var editMin      by remember { mutableStateOf(ing.minQuantity.toInt().toString()) }
    var editUnit     by remember { mutableStateOf(ing.unit) }
    var editExpiry   by remember { mutableStateOf(ing.expiryDate) }
    var saved        by remember { mutableStateOf(false) }
    val scope        = rememberCoroutineScope()

    val units = listOf("kg", "g", "l", "ml", "dona", "litr")
    val categories = listOf("Sabzavot", "Don mahsulot", "Go'sht mahsulot", "Sut mahsulot", "Meva", "Boshqa")

    // ── CARD ──
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
                Modifier.size(42.dp).clip(RoundedCornerShape(8.dp)).background(AmberContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Inventory, null, tint = Amber10, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(editName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    StatusChip(ing.status.name, color, container)
                }
                Spacer(Modifier.height(2.dp))
                Text("$editCategory · min: $editMin $editUnit",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Tugash: $editExpiry",
                    fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text(
                    "$editQuantity $editUnit",
                    fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color
                )
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showSheet = true }
                        .background(Blue10.copy(0.09f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, "Tahrirlash", tint = Blue10, modifier = Modifier.size(20.dp))
                }
            }
        }
    }

    // ── BOTTOM SHEET ──
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            windowInsets = WindowInsets.navigationBars
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(AmberContainer),
                        contentAlignment = Alignment.Center
                    ) {
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

                // Nom
                SheetField(
                    label = "Ingredient nomi",
                    value = editName,
                    onValueChange = { editName = it },
                    icon = Icons.Default.Label,
                    placeholder = "Masalan: Kartoshka"
                )
                Spacer(Modifier.height(14.dp))

                // Kategoriya
                Text("Kategoriya", fontSize = 13.sp, fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSel = editCategory == cat
                        FilterChip(
                            selected = isSel,
                            onClick = { editCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Amber10,
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true, selected = isSel,
                                borderColor = Outline, selectedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))

                // O'lchov birligi
                Text("O'lchov birligi", fontSize = 13.sp, fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    units.forEach { u ->
                        val isSel = editUnit == u
                        FilterChip(
                            selected = isSel,
                            onClick = { editUnit = u },
                            label = { Text(u, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Amber10,
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true, selected = isSel,
                                borderColor = Outline, selectedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))

                // Miqdor va Minimal miqdor — ikki ustun
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        SheetField(
                            label = "Joriy miqdor",
                            value = editQuantity,
                            onValueChange = { editQuantity = it.filter { c -> c.isDigit() } },
                            icon = Icons.Default.Inventory,
                            placeholder = "0",
                            keyboardType = KeyboardType.Number
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        SheetField(
                            label = "Minimal miqdor",
                            value = editMin,
                            onValueChange = { editMin = it.filter { c -> c.isDigit() } },
                            icon = Icons.Default.Warning,
                            placeholder = "0",
                            keyboardType = KeyboardType.Number
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))

                // Yaroqlilik muddati
                SheetField(
                    label = "Yaroqlilik muddati",
                    value = editExpiry,
                    onValueChange = { editExpiry = it },
                    icon = Icons.Default.CalendarToday,
                    placeholder = "DD.MM.YYYY"
                )
                Spacer(Modifier.height(24.dp))

                // Saqlash tugmasi
                if (saved) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(TealContainer)
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Teal10, modifier = Modifier.size(20.dp))
                        Text("Muvaffaqiyatli saqlandi!", fontSize = 14.sp, color = Teal10, fontWeight = FontWeight.Medium)
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(0.5.dp, Outline)
                        ) {
                            Text("Bekor qilish", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Button(
                            onClick = {
                                saved = true
                                scope.launch {
                                    kotlinx.coroutines.delay(1500)
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
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            placeholder = { Text(placeholder, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingIcon = { Icon(icon, null, tint = Amber10, modifier = Modifier.size(18.dp)) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Amber10,
                unfocusedBorderColor = Outline
            )
        )
    }
}

// ─────────────────────────────────────────────
// 3. INGREDIENTLAR BOSHQARUVI
// ─────────────────────────────────────────────

@Composable
fun ChefIngredientsScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Barchasi") }
    val filters = listOf("Barchasi", "Sabzavot", "Don mahsulot", "Et mahsulot", "Sut mahsulot")
    val filtered = ChefMock.ingredients.filter { ing ->
        (selectedFilter == "Barchasi" || ing.category == selectedFilter) &&
                ing.name.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Column(Modifier.padding(16.dp)) {
                SectionHeader("Ingredient boshqaruvi") {
                    Button(
                        onClick = {},
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
                    "Ingredient kartalari, o'lchov birliklari, rasm yuklash, minimal qoldiq va yaroqlilik muddatlarini yuriting.",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp
                )
                Spacer(Modifier.height(12.dp))
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
                // Summary chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip("${ChefMock.ingredients.size} Ingredientlar", Blue10, BlueContainer)
                    StatusChip("${ChefMock.ingredients.count { it.status == StockStatus.KAM }} Kam qolgan", Amber10, AmberContainer)
                    StatusChip("1 Yaroqlilik muddati", Red10, RedContainer)
                }
                Spacer(Modifier.height(10.dp))
                // Category filter scroll
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filters) { f ->
                        FilterChip(
                            selected = selectedFilter == f,
                            onClick = { selectedFilter = f },
                            label = { Text(f, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Teal10,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true, selected = selectedFilter == f,
                                borderColor = Outline, selectedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }

        // Table header
        item {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp)
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
                    .background(Color.White)
                    .border(0.5.dp, Outline)
            ) {
                if (idx > 0) HorizontalDivider(color = Outline, thickness = 0.5.dp)
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1.8f)) {
                        Text(ing.name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(ing.category, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("${ing.minQuantity} ${ing.unit}", fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${ing.quantity} ${ing.unit}", fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = color)
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                        StatusChip(ing.status.name.lowercase().replaceFirstChar { it.uppercase() }, color, container)
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
fun ChefRecipesScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var filterActive by remember { mutableStateOf<Boolean?>(null) }
    val filtered = ChefMock.recipes.filter { r ->
        r.name.contains(searchQuery, ignoreCase = true) &&
                (filterActive == null || r.isActive == filterActive)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Column(Modifier.padding(16.dp)) {
                SectionHeader("Taom retseptlari") {
                    Button(
                        onClick = {},
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Qidirish", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Teal10, unfocusedBorderColor = Outline)
                    )
                    FilterChip(
                        selected = filterActive == true,
                        onClick = { filterActive = if (filterActive == true) null else true },
                        label = { Text("Faol", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(14.dp)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TealContainer, selectedLabelColor = Teal10),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = filterActive == true, borderColor = Outline, selectedBorderColor = Teal10)
                    )
                    FilterChip(
                        selected = filterActive == false,
                        onClick = { filterActive = if (filterActive == false) null else false },
                        label = { Text("Nofaol", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Cancel, null, modifier = Modifier.size(14.dp)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = RedContainer, selectedLabelColor = Red10),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = filterActive == false, borderColor = Outline, selectedBorderColor = Red10)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text("${filtered.size} / ${ChefMock.recipes.size}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
            }
        }

        if (filtered.isEmpty()) {
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

@Composable
private fun RecipeCard(recipe: Recipe, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                    .background(if (recipe.isActive) TealContainer else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.RestaurantMenu, null,
                    tint = if (recipe.isActive) Teal10 else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
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
}

// ─────────────────────────────────────────────
// 5. MENYU KALENDARI
// ─────────────────────────────────────────────

@Composable
fun ChefMenuCalendarScreen() {
    var viewMode by remember { mutableStateOf("hafta") } // hafta / kun / oy
    val viewModes = listOf("Hafta", "Kun", "Oy")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Menyu kalendari", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Row {
                        IconButton(onClick = {}) { Icon(Icons.Default.ChevronLeft, null) }
                        TextButton(onClick = {}) { Text("Bugun", color = Teal10) }
                        IconButton(onClick = {}) { Icon(Icons.Default.ChevronRight, null) }
                    }
                }
                Text("1 - 7-iyun, 2026", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                // View mode selector
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    viewModes.forEach { mode ->
                        val selected = viewMode == mode.lowercase()
                        FilterChip(
                            selected = selected,
                            onClick = { viewMode = mode.lowercase() },
                            label = { Text(mode, fontSize = 13.sp) },
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
                Spacer(Modifier.height(14.dp))
            }
        }

        // Weekly calendar — show days as vertical cards
        items(ChefMock.weekDays) { day ->
            WeekDayMenuCard(
                day = day,
                isToday = day == "Sesh 2",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
            )
        }
    }
}

@Composable
private fun WeekDayMenuCard(day: String, isToday: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (isToday) TealContainer else Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (isToday) 1.dp else 0.5.dp, if (isToday) Teal10.copy(0.5f) else Outline),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    day, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = if (isToday) Teal10 else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (isToday) StatusChip("Bugun", Teal10, TealContainer)
            }
            Spacer(Modifier.height(8.dp))
            ChefMock.mealTimes.forEach { meal ->
                DayMealSlot(meal)
            }
        }
    }
}

@Composable
private fun DayMealSlot(mealName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(0.5.dp, Outline, RoundedCornerShape(8.dp))
            .clickable { }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Default.Add, null, tint = Teal10, modifier = Modifier.size(16.dp))
        Text(mealName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ─────────────────────────────────────────────
// 6. STOCK HARAKATLARI
// ─────────────────────────────────────────────

@Composable
fun ChefStockMovementsScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<MovementType?>(null) }
    val filtered = ChefMock.movements.filter { mv ->
        mv.ingredient.contains(searchQuery, ignoreCase = true) &&
                (selectedType == null || mv.type == selectedType)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Column(Modifier.padding(16.dp)) {
                SectionHeader("Ombor harakatlari") {}
                Text(
                    "Har bir kirim, chiqim, iste'mol va qo'lda tuzatish jumalini audit izi sifatida ko'ring.",
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Harakat, sabab yoki ingredient qidirish", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Teal10, unfocusedBorderColor = Outline)
                )
                Spacer(Modifier.height(8.dp))
                // Type filters
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(null to "Barchasi", MovementType.KIRIM to "Kirim", MovementType.CHIQIM to "Chiqim", MovementType.TUZATISH to "Tuzatish")
                        .forEach { (type, label) ->
                            val sel = selectedType == type
                            FilterChip(
                                selected = sel,
                                onClick = { selectedType = type },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (type) {
                                        MovementType.KIRIM -> TealContainer
                                        MovementType.CHIQIM -> RedContainer
                                        MovementType.TUZATISH -> AmberContainer
                                        null -> MaterialTheme.colorScheme.primary
                                    },
                                    selectedLabelColor = when (type) {
                                        MovementType.KIRIM -> Teal10
                                        MovementType.CHIQIM -> Red10
                                        MovementType.TUZATISH -> Amber10
                                        null -> MaterialTheme.colorScheme.onPrimary
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

        // Table header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth().padding(horizontal = 16.dp)
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
                MovementType.KIRIM -> "+${mv.amount.toInt()} ${mv.unit}"
                MovementType.CHIQIM -> "-${mv.amount.toInt()} ${mv.unit}"
                MovementType.TUZATISH -> "~${mv.amount.toInt()} ${mv.unit}"
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth().padding(horizontal = 16.dp)
                    .let { if (idx == filtered.lastIndex) it.clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)) else it }
                    .background(Color.White)
                    .border(0.5.dp, Outline)
            ) {
                if (idx > 0) HorizontalDivider(color = Outline, thickness = 0.5.dp)
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1.2f)) {
                        Text(mv.date, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text(mv.time, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(Modifier.weight(1.5f)) {
                        Text(mv.ingredient, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(mv.unit, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(Modifier.weight(0.8f)) {
                        StatusChip(mv.type.name, color, container)
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text(amountText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = color)
                        Text("${mv.prevQty.toInt()} → ${mv.newQty.toInt()} ${mv.unit}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
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

        // Sarf trendi chart (simplified bar chart)
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
                                Box(
                                    modifier = Modifier
                                        .width(28.dp)
                                        .height((100 * value).dp)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(Teal10.copy(if (day == "Pay") 1f else 0.5f))
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(day, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Teal10))
                        Text("Consumed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item { Spacer(Modifier.height(12.dp)) }

        // Ombor ishlatilishi donut chart (simplified)
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
                        // Simplified donut visual
                        Box(Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                            Box(
                                Modifier.size(120.dp).clip(CircleShape).background(Teal10)
                            )
                            Box(
                                Modifier.size(70.dp).clip(CircleShape).background(Color.White)
                            )
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

        // Eng ko'p ishlatilgan ingredientlar
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
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .fillMaxWidth()
                    ) {
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
                            Box(
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(pct / 100f).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(Teal10)
                                )
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
                Text("$sign${mv.amount.toInt()} ${mv.unit}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = color)
                Text("${mv.prevQty.toInt()} → ${mv.newQty.toInt()}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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