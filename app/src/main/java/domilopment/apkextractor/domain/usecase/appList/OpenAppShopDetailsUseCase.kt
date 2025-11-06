package domilopment.apkextractor.domain.usecase.appList

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import domilopment.apkextractor.R
import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.utils.MySnackbarVisuals
import domilopment.apkextractor.utils.PackageName
import domilopment.apkextractor.utils.Utils
import timber.log.Timber

interface OpenAppShopDetailsUseCase {
    operator fun invoke(
        appDetailsModel: ApplicationModel, showSnackbar: (MySnackbarVisuals) -> Unit
    )
}

class OpenAppShopDetailsUseCaseImpl(private val context: Context) : OpenAppShopDetailsUseCase {
    /**
     * Creates ACTION_VIEW intent for app, opening its store page and launches it
     * if it could not be launched, creates Snackbar with error message
     * Supports Google Play Store, Samsung Galaxy Store and Amazon Appstore for now
     * other installation Sources just call market uri
     * @param showSnackbar function triggering a snackbar host to show a message
     */
    private fun openShop(
        packageName: PackageName,
        installationSource: PackageName,
        showSnackbar: (MySnackbarVisuals) -> Unit
    ) {
        installationSource.also {
            try {
                val shopIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = "${Utils.listOfKnownStores.getValue(it)}${packageName}".toUri()
                    if (it in Utils.listOfKnownStores) setPackage(it)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(shopIntent)
            } catch (e: ActivityNotFoundException) {
                Timber.tag("OpenAppShopDetailsUseCase")
                    .e("openShop: $installationSource; with error: $e")
                showSnackbar(
                    MySnackbarVisuals(
                        duration = SnackbarDuration.Short,
                        message = context.getString(R.string.snackbar_no_activity_for_market_intent),
                        messageColor = Color.Red
                    )
                )
            }
        }
    }

    override fun invoke(
        appDetailsModel: ApplicationModel, showSnackbar: (MySnackbarVisuals) -> Unit
    ) {
        appDetailsModel.installationSource?.let {
            openShop(
                appDetailsModel.appPackageName, it, showSnackbar
            )
        }
    }
}