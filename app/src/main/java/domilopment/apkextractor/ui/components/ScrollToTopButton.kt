package domilopment.apkextractor.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ScrollToTopButton(visible: Boolean, modifier: Modifier = Modifier, onScrollToTop: () -> Unit) {
    AnimatedVisibility(
        visible = visible, modifier = modifier, enter = fadeIn(), exit = fadeOut()
    ) {
        IconButton(
            onClick = onScrollToTop, colors = IconButtonDefaults.iconButtonColors().copy(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
        ) {
            Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "ScrollToTop Button")
        }
    }
}