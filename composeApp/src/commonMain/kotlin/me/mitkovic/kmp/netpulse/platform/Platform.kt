package me.mitkovic.kmp.netpulse.platform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
