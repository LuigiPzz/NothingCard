package com.nothing.card.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.card.ui.theme.NothingBlack
import com.nothing.card.ui.theme.NothingBorder
import com.nothing.card.ui.theme.NothingRed
import com.nothing.card.ui.theme.NothingWhite
import com.nothing.card.ui.theme.SpaceMonoFamily
import com.nothing.card.ui.theme.NothingSerifFamily
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontFamily = NothingSerifFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        color = NothingWhite.copy(alpha = 0.6f),
        modifier = Modifier.padding(start = 24.dp, bottom = 8.dp, top = 16.dp)
    )
}

@Composable
fun SettingsGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = NothingWhite.copy(alpha = 0.05f),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isLast: Boolean = false,
    statusText: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = NothingWhite,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = NothingWhite.copy(alpha = 0.5f)
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (statusText != null) {
                    Text(
                        text = statusText,
                        fontFamily = SpaceMonoFamily,
                        fontSize = 12.sp,
                        color = NothingWhite.copy(alpha = 0.4f),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = NothingWhite.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
    if (!isLast) {
        androidx.compose.material3.HorizontalDivider(
            color = NothingWhite.copy(alpha = 0.05f), 
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun NothingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = true
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (isPrimary) NothingWhite else NothingBlack)
            .border(
                BorderStroke(1.dp, if (isPrimary) NothingWhite else NothingBorder),
                RoundedCornerShape(24.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(
                color = if (isPrimary) NothingBlack else NothingWhite,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun NothingCardItem(
    title: String,
    subtitle: String,
    colorHex: String,
    isFavorite: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = remember(colorHex) {
        try { 
            val fullColor = if (colorHex.startsWith("#")) colorHex else "#$colorHex"
            Color(android.graphics.Color.parseColor(fullColor)) 
        }
        catch (e: Exception) { NothingWhite.copy(alpha = 0.1f) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NothingBlack)
            .border(BorderStroke(1.dp, NothingBorder), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = NothingSerifFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = NothingWhite
                    )
                    if (isFavorite) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorite",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontFamily = SpaceMonoFamily,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            // Thumbnail Card 2.0
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .aspectRatio(1.58f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(cardColor.copy(alpha = 0.8f), cardColor),
                            center = Offset.Zero,
                            radius = 200f
                        )
                    )
                    .border(BorderStroke(1.dp, NothingWhite.copy(alpha = 0.1f)), RoundedCornerShape(4.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val dotSize = 0.5.dp.toPx()
                    val gap = 4.dp.toPx()
                    for (i in 0..(size.width / gap).toInt()) {
                        for (j in 0..(size.height / gap).toInt()) {
                            drawCircle(
                                color = NothingWhite.copy(alpha = 0.1f),
                                radius = dotSize / 2,
                                center = Offset(i * gap, j * gap)
                            )
                        }
                    }
                }
            }
        }
        
        // Nothing Red Dot
        Canvas(
            modifier = Modifier
                .size(4.dp)
                .align(Alignment.TopEnd)
                .padding(4.dp)
        ) {
            drawCircle(color = NothingRed)
        }
    }
}

@Composable
fun DotMatrixText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = NothingWhite,
    fontSize: Int = 24
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.displaySmall.copy(
            color = color,
            fontSize = fontSize.sp,
            fontFamily = SpaceMonoFamily
        )
    )
}

@Composable
fun NothingDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(NothingBorder)
    )
}
