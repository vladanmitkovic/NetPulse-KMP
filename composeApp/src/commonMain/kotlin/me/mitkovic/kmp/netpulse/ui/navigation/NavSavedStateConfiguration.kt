package me.mitkovic.kmp.netpulse.ui.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val NavSavedStateConfiguration: SavedStateConfiguration =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Screen.Home::class)
                    subclass(Screen.SpeedTest::class)
                    subclass(Screen.History::class)
                    subclass(Screen.Settings::class)
                }
            }
    }
