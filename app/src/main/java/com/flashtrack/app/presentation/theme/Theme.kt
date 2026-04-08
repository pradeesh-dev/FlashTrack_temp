package com.flashtrack.app.presentation.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FlashTrackDarkColorScheme = darkColorScheme(
    primary          = Primary,
    onPrimary        = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = Color(0xFF81C784),
    secondary        = Primary,
    onSecondary      = OnPrimary,
    tertiary         = TransferBlue,
    background       = Background,
    onBackground     = OnBackground,
    surface          = Surface,
    onSurface        = OnSurface,
    surfaceVariant   = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline          = DividerColor,
    error            = Error,
    onError          = Color.White,
    scrim            = Color(0x99000000),
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceVariant,
    surfaceContainerLow  = Surface,
)

@Composable
fun FlashTrackTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FlashTrackDarkColorScheme,
        typography  = FlashTrackTypography,
        content     = content
    )
}
