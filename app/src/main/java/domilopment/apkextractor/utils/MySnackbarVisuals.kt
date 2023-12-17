package domilopment.apkextractor.utils

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.ui.graphics.Color

data class MySnackbarVisuals(
    override val actionLabel: String? = null,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    override val message: String,
    override val withDismissAction: Boolean = false,
    val messageColor: Color? = null
) : SnackbarVisuals
