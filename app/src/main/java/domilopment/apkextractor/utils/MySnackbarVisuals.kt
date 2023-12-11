package domilopment.apkextractor.utils

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

data class MySnackbarVisuals(
    override val actionLabel: String?,
    override val duration: SnackbarDuration,
    override val message: String,
    override val withDismissAction: Boolean,
    val messageColor: Color?,
    val snackbarOffset: Dp?
): SnackbarVisuals
