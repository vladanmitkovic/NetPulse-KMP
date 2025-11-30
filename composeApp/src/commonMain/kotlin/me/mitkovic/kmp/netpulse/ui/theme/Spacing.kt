package me.mitkovic.kmp.netpulse.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Spacing(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val extraMedium: Dp = 10.dp,
    val medium: Dp = 16.dp,
    val small20: Dp = 20.dp,
    val large: Dp = 24.dp,
    val xLarge: Dp = 32.dp,
    val iconSize: Dp = 32.dp,
    val deleteButtonSize: Dp = 36.dp,
    val chartHeight: Dp = 40.dp,
    val chartHeightLarge: Dp = 60.dp,
    val chartWidth: Dp = 100.dp,
    val xxLarge: Dp = 96.dp,
    val xxxLarge: Dp = 128.dp,
    val buttonSizeLarge: Dp = 200.dp,
    val gaugeWidth: Dp = 300.dp,
    val gaugeHeight: Dp = 200.dp,
    val elevation: Dp = 2.dp,
    val progressIndicatorWidth: Dp = 4.dp,
)

val LocalSpacing = compositionLocalOf { Spacing() }

val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current
