package me.mitkovic.kmp.netpulse

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform