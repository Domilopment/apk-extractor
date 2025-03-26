package domilopment.apkextractor.ui.settings.about

import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import domilopment.apkextractor.utils.Constants
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import androidx.core.net.toUri
import domilopment.apkextractor.ui.Screen
import domilopment.apkextractor.ui.navigation.Route

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsAboutScreen(
    onBackClicked: () -> Unit, context: Context = LocalContext.current
) {
    LaunchedEffect(key1 = Unit) {
        Route.SettingsAbout.buttons.onEach { button ->
            when (button) {
                Screen.ScreenActions.NavigationIcon -> onBackClicked()
                else -> Unit
            }
        }.launchIn(this)
    }

    SettingsAboutContent(onPrivacyPolicy = {
        CustomTabsIntent.Builder().build().launchUrl(context, Constants.PRIVACY_POLICY_URL.toUri())
    }, onTerms = {
        CustomTabsIntent.Builder().build().launchUrl(context, Constants.TERMS_URL.toUri())
    }, ossDependencies = {
        context.startActivity(
            Intent(context.applicationContext, OssLicensesMenuActivity::class.java)
        )
    })
}
