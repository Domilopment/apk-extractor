package domilopment.apkextractor.ui.dialogs

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import domilopment.apkextractor.R
import domilopment.apkextractor.utils.Constants
import domilopment.apkextractor.utils.Utils

@Composable
fun AnalyticsDialog(
    onConfirmButton: (Boolean, Boolean, Boolean) -> Unit, context: Context = LocalContext.current
) {
    val scrollState = rememberScrollState()
    val endReached by remember {
        derivedStateOf {
            scrollState.value == scrollState.maxValue
        }
    }

    var analytics by remember {
        mutableStateOf(true)
    }
    var crashlytics by remember {
        mutableStateOf(true)
    }
    var performance by remember {
        mutableStateOf(true)
    }

    AlertDialog(onDismissRequest = {}, confirmButton = {
        TextButton(
            onClick = { onConfirmButton(analytics, crashlytics, performance) }, enabled = endReached
        ) {
            Text(text = stringResource(id = R.string.consent_dialog_confirm))
        }
    }, dismissButton = {
        TextButton(onClick = { onConfirmButton(false, false, false) }, enabled = endReached) {
            Text(text = stringResource(id = R.string.consent_dialog_dismiss))
        }
    }, title = {
        Text(text = stringResource(id = R.string.data_collection_header))
    }, text = {
        Column(modifier = Modifier.verticalScroll(state = scrollState)) {
            Text(
                text = AnnotatedString(
                    text = stringResource(id = R.string.consent_dialog_header),
                    SpanStyle(fontWeight = FontWeight.Bold)
                )
            )
            Column(
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
            ) {
                ListItem(
                    headlineContent = { Text(text = stringResource(id = R.string.data_collection_analytics)) },
                    modifier = Modifier.clickable { analytics = !analytics },
                    supportingContent = {
                        Text(text = stringResource(id = R.string.data_collection_analytics_summary))
                    },
                    trailingContent = {
                        Switch(checked = analytics, onCheckedChange = { analytics = it })
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                ListItem(
                    headlineContent = { Text(text = stringResource(id = R.string.data_collection_crashlytics)) },
                    modifier = Modifier.clickable { crashlytics = !crashlytics },
                    supportingContent = {
                        Text(text = stringResource(id = R.string.data_collection_crashlytics_summary))
                    },
                    trailingContent = {
                        Switch(checked = crashlytics, onCheckedChange = { crashlytics = it })
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                ListItem(
                    headlineContent = { Text(text = stringResource(id = R.string.data_collection_perf)) },
                    modifier = Modifier.clickable { performance = !performance },
                    supportingContent = {
                        Text(text = stringResource(id = R.string.data_collection_perf_summary))
                    },
                    trailingContent = {
                        Switch(checked = performance, onCheckedChange = { performance = it })
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
            Text(
                text = AnnotatedString(text = stringResource(id = R.string.consent_dialog_consent_notice))
            )
            val text = Utils.getAnnotatedUrlString(
                stringResource(
                    id = R.string.consent_dialog_footer,
                    stringResource(id = R.string.privacy_policy_title),
                    stringResource(id = R.string.terms_title)
                ), Pair(
                    stringResource(id = R.string.privacy_policy_title), Constants.PRIVACY_POLICY_URL
                ), Pair(stringResource(id = R.string.terms_title), Constants.TERMS_URL)
            )
            ClickableText(
                text = text
            ) { offset ->
                text.getStringAnnotations(offset, offset).find { it.tag.startsWith("URL") }
                    ?.let { stringAnnotation ->
                        CustomTabsIntent.Builder().build().launchUrl(
                            context, Uri.parse(stringAnnotation.item)
                        )
                    }
            }
        }
    })
}