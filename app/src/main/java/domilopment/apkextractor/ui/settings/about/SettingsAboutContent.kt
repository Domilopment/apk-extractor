package domilopment.apkextractor.ui.settings.about

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import domilopment.apkextractor.BuildConfig
import domilopment.apkextractor.R
import domilopment.apkextractor.ui.settings.preferences.Preference
import domilopment.apkextractor.ui.settings.preferences.preferenceCategoryItemBottom
import domilopment.apkextractor.ui.settings.preferences.preferenceCategoryItemMiddle
import domilopment.apkextractor.ui.settings.preferences.preferenceCategoryItemTop

@Composable
fun SettingsAboutContent(
    onPrivacyPolicy: () -> Unit,
    onTerms: () -> Unit,
    ossDependencies: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.testTag("SettingsLazyColumn"),
        state = rememberLazyListState(),
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
            .union(WindowInsets(left = 8.dp, right = 8.dp)).asPaddingValues()
    ) {
        preferenceCategoryItemTop {
            Preference(
                name = R.string.privacy_policy_title,
                icon = Icons.Default.PrivacyTip,
                onClick = onPrivacyPolicy
            ) {
                Icon(imageVector = Icons.AutoMirrored.Default.OpenInNew, contentDescription = null)
            }
        }
        preferenceCategoryItemMiddle {
            Preference(
                name = R.string.terms_title, icon = Icons.Default.Info, onClick = onTerms
            ) {
                Icon(imageVector = Icons.AutoMirrored.Default.OpenInNew, contentDescription = null)
            }
        }
        preferenceCategoryItemMiddle {
            Preference(
                name = stringResource(id = R.string.oss_dependencies_title),
                icon = Icons.Default.Code,
                onClick = ossDependencies
            )
        }
        preferenceCategoryItemBottom {
            Preference(
                name = stringResource(
                    id = R.string.version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE
                ),
                enabled = false,
                onClick = {},
            )
        }
    }
}
