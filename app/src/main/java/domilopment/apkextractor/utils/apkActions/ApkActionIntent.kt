package domilopment.apkextractor.utils.apkActions

import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.utils.MySnackbarVisuals

data class ApkActionIntentParams(val showSnackbar: ((MySnackbarVisuals) -> Unit)? = null)

sealed class ApkActionIntent(
    open val app: ApplicationModel?, val params: ApkActionIntentParams? = null
) {
    data class Save(override val app: ApplicationModel) : ApkActionIntent(app)
    data class Share(override val app: ApplicationModel) : ApkActionIntent(app)
    data class Icon(
        override val app: ApplicationModel, val showSnackbar: (MySnackbarVisuals) -> Unit
    ) : ApkActionIntent(app, ApkActionIntentParams(showSnackbar))

    data class Settings(override val app: ApplicationModel) : ApkActionIntent(app)
    data class Open(override val app: ApplicationModel) : ApkActionIntent(app)
    data class Uninstall(override val app: ApplicationModel) : ApkActionIntent(app)
    data class StorePage(
        override val app: ApplicationModel, val showSnackbar: (MySnackbarVisuals) -> Unit
    ) : ApkActionIntent(app, ApkActionIntentParams(showSnackbar))

    data object None : ApkActionIntent(null)
}