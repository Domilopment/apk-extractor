package domilopment.apkextractor.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun InfoText(text: String) {
    Text(
        text = text.toAnnotatedInfoText(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

fun String.toAnnotatedInfoText(
    style: SpanStyle
): AnnotatedString = buildAnnotatedString {
    append(this@toAnnotatedInfoText)

    val separatorIndex = indexOfAny(charArrayOf(':', '：'))

    if (separatorIndex >= 0) {
        addStyle(
            style = style,
            start = 0,
            end = separatorIndex + 1,
        )
    }
}