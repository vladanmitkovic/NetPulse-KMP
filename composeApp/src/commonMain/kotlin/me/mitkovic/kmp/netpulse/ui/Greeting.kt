package me.mitkovic.kmp.netpulse.ui

import me.mitkovic.kmp.netpulse.platform.getPlatform

class Greeting {
    private val platform = getPlatform()

    fun greet(): String = "Hello, ${platform.name}!"
}
