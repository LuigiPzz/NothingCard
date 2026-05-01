package com.nothing.card.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NothingWhite,
    secondary = NothingOnSurfaceVariant,
    tertiary = NothingRed,
    background = NothingBlack,
    surface = NothingBlack,
    onPrimary = NothingBlack,
    onSecondary = NothingWhite,
    onTertiary = NothingWhite,
    onBackground = NothingWhite,
    onSurface = NothingWhite,
    surfaceVariant = NothingSurface,
    onSurfaceVariant = NothingOnSurfaceVariant,
    outline = NothingBorder,
    error = NothingError,
    onError = NothingOnError,
    // Add surface container variants for M3 components like Dialogs
    surfaceContainer = NothingBlack,
    surfaceContainerHigh = NothingBlack,
    surfaceContainerHighest = NothingBlack,
    surfaceContainerLow = NothingBlack,
    surfaceContainerLowest = NothingBlack
)

@Composable
fun NothingCardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme // Nothing OS is AMOLED-first

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            var context = view.context
            while (context is android.content.ContextWrapper) {
                if (context is Activity) break
                context = context.baseContext
            }
            
            val window = (context as? Activity)?.window
            window?.let {
                it.statusBarColor = colorScheme.background.toArgb()
                it.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NothingTypography,
        content = content
    )
}
