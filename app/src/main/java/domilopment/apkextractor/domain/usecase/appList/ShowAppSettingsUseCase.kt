package domilopment.apkextractor.domain.usecase.appList

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.utils.PackageName

interface ShowAppSettingsUseCase {
    operator fun invoke(appDetailsModel: ApplicationModel)
}

class ShowAppSettingsUseCaseImpl(private val context: Context) : ShowAppSettingsUseCase {
    /**
     * Creates an Intent to open settings page of app and starts it
     */
    private fun showSettings(packageName: PackageName) {
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.let {
            context.startActivity(it)
        }
    }

    override fun invoke(appDetailsModel: ApplicationModel) {
        showSettings(appDetailsModel.appPackageName)
    }
}