package me.mitkovic.kmp.netpulse.ui.components

import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import me.mitkovic.kmp.netpulse.ui.navigation.AppNavigator
import me.mitkovic.kmp.netpulse.ui.navigation.BottomNavigation
import me.mitkovic.kmp.netpulse.ui.navigation.Screen
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun BottomNavigationBar(
    navigator: AppNavigator,
    backStack: NavBackStack<NavKey>,
) {
    val current = backStack.lastOrNull()

    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        BottomNavigation.entries.forEach { item ->
            val isSelected =
                when (item) {
                    BottomNavigation.HOME -> current == Screen.Home || current is Screen.SpeedTest
                    else -> current == item.route
                }

            NavigationBarItem(
                selected = isSelected,
                label = {
                    Text(
                        stringResource(item.label),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
                icon = {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = stringResource(item.label),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                },
                onClick = {
                    navigator.navigateToTab(item.route)
                },
            )
        }
    }
}
