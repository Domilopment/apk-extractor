package domilopment.apkextractor.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun InfoText(text: String) {
    Text(
        text = AnnotatedString(
            text, listOf(
                AnnotatedString.Range(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold
                    ), 0, text.indexOf(':') + 1
                )
            )
        ), maxLines = 1, overflow = TextOverflow.Ellipsis
    )
}