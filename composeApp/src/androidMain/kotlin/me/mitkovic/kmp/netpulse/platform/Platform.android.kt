package me.mitkovic.kmp.netpulse.platform

import android.app.Activity
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

@Composable
actual fun UpdateStatusBarAppearance(isDarkTheme: Boolean) {
    val context = LocalContext.current
    val view = LocalView.current
    if (context is Activity) {
        DisposableEffect(isDarkTheme) {
            WindowCompat.setDecorFitsSystemWindows(context.window, false)
            // Update status bar icon appearance based on theme.
            WindowCompat.getInsetsController(context.window, view).isAppearanceLightStatusBars = !isDarkTheme
            onDispose { }
        }
    }
}
