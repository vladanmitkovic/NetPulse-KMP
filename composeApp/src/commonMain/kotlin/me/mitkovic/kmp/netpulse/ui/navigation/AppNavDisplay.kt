package me.mitkovic.kmp.netpulse.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import me.mitkovic.kmp.netpulse.ui.screens.history.HistoryScreen
import me.mitkovic.kmp.netpulse.ui.screens.history.HistoryScreenViewModel
import me.mitkovic.kmp.netpulse.ui.screens.home.HomeScreen
import me.mitkovic.kmp.netpulse.ui.screens.home.HomeScreenViewModel
import me.mitkovic.kmp.netpulse.ui.screens.settings.SettingsScreen
import me.mitkovic.kmp.netpulse.ui.screens.settings.SettingsScreenViewModel
import me.mitkovic.kmp.netpulse.ui.screens.speedtest.SpeedTestScreen
import me.mitkovic.kmp.netpulse.ui.screens.speedtest.SpeedTestScreenViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun AppNavDisplay(
    navigator: AppNavigator,
) {
    NavDisplay(
        backStack = navigator.backStack,
        onBack = { navigator.goBack() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {

            entry<Screen.Home> {
                val vm: HomeScreenViewModel = koinInject()
                HomeScreen(
                    viewModel = vm,
                    onNavigateToSpeedTest = { serverId ->
                        navigator.navigate(Screen.SpeedTest(serverId))
                    },
                )
            }

            entry<Screen.SpeedTest> { key ->
                val vm: SpeedTestScreenViewModel = koinInject { parametersOf(key.serverId) }
                SpeedTestScreen(viewModel = vm)
            }

            entry<Screen.History> {
                val vm: HistoryScreenViewModel = koinInject()
                HistoryScreen(viewModel = vm)
            }

            entry<Screen.Settings> {
                val vm: SettingsScreenViewModel = koinInject()
                SettingsScreen(viewModel = vm)
            }
        },
    )
}
