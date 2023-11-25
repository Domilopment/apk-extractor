package domilopment.apkextractor.ui.composables

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

@Composable
fun attrColorResource(attrId: Int): Color {
    val context = LocalContext.current
    val attrs = context.theme.obtainStyledAttributes(intArrayOf(attrId))
    val colorValue = attrs.getColor(0, 0)
    return Color(colorValue)
}

@Composable
fun SpannableText(
    text: String, searchString: String, fontSize: TextUnit, maxLines: Int, overflow: TextOverflow
) {
    val annotatedString = if (searchString.isNotBlank() && text.contains(searchString, ignoreCase = true)) {
        val color = attrColorResource(attrId = android.R.attr.textColorHighlight)
        val startIndex = text.lowercase().indexOf(searchString.lowercase())
        val endIndex = startIndex + searchString.length

        AnnotatedString(text, listOf(AnnotatedString.Range(SpanStyle(color), startIndex, endIndex)))
    } else AnnotatedString(text)

    Text(text = annotatedString, fontSize = fontSize, maxLines = maxLines, overflow = overflow)
}