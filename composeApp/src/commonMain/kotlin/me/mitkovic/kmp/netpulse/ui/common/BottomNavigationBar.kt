package me.mitkovic.kmp.netpulse.ui.common

import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import me.mitkovic.kmp.netpulse.logging.AppLogger
import me.mitkovic.kmp.netpulse.ui.navigation.BottomNavigation
import me.mitkovic.kmp.netpulse.ui.utils.NavigationUtils.isSelectedRoute
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun BottomNavigationBar(
    navController: NavController,
    onError: (String, Throwable?) -> Unit,
    logger: AppLogger,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute =
        navBackStackEntry?.destination?.route
            ?: BottomNavigation.HOME.route::class.qualifiedName.orEmpty()

    val currentRouteTrimmed by remember(currentRoute) {
        derivedStateOf { currentRoute.substringBefore("?") }
    }

    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        BottomNavigation.entries.forEachIndexed { index, navigationItem ->
            NavigationBarItem(
                selected =
                    isSelectedRoute(
                        currentRouteTrimmed,
                        navigationItem.route::class.qualifiedName.toString(),
                        navController,
                        logger,
                    ),
                label = { Text(stringResource(navigationItem.label)) },
                icon = {
                    Icon(
                        painter = painterResource(resource = navigationItem.icon),
                        contentDescription = stringResource(resource = navigationItem.label),
                    )
                },
                onClick = {
                    if (navBackStackEntry?.destination?.route != navigationItem.route::class.qualifiedName.toString()) {
                        try {
                            navController.navigate(navigationItem.route::class.qualifiedName.toString()) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } catch (e: Exception) {
                            onError("Navigation error: ${e.message}", e)
                        }
                    } else {
                        navController.popBackStack(
                            navigationItem.route::class.qualifiedName.toString(),
                            inclusive = false,
                        )
                    }
                },
            )
        }
    }
}
