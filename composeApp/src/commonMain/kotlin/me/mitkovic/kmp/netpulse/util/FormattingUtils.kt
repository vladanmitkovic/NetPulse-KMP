package me.mitkovic.kmp.netpulse.util

import kotlin.math.roundToInt

fun formatDoubleToInt(value: Double?): String = value?.roundToInt()?.toString() ?: "N/A"
