package domilopment.apkextractor.ui.settings.donation

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import domilopment.apkextractor.utils.Constants
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import androidx.core.net.toUri
import domilopment.apkextractor.ui.ScreenConfig
import domilopment.apkextractor.ui.navigation.Route

@Composable
fun SettingsDonationScreen(
    onBackClicked: () -> Unit, context: Context = LocalContext.current
) {
    LaunchedEffect(key1 = Unit) {
        Route.Screen.SettingsDonation.buttons.onEach { button ->
            when (button) {
                ScreenConfig.ScreenActions.NavigationIcon -> onBackClicked()
                else -> Unit
            }
        }.launchIn(this)
    }

    SettingsDonationContent(
        onGithubSponsors = {
            CustomTabsIntent.Builder().build()
                .launchUrl(context, Constants.GITHUB_SPONSORS_URL.toUri())
        },
        onPatreon = {
            CustomTabsIntent.Builder().build().launchUrl(context, Constants.PATREON_URL.toUri())
        },
        onBuyMeACoffee = {
            CustomTabsIntent.Builder().build()
                .launchUrl(context, Constants.BUY_ME_A_COFFEE_URL.toUri())
        },
    )
}
