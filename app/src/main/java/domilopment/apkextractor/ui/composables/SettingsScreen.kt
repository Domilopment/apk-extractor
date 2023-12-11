package domilopment.apkextractor.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidViewBinding
import domilopment.apkextractor.databinding.SettingsFragmentBinding
import domilopment.apkextractor.ui.viewModels.SettingsFragmentViewModel

@Composable
fun SettingsScreen(model: SettingsFragmentViewModel) {
    AndroidViewBinding(SettingsFragmentBinding::inflate)
}