package domilopment.apkextractor.ui.dialogs

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import domilopment.apkextractor.R
import domilopment.apkextractor.data.repository.analytics.AnalyticsHelper
import domilopment.apkextractor.data.repository.analytics.LocalAnalyticsHelper
import domilopment.apkextractor.ui.components.DoubleBackPressDialog
import domilopment.apkextractor.ui.components.HyperlinkText
import domilopment.apkextractor.ui.components.Link
import domilopment.apkextractor.utils.Constants
import domilopment.apkextractor.utils.fadingBottom
import domilopment.apkextractor.utils.fadingTop

@Composable
fun AnalyticsDialog(
    onConfirmButton: (Boolean, Boolean, Boolean) -> Unit, context: Context = LocalContext.current
) {
    val analyticsHelper = LocalAnalyticsHelper.current

    val scrollState = rememberScrollState()

    var endReached by remember {
        mutableStateOf(false)
    }

    var analytics by remember {
        mutableStateOf(false)
    }
    var crashlytics by remember {
        mutableStateOf(false)
    }
    var performance by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = scrollState.canScrollForward) {
        if (!scrollState.canScrollForward && !endReached) endReached = true
    }

    DoubleBackPressDialog(
        onDismissRequest = { (context as? Activity)?.finish() },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmButton(analytics, crashlytics, performance)
                    analyticsHelper.logEvent(AnalyticsHelper.Events.SET_DATA_COLLECTION) {
                        putString(AnalyticsHelper.Param.COLLECT_ANALYTICS, analytics.toString())
                        putString(AnalyticsHelper.Param.COLLECT_CRASHLYTICS, crashlytics.toString())
                        putString(AnalyticsHelper.Param.COLLECT_PERFORMANCE, performance.toString())
                    }
                }, enabled = endReached
            ) {
                Text(text = stringResource(id = R.string.consent_dialog_confirm))
            }
        },
        backPressNotice = stringResource(id = R.string.double_back_press_first_press_toast_text),
        dismissButton = {
            TextButton(onClick = { onConfirmButton(false, false, false) }, enabled = endReached) {
                Text(text = stringResource(id = R.string.consent_dialog_dismiss))
            }
        },
        title = {
            Text(text = stringResource(id = R.string.data_collection_header))
        },
        text = {
            Column(
                modifier = Modifier
                    .fadingTop(visible = scrollState.canScrollBackward)
                    .fadingBottom(visible = scrollState.canScrollForward)
                    .verticalScroll(state = scrollState)
            ) {
                Text(
                    text = AnnotatedString(
                        text = stringResource(id = R.string.consent_dialog_header),
                        SpanStyle(fontWeight = FontWeight.Bold)
                    )
                )
                Column(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .background(
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

                val privacyPolicy = stringResource(id = R.string.privacy_policy_title)
                val terms = stringResource(id = R.string.terms_title)
                HyperlinkText(
                    text = stringResource(
                        id = R.string.consent_dialog_footer, privacyPolicy, terms
                    ), links = arrayOf(
                        Link(text = privacyPolicy, href = Constants.PRIVACY_POLICY_URL),
                        Link(text = terms, href = Constants.TERMS_URL)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = AnnotatedString(
                        text = stringResource(id = R.string.consent_dialog_consent_notice),
                        SpanStyle(fontWeight = FontWeight.Bold)
                    )
                )
            }
        },
        properties = DialogProperties(dismissOnClickOutside = false),
        context = context
    )
}

@Preview
@Composable
private fun AnalyticsDialogPreview() {
    val localAnalyticsHelper = object : AnalyticsHelper {
        override fun logEvent(event: String, params: Bundle) {
            // No op
        }
    }
    CompositionLocalProvider(LocalAnalyticsHelper provides localAnalyticsHelper) {
        AnalyticsDialog(onConfirmButton = { _, _, _ -> /* No op */ })
    }
}