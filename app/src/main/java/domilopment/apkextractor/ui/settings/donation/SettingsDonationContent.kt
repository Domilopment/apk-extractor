package domilopment.apkextractor.ui.settings.donation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import domilopment.apkextractor.R
import domilopment.apkextractor.ui.settings.preferences.Preference
import domilopment.apkextractor.ui.settings.preferences.preferenceCategoryItemBottom
import domilopment.apkextractor.ui.settings.preferences.preferenceCategoryItemMiddle
import domilopment.apkextractor.ui.settings.preferences.preferenceCategoryItemTop
import domilopment.apkextractor.ui.tabletLazyListInsets

@Composable
fun SettingsDonationContent(
    onGithubSponsors: () -> Unit,
    onPatreon: () -> Unit,
    onBuyMeACoffee: () -> Unit,
) {
    LazyColumn(
        state = rememberLazyListState(), contentPadding = WindowInsets.tabletLazyListInsets.union(
            WindowInsets(left = 8.dp, right = 8.dp)
        ).asPaddingValues()
    ) {
        item {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Red
                )
                Text(
                    text = stringResource(id = R.string.donations_page_header),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(id = R.string.donations_page_text),
                    textAlign = TextAlign.Center,
                )
            }
        }
        preferenceCategoryItemTop {
            Preference(
                name = R.string.donations_github_sponsors,
                icon = ImageVector.vectorResource(id = R.drawable.github_mark_white),
                onClick = onGithubSponsors
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.OpenInNew, contentDescription = null
                )
            }
        }
        preferenceCategoryItemMiddle {
            Preference(
                name = R.string.donations_patreon,
                icon = ImageVector.vectorResource(id = R.drawable.patreon_symbol_1_white_rgb),
                onClick = onPatreon
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.OpenInNew, contentDescription = null
                )
            }
        }
        preferenceCategoryItemBottom {
            Preference(
                name = R.string.donations_bmc,
                icon = ImageVector.vectorResource(id = R.drawable.bmc_logo),
                onClick = onBuyMeACoffee
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.OpenInNew, contentDescription = null
                )
            }
        }
    }
}
