package me.mitkovic.kmp.netpulse.ui

import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import me.mitkovic.kmp.netpulse.logging.AppLogger
import me.mitkovic.kmp.netpulse.ui.common.ApplicationTitle
import me.mitkovic.kmp.netpulse.ui.navigation.AppNavHost
import me.mitkovic.kmp.netpulse.ui.theme.AppTheme
import me.mitkovic.kmp.netpulse.ui.theme.spacing
import netpulse_kmp.composeapp.generated.resources.Res
import netpulse_kmp.composeapp.generated.resources.app_name
import netpulse_kmp.composeapp.generated.resources.content_description_back_arrow
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

sealed class MainAction {
    data class TitleTextChanged(
        val title: String,
    ) : MainAction()

    data class ShowActionsChanged(
        val showActions: Boolean,
    ) : MainAction()

    data class ShowBackIconChanged(
        val showBackButton: Boolean,
    ) : MainAction()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    koinInject<AppViewModel>()
    val appLogger: AppLogger = koinInject()
    appLogger.logDebug("App", "App Start from: ${Greeting().greet()}")

    val topBarTitle = remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        topBarTitle.value = getString(Res.string.app_name)
    }

    val showActions = remember { mutableStateOf(false) }
    val showBackIcon = remember { mutableStateOf(false) }

    val navController = rememberNavController()

    AppTheme(isLightTheme = true) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        ApplicationTitle(topBarTitle.value, showActions.value)
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                        ),
                    navigationIcon = {
                        if (showBackIcon.value) {
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
        ) { innerPadding ->

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AppNavHost(
                    navHostController = navController,
                    onAction = { action ->
                        when (action) {
                            is MainAction.TitleTextChanged -> topBarTitle.value = action.title
                            is MainAction.ShowActionsChanged -> showActions.value = action.showActions
                            is MainAction.ShowBackIconChanged -> showBackIcon.value = action.showBackButton
                        }
                    },
                )
            }
        }
    }
}
