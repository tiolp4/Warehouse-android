package com.example.warehouse.ui.manager

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warehouse.data.model.ApiCountEntry
import com.example.warehouse.data.model.ApiDailyFlow

@Composable
fun KpiTile(
    title: String,
    value: String,
    sub: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            if (!sub.isNullOrBlank()) {
                Text(
                    sub,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Two-series day-by-day bar chart (отправлено vs прибыло / приём vs отгрузка).
 */
@Composable
fun DailyFlowChart(
    data: List<ApiDailyFlow>,
    seriesA: String = "Отправлено",
    seriesB: String = "Прибыло",
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LegendDot(primary); Spacer(Modifier.width(4.dp))
            Text(seriesA, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.width(12.dp))
            LegendDot(secondary); Spacer(Modifier.width(4.dp))
            Text(seriesB, style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.height(8.dp))

        val max = (data.maxOfOrNull { maxOf(it.incoming, it.outgoing) } ?: 0L)
            .coerceAtLeast(1L)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (data.isEmpty()) return@Canvas
            val w = size.width
            val h = size.height
            val padX = 8f
            val padY = 16f
            val plotW = w - 2 * padX
            val plotH = h - 2 * padY
            val groupW = plotW / data.size
            val barW = groupW * 0.35f

            // Grid lines (4)
            repeat(4) { i ->
                val y = padY + plotH * i / 4
                drawLine(gridColor, Offset(padX, y), Offset(w - padX, y), strokeWidth = 1f)
            }

            data.forEachIndexed { idx, df ->
                val cx = padX + groupW * idx + groupW / 2f
                val hA = plotH * df.incoming.toFloat() / max
                val hB = plotH * df.outgoing.toFloat() / max
                drawRoundedBar(
                    primary,
                    Offset(cx - barW - 2f, padY + plotH - hA),
                    Size(barW, hA)
                )
                drawRoundedBar(
                    secondary,
                    Offset(cx + 2f, padY + plotH - hB),
                    Size(barW, hB)
                )
            }
        }
    }
}

@Composable
fun HorizontalCountBars(
    entries: List<ApiCountEntry>,
    labelMapper: (String) -> String = { it },
    modifier: Modifier = Modifier
) {
    if (entries.isEmpty()) {
        Text(
            "Нет данных",
            modifier = modifier.padding(8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }
    val max = entries.maxOf { it.count }.coerceAtLeast(1)
    val barColor = MaterialTheme.colorScheme.primary

    Column(modifier) {
        entries.forEach { e ->
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(
                    labelMapper(e.key),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(110.dp),
                    maxLines = 1
                )
                Box(
                    Modifier
                        .weight(1f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(e.count.toFloat() / max)
                            .background(barColor, RoundedCornerShape(6.dp))
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    e.count.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color) {
    Box(
        Modifier
            .size(10.dp)
            .clip(RoundedCornerShape(50))
            .background(color)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRoundedBar(
    color: Color, topLeft: Offset, size: Size
) {
    if (size.height <= 0f) return
    drawRoundRect(
        color = color,
        topLeft = topLeft,
        size = size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )
}
