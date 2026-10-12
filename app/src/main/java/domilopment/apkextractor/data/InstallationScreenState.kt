package domilopment.apkextractor.data

import domilopment.apkextractor.ui.model.installation.InstallationResultType

data class InstallationScreenState(val progressState: TaskUiState.Progress?, val result: InstallationResultType?)
