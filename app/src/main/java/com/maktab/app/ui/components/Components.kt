package com.maktab.app.ui.components
import androidx.compose.foundation.background; import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape; import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*; import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment; import androidx.compose.ui.Modifier; import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color; import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp; import androidx.compose.ui.unit.dp; import androidx.compose.ui.unit.sp
import com.maktab.app.ui.theme.*

@Composable fun AvatarCircle(initials: String, color: Color = Teal10, size: Dp = 40.dp) {
    Box(Modifier.size(size).clip(CircleShape).background(color.copy(0.15f)), Alignment.Center) { Text(initials, color = color, fontSize = (size.value * 0.35f).sp, fontWeight = FontWeight.Medium) }
}
@Composable fun StatusChip(text: String, textColor: Color, bgColor: Color, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(4.dp)).background(bgColor).padding(horizontal = 8.dp, vertical = 2.dp)) { Text(text, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Medium) }
}
@Composable fun StatCard(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(10.dp), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.padding(12.dp)) { Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(2.dp)); Text(value, fontSize = 22.sp, fontWeight = FontWeight.Medium, color = valueColor) }
    }
}
@Composable fun SectionHeader(title: String, modifier: Modifier = Modifier, action: @Composable (() -> Unit)? = null) {
    Row(modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) { Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium); action?.invoke() }
}
@Composable fun AppCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(0.dp), border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)) { Column(Modifier.padding(14.dp), content = content) }
}
@Composable fun AttendanceDot(isPresent: Boolean, onClick: () -> Unit) {
    val bg = if (isPresent) TealContainer else RedContainer; val color = if (isPresent) Teal10 else Red10
    Surface(onClick = onClick, modifier = Modifier.size(30.dp), shape = CircleShape, color = bg) { Box(contentAlignment = Alignment.Center) { Text(if (isPresent) "✓" else "✗", color = color, fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center) } }
}
