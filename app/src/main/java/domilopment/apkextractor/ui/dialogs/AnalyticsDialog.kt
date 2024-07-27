package domilopment.apkextractor.ui.dialogs

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp

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
            Text(text = "Confirm")
        }
    }, dismissButton = {
        TextButton(onClick = { onConfirmButton(false, false, false) }) {
            Text(text = "Deny All")
        }
    }, title = {
        Text(text = "Data Collection")
    }, text = {
        Column(modifier = Modifier.verticalScroll(state = rememberScrollState())) {
            Text(text = "Data is collected anonymously (unique IDs per Installation on each device) and only used for provided use cases.")
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = {
                        CustomTabsIntent.Builder().build().launchUrl(
                            context,
                            Uri.parse("https://sites.google.com/view/domilopment/apk-extractor/terms")
                        )
                    }, border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = "Terms & Conditions")
                }
                TextButton(
                    onClick = {
                        CustomTabsIntent.Builder().build().launchUrl(
                            context,
                            Uri.parse("https://sites.google.com/view/domilopment/apk-extractor/privacy-policy")
                        )
                    }, border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = "Privacy Policy")
                }
            }
            Column(
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
            ) {
                ListItem(
                    headlineContent = { Text(text = "Analytics") },
                    modifier = Modifier.clickable { analytics = !analytics },
                    supportingContent = {
                        Text(
                            text = "Collects user activity data (like app interactions, screen views), device and network information, crash and performance data, and identifiers (like Instance ID or Android Advertising ID). To understand user behavior, improve user experience, and optimize app performance."
                        )
                    },
                    trailingContent = {
                        Switch(checked = analytics, onCheckedChange = { analytics = it })
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                ListItem(
                    headlineContent = { Text(text = "Crashlytics") },
                    modifier = Modifier.clickable { crashlytics = !crashlytics },
                    supportingContent = {
                        Text(
                            text = "Collects crash reports, device and system data, user actions leading up to the crash, and app-specific log data. To monitor and resolve app crashes and improve app stability."
                        )
                    },
                    trailingContent = {
                        Switch(checked = crashlytics, onCheckedChange = { crashlytics = it })
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                ListItem(
                    headlineContent = { Text(text = "Performance") },
                    modifier = Modifier.clickable { performance = !performance },
                    supportingContent = {
                        Text(
                            text = "Collects performance metrics like app start time, network request details, and user interaction times, device and network information. To monitor app performance, identify bottlenecks, and optimize user experience."
                        )
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