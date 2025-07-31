package me.mitkovic.kmp.netpulse.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun LinearChart(
    speeds: List<Float>,
    lineColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        if (speeds.isNotEmpty()) {
            val max = speeds.maxOrNull() ?: 1f
            val points =
                speeds.mapIndexed { index, s ->
                    val x = (index.toFloat() / (speeds.size - 1).coerceAtLeast(1)) * size.width
                    val y = size.height - (s / max * size.height)
                    Offset(x, y)
                }
            drawPath(
                Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                },
                color = lineColor,
                style = Stroke(width = 8f),
            )
        }
    }
}
