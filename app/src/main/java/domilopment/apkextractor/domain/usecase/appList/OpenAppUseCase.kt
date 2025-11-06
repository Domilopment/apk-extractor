package domilopment.apkextractor.domain.usecase.appList

import android.content.Context
import android.content.Intent
import domilopment.apkextractor.data.model.appList.ApplicationModel

interface OpenAppUseCase {
    operator fun invoke(appDetailsModel: ApplicationModel)
}

class OpenAppUseCaseImpl(private val context: Context): OpenAppUseCase {
    /**
     * Launch App via Intent
     */
    private fun openApp(launchIntent: Intent) {
        try {
            context.startActivity(launchIntent)
        } catch (_: SecurityException) {
            // Permission denial
        }
    }

    override fun invoke(appDetailsModel: ApplicationModel) {
        appDetailsModel.launchIntent?.let { openApp(it) }
    }
}