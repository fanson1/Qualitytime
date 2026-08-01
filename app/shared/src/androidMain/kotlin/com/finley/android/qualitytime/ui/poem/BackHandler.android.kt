package com.finley.android.qualitytime.ui.poem

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Explicitly call the library function to avoid recursion
    androidx.activity.compose.BackHandler(enabled = enabled, onBack = onBack)
}
