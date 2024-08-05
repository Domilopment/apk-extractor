package domilopment.apkextractor.ui.components

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration

data class Link(
    val text: String, val href: String
)

@Composable
fun HyperlinkText(
    text: String,
    vararg links: Link,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current
) {
    val annotatedText = buildAnnotatedString {
        append(text)

        links.forEach { link ->
            val startIndex = text.indexOf(link.text)

            if (startIndex < 0) return@forEach

            val endIndex = startIndex + link.text.length

            addStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                ), start = startIndex, end = endIndex
            )

            addStringAnnotation(
                tag = "URL", annotation = link.href, start = startIndex, end = endIndex
            )
        }
    }
    ClickableText(
        text = annotatedText,
        modifier = modifier,
        style = LocalTextStyle.current.copy(color = LocalContentColor.current)
    ) { offset ->
        annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset).firstOrNull()
            ?.let { stringAnnotation ->
                CustomTabsIntent.Builder().build().launchUrl(
                    context, Uri.parse(stringAnnotation.item)
                )
            }
    }
}