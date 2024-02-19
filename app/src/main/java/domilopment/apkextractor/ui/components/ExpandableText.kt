package domilopment.apkextractor.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun ExpandableText(
    text: String, modifier: Modifier = Modifier, maxLines: Int, overflow: TextOverflow
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    Text(
        text = text,
        modifier = modifier
            .animateContentSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = null
            ) { expanded = !expanded },
        maxLines = if (expanded) Int.MAX_VALUE else maxLines,
        overflow = overflow
    )
}

@Composable
fun ExpandableText(
    text: AnnotatedString, modifier: Modifier = Modifier, maxLines: Int, overflow: TextOverflow
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    Text(
        text = text,
        modifier = modifier
            .animateContentSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = null
            ) { expanded = !expanded },
        maxLines = if (expanded) Int.MAX_VALUE else maxLines,
        overflow = overflow
    )
}