package domilopment.apkextractor.domain.usecase.appList

import android.content.Context
import android.content.Intent
import android.net.Uri
import domilopment.apkextractor.InstallerActivity
import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.utils.PackageName
import kotlin.jvm.java

interface UninstallAppUseCase {
    operator fun invoke(appDetailsModel: ApplicationModel)
}

class UninstallAppUseCaseImpl(private val context: Context) : UninstallAppUseCase {
    /**
     * Creates an Intent to delete selected app
     */
    private fun uninstallApp(packageName: PackageName) {
        Intent(context, InstallerActivity::class.java).apply {
            data = Uri.fromParts("package", packageName, null)
            setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
        }.let { intent ->
            context.startActivity(intent)
        }
    }

    override fun invoke(appDetailsModel: ApplicationModel) {
        uninstallApp(appDetailsModel.appPackageName)
    }
}