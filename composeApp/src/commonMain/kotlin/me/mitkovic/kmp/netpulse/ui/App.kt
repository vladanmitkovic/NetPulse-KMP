package me.mitkovic.kmp.netpulse.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.mitkovic.kmp.netpulse.logging.IAppLogger
import me.mitkovic.kmp.netpulse.ui.components.ApplicationTitle
import me.mitkovic.kmp.netpulse.ui.components.BottomNavigationBar
import me.mitkovic.kmp.netpulse.ui.navigation.AppNavDisplay
import me.mitkovic.kmp.netpulse.ui.navigation.currentTopBarState
import me.mitkovic.kmp.netpulse.ui.navigation.rememberAppNavigator
import me.mitkovic.kmp.netpulse.ui.theme.AppTheme
import me.mitkovic.kmp.netpulse.ui.theme.spacing
import netpulse_kmp.composeapp.generated.resources.Res
import netpulse_kmp.composeapp.generated.resources.content_description_back_arrow
import netpulse_kmp.composeapp.generated.resources.content_description_change_theme_icon
import netpulse_kmp.composeapp.generated.resources.light_off
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val appViewModel: AppViewModel = koinInject<AppViewModel>()

    val themeValue by appViewModel.theme.collectAsStateWithLifecycle(initialValue = null)

    val appLogger: IAppLogger = koinInject()

    LaunchedEffect(Unit) {
        appLogger.logDebug("App", "App Start from: ${Greeting().greet()}")
    }

    themeValue?.let { loadedTheme ->
        AppTheme(isLightTheme = loadedTheme) {
            MainScreen(
                appViewModel = appViewModel,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(appViewModel: AppViewModel) {
    val navigator = rememberAppNavigator()
    val backStack = navigator.backStack

    val topBarState = currentTopBarState(backStack)

    Scaffold(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        // 60% dominant color for main screen bg
        topBar = {
            TopAppBar(
                title = { ApplicationTitle(topBarState.title, topBarState.showActions) },
                actions = {
                    if (topBarState.showActions) {
                        IconButton(
                            onClick = { appViewModel.toggleTheme() },
                            modifier =
                                Modifier
                                    .padding(end = 8.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.secondary,
                                        shape = CircleShape,
                                    ),
                        ) {
                            Icon(
                                painterResource(Res.drawable.light_off),
                                modifier = Modifier.size(MaterialTheme.spacing.iconSize),
                                contentDescription = stringResource(Res.string.content_description_change_theme_icon),
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (topBarState.showBackIcon) {
                        IconButton(onClick = { navigator.goBack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                modifier = Modifier.size(MaterialTheme.spacing.iconSize),
                                contentDescription = stringResource(Res.string.content_description_back_arrow),
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
        bottomBar = {
            Column {
                // Bottom Navigation Bar
                BottomNavigationBar(
                    navigator = navigator,
                    backStack = backStack,
                )
            }
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            AppNavDisplay(navigator = navigator)
        }
    }
}
