package domilopment.apkextractor.ui.dialogs

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import domilopment.apkextractor.R
import domilopment.apkextractor.utils.Constants

@Composable
fun AnalyticsDialog(
    onConfirmButton: (Boolean, Boolean, Boolean) -> Unit, context: Context = LocalContext.current
) {
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
        TextButton(onClick = { onConfirmButton(analytics, crashlytics, performance) }) {
            Text(text = stringResource(id = R.string.data_collection_confirm))
        }
    }, dismissButton = {
        TextButton(onClick = { onConfirmButton(false, false, false) }) {
            Text(text = stringResource(id = R.string.data_collection_dismiss))
        }
    }, title = {
        Text(text = stringResource(id = R.string.data_collection_header))
    }, text = {
        Column(modifier = Modifier.verticalScroll(state = rememberScrollState())) {
            ClickableText(text = AnnotatedString("Data is collected anonymously (unique IDs per Installation on each device) and only used for provided use cases.")) {

            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = {
                        CustomTabsIntent.Builder().build().launchUrl(
                            context, Uri.parse(Constants.TERMS_URL)
                        )
                    }, border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = "Terms & Conditions")
                }
                TextButton(
                    onClick = {
                        CustomTabsIntent.Builder().build().launchUrl(
                            context, Uri.parse(Constants.PRIVACY_POLICY_URL)
                        )
                    }, border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = stringResource(id = R.string.privacy_policy_title))
                }
            }
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
        }
    })
}