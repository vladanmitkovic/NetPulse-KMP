package me.mitkovic.kmp.netpulse.ui.common

import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import me.mitkovic.kmp.netpulse.ui.navigation.BottomNavigation
import me.mitkovic.kmp.netpulse.ui.navigation.Screen
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        BottomNavigation.entries.forEach { navigationItem ->
            val isSelected =
                when (navigationItem) {
                    BottomNavigation.HOME -> {
                        currentDestination?.hasRoute(Screen.Home::class) == true ||
                            currentDestination?.hasRoute(Screen.SpeedTest::class) == true
                    }
                    else -> currentDestination?.hasRoute(navigationItem.route::class) == true
                }

            NavigationBarItem(
                selected = isSelected,
                label = { Text(stringResource(navigationItem.label)) },
                icon = {
                    Icon(
                        painter = painterResource(navigationItem.icon),
                        contentDescription = stringResource(navigationItem.label),
                    )
                },
                onClick = {
                    if (currentDestination?.hasRoute(navigationItem.route::class) != true) {
                        navController.navigate(navigationItem.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    }
                },
            )
        }
    }
}
