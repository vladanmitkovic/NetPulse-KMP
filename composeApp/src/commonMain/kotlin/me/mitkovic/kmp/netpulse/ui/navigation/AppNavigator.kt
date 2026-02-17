package me.mitkovic.kmp.netpulse.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

class AppNavigator internal constructor(
    val backStack: NavBackStack<NavKey>,
) {
    fun navigate(route: Screen) {
        backStack.add(route)
    }

    fun goBack() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    // For bottom tabs: keep a single instance of the tab screen.
    fun navigateToTab(tab: Screen) {
        val root = backStack.firstOrNull() as? Screen ?: Screen.Home

        // Clear everything except root
        while (backStack.size > 1) {
            backStack.removeLastOrNull()
        }

        // If tab is root, we’re done
        if (tab == root) return

        backStack.add(tab)
    }
}

@Composable
fun rememberAppNavigator(): AppNavigator {
    val backStack: NavBackStack<NavKey> = rememberNavBackStack(
        NavSavedStateConfiguration,
        Screen.Home,
    )
    return remember(backStack) { AppNavigator(backStack) }
}
