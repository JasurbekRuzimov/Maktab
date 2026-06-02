package com.maktab.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maktab.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val PIN_LENGTH = 4

// ══════════════════════════════════════════════════════════════
//  PIN O'RNATISH EKRANI  (birinchi logindan keyin)
// ══════════════════════════════════════════════════════════════
@Composable
fun PinSetupScreen(
    role: String,
    onPinSet: (String) -> Unit
) {
    val isTeacher = role == "teacher"
    val accent = if (isTeacher) Teal10 else Blue10

    var step by remember { mutableStateOf(0) }   // 0=kiriting, 1=tasdiqlang
    var firstPin by remember { mutableStateOf("") }
    var currentPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    val title = if (step == 0) "PIN kod o'rnating" else "PIN kodni tasdiqlang"
    val subtitle = if (step == 0) "4 raqamli PIN kod kiriting" else "Tasdiqlash uchun PIN kodni qayta kiriting"

    fun onDigit(d: String) {
        if (currentPin.length >= PIN_LENGTH) return
        currentPin += d
        errorMsg = ""
        if (currentPin.length == PIN_LENGTH) {
            if (step == 0) {
                firstPin = currentPin
                currentPin = ""
                step = 1
            } else {
                if (currentPin == firstPin) {
                    onPinSet(currentPin)
                } else {
                    errorMsg = "PIN kodlar mos kelmadi. Qayta urinib ko'ring"
                    currentPin = ""
                    firstPin = ""
                    step = 0
                }
            }
        }
    }

    PinScaffold(
        accent = accent,
        title = title,
        subtitle = subtitle,
        pin = currentPin,
        errorMsg = errorMsg,
        onDigit = ::onDigit,
        onDelete = { if (currentPin.isNotEmpty()) currentPin = currentPin.dropLast(1) },
        bottomContent = {
            Text(
                text = "PIN kod telefon qulfini ochish uchun ishlatiladi",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    )
}

// ══════════════════════════════════════════════════════════════
//  PIN KIRITISH EKRANI  (har safar ilova ochilganda)
// ══════════════════════════════════════════════════════════════
@Composable
fun PinEntryScreen(
    role: String,
    savedPin: String,
    userName: String = "Karimova Nargiza",
    onSuccess: () -> Unit,
    onForgotPin: () -> Unit
) {
    val isTeacher = role == "teacher"
    val accent = if (isTeacher) Teal10 else Blue10

    var currentPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    var attempts by remember { mutableStateOf(0) }
    var isShaking by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun onDigit(d: String) {
        if (currentPin.length >= PIN_LENGTH) return
        currentPin += d
        errorMsg = ""
        if (currentPin.length == PIN_LENGTH) {
            if (currentPin == savedPin) {
                onSuccess()
            } else {
                attempts++
                isShaking = true
                errorMsg = if (attempts >= 3)
                    "Juda ko'p xato urinish. Qayta kiring."
                else
                    "Noto'g'ri PIN. ${3 - attempts} urinish qoldi"
                scope.launch { delay(500); isShaking = false; delay(100); currentPin = "" }
            }
        }
    }

    PinScaffold(
        accent = accent,
        title = "PIN kod kiriting",
        subtitle = userName,
        pin = currentPin,
        errorMsg = errorMsg,
        isShaking = isShaking,
        onDigit = ::onDigit,
        onDelete = { if (currentPin.isNotEmpty()) currentPin = currentPin.dropLast(1) },
        topContent = {
            // Avatar
            Box(
                modifier = Modifier.size(60.dp).clip(CircleShape).background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (role) {
                        "teacher" -> Icons.Default.School
                        "chef"    -> Icons.Default.Restaurant
                        else      -> Icons.Default.FamilyRestroom
                    },
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
        },
        bottomContent = {
            TextButton(onClick = onForgotPin) {
                Text("PIN kodni unutdim", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

// ══════════════════════════════════════════════════════════════
//  UMUMIY PIN SCAFFOLD
// ══════════════════════════════════════════════════════════════
@Composable
private fun PinScaffold(
    accent: Color,
    title: String,
    subtitle: String,
    pin: String,
    errorMsg: String,
    isShaking: Boolean = false,
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    topContent: (@Composable () -> Unit)? = null,
    bottomContent: (@Composable () -> Unit)? = null
) {
    val shakeOffset by animateFloatAsState(
        targetValue = if (isShaking) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.2f, stiffness = Spring.StiffnessHigh),
        label = "shake"
    )

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
        ) {
            Spacer(Modifier.height(48.dp))

            topContent?.invoke()

            Text(title, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(36.dp))

            // PIN nuqtalar
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.offset(x = (shakeOffset * 8).dp)
            ) {
                repeat(PIN_LENGTH) { i ->
                    val filled = i < pin.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (filled) accent
                                else if (errorMsg.isNotEmpty()) Red10.copy(alpha = 0.4f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }

            // Xato xabar
            Spacer(Modifier.height(12.dp))
            Box(modifier = Modifier.height(20.dp)) {
                if (errorMsg.isNotEmpty()) {
                    Text(errorMsg, fontSize = 12.sp, color = Red10, textAlign = TextAlign.Center)
                }
            }

            Spacer(Modifier.height(36.dp))

            // PIN klaviatura
            PinKeypad(accent = accent, onDigit = onDigit, onDelete = onDelete)

            Spacer(Modifier.height(28.dp))

            bottomContent?.invoke()
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  PIN KLAVIATURA
// ══════════════════════════════════════════════════════════════
@Composable
private fun PinKeypad(accent: Color, onDigit: (String) -> Unit, onDelete: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "⌫")
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    if (key.isEmpty()) {
                        Spacer(Modifier.size(72.dp))
                    } else {
                        PinKeyButton(
                            label = key,
                            accent = accent,
                            isDelete = key == "⌫",
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (key == "⌫") onDelete() else onDigit(key)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.PinKeyButton(
    label: String,
    accent: Color,
    isDelete: Boolean,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessHigh),
        label = "keyScale"
    )

    Surface(
        onClick = {
            pressed = true
            onClick()
        },
        modifier = Modifier.weight(1f).aspectRatio(1.25f).scale(scale),
        shape = RoundedCornerShape(16.dp),
        color = if (isDelete) MaterialTheme.colorScheme.surfaceVariant
        else accent.copy(alpha = 0.09f),
        tonalElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isDelete) {
                Icon(
                    Icons.Default.Backspace,
                    contentDescription = "O'chirish",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = label,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    LaunchedEffect(pressed) {
        if (pressed) { delay(80); pressed = false }
    }
}