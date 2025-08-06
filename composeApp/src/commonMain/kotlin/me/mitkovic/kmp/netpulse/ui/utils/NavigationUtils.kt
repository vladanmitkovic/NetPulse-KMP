package me.mitkovic.kmp.netpulse.ui.utils

import androidx.navigation.NavController
import me.mitkovic.kmp.netpulse.logging.AppLogger
import me.mitkovic.kmp.netpulse.ui.navigation.BottomNavigation

object NavigationUtils {

    private val homeRoute = BottomNavigation.HOME.route::class.qualifiedName
    private val historyRoute = BottomNavigation.HISTORY.route::class.qualifiedName
    private val settingsRoute = BottomNavigation.SETTINGS.route::class.qualifiedName
    private const val SPEED_TEST_SCREEN_ROUTE_PATTERN = "SpeedTest/{serverId}"

    /**
     * Determines if the current route corresponds to the target destination.
     *
     * @param currentRoute The current route from NavController.
     * @param targetRoute The target route string.
     * @param navController The NavController instance.
     * @return True if the current route matches the target destination or is a detail route for it.

     Handling bottom navigation tab selection
     When user goes to detail screen from Home, SpeedTest screen Home tab has to stay selected.
     The same for History tab. When user goes to History detail screen from History tab, Detail screen History tab has to stay selected.
     */
    fun isSelectedRoute(
        currentRoute: String?,
        targetRoute: String,
        navController: NavController,
        logger: AppLogger,
    ): Boolean {
        logger.logError("GILE", "currentRoute: $currentRoute", null)

        // Helper function to check if the current route is a detail route for the target route
        fun isDetailRouteFor(targetRoute: String): Boolean =
            currentRoute?.endsWith(SPEED_TEST_SCREEN_ROUTE_PATTERN) == true &&
                navController.previousBackStackEntry?.destination?.route == targetRoute

        // Compare the current route with target route or check if it's a detail route
        return when (targetRoute) {
            homeRoute -> currentRoute == homeRoute || isDetailRouteFor(homeRoute)
            historyRoute -> currentRoute == historyRoute || isDetailRouteFor(historyRoute)
            settingsRoute -> currentRoute == settingsRoute
            else -> false
        }
    }
}
