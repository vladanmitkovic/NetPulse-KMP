package me.mitkovic.kmp.netpulse.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedGauge(
    angle: Float,
    width: Int,
    height: Int,
    arcColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier =
            modifier
                .requiredWidth(width.dp)
                .requiredHeight(height.dp),
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val sinMax = sin(60 * PI / 180).toFloat() // approx 0.866f
        val verticalSpanRatio = 1f + sinMax // approx 1.866f
        val strokeRatio = 0.2f // strokeWidth = radius * strokeRatio
        val radius = canvasHeight / (verticalSpanRatio + strokeRatio)
        val strokeWidth = radius * strokeRatio
        val centerX = canvasWidth / 2f
        val centerY = radius + strokeWidth / 2f
        val topLeftX = centerX - radius
        val topLeftY = centerY - radius
        val arcSize = 2f * radius

        val gaugeStartAngle = 120f
        val maxSweepAngle = 300f
        val scaledAngle = angle * (maxSweepAngle / 180f)

        drawArc(
            color = Color.Gray,
            startAngle = gaugeStartAngle,
            sweepAngle = maxSweepAngle,
            useCenter = false,
            topLeft = Offset(topLeftX, topLeftY),
            size = Size(arcSize, arcSize),
            style = Stroke(width = strokeWidth),
        )

        drawArc(
            color = arcColor,
            startAngle = gaugeStartAngle,
            sweepAngle = scaledAngle,
            useCenter = false,
            topLeft = Offset(topLeftX, topLeftY),
            size = Size(arcSize, arcSize),
            style = Stroke(width = strokeWidth),
        )

        val needleLength = radius - (strokeWidth / 2f)
        val needleAngleDeg = gaugeStartAngle + scaledAngle
        val needleAngleRad = (needleAngleDeg * PI / 180f).toFloat()
        val needleEndX = centerX + needleLength * cos(needleAngleRad)
        val needleEndY = centerY + needleLength * sin(needleAngleRad)
        drawLine(
            color = Color.Gray,
            start = Offset(centerX, centerY),
            end = Offset(needleEndX, needleEndY),
            strokeWidth = radius * 0.1f,
        )

        drawCircle(
            color = Color.Gray,
            radius = radius * 0.1f,
            center = Offset(centerX, centerY),
        )
    }
}
