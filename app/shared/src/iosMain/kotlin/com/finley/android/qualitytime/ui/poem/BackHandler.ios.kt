package com.finley.android.qualitytime.ui.poem

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS doesn't have a hardware back button, navigation is typically handled by UI buttons
}
