package domilopment.apkextractor.ui.settings.dataCollection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import domilopment.apkextractor.ui.viewModels.SettingsScreenViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import domilopment.apkextractor.ui.navigation.Route

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsDataCollectionScreen(
    model: SettingsScreenViewModel,
    onBackClicked: () -> Unit,
) {
    val uiState by model.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = Unit) {
        Route.SettingsDataCollection.buttons.onEach { button ->
            when (button) {
                else -> onBackClicked()
            }
        }.launchIn(this)
    }

    SettingsDataCollectionContent(
        analytics = uiState.analytics,
        onAnalytics = model::setAnalytics,
        crashlytics = uiState.crashlytics,
        onCrashlytics = model::setCrashlytics,
        performance = uiState.performance,
        onPerformance = model::setPerformance,
    )
}
