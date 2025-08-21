package me.mitkovic.kmp.netpulse.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import me.mitkovic.kmp.netpulse.logging.AppLogger
import me.mitkovic.kmp.netpulse.ui.common.ApplicationTitle
import me.mitkovic.kmp.netpulse.ui.common.BottomNavigationBar
import me.mitkovic.kmp.netpulse.ui.navigation.AppNavHost
import me.mitkovic.kmp.netpulse.ui.navigation.currentTopBarState
import me.mitkovic.kmp.netpulse.ui.theme.AppTheme
import me.mitkovic.kmp.netpulse.ui.theme.spacing
import netpulse_kmp.composeapp.generated.resources.Res
import netpulse_kmp.composeapp.generated.resources.content_description_back_arrow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val viewModel: AppViewModel = koinInject<AppViewModel>()

    val appLogger: AppLogger = koinInject()
    appLogger.logDebug("App", "App Start from: ${Greeting().greet()}")

    val navController = rememberNavController()
    val topBarState = navController.currentTopBarState()

    AppTheme(isLightTheme = true) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        ApplicationTitle(topBarState.title, topBarState.showActions)
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                        ),
                    navigationIcon = {
                        if (topBarState.showBackIcon) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    modifier = Modifier.size(MaterialTheme.spacing.iconSize),
                                    contentDescription = stringResource(Res.string.content_description_back_arrow),
                                    tint = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }
                    },
                )
            },
            bottomBar = {
                Column {
                    // Bottom Navigation Bar
                    BottomNavigationBar(
                        navController = navController,
                    )
                }
            },
        ) { paddingValues ->
            Box(
                modifier =
                    Modifier
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background),
            ) {
                AppNavHost(
                    navHostController = navController,
                )
            }
        }
    }
}
