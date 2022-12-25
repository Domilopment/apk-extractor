package domilopment.apkextractor.data

data class ProgressDialogUiState(
    val title: String? = null,
    val process: String? = null,
    val progress: Int = 0,
    val tasks: Int = 0,
    val shouldBeShown: Boolean = false
)