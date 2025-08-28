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
                label = {
                    Text(
                        stringResource(navigationItem.label),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
                icon = {
                    Icon(
                        painter = painterResource(navigationItem.icon),
                        contentDescription = stringResource(navigationItem.label),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                },
                onClick = {
                    if (currentDestination?.hasRoute(navigationItem.route::class) != true) {
                        navController.navigate(navigationItem.route) {
                            popUpTo<Screen.Home> {
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
