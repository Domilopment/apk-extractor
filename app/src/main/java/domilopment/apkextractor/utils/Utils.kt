package domilopment.apkextractor.utils

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import androidx.core.text.toSpannable
import domilopment.apkextractor.data.ApplicationModel
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    @Throws(PackageManager.NameNotFoundException::class)
    fun getPackageInfo(packageManager: PackageManager, packageName: String): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) packageManager.getPackageInfo(
            packageName, PackageManager.PackageInfoFlags.of(0L)
        )
        else packageManager.getPackageInfo(packageName, 0)
    }

    fun versionCode(packageInfo: PackageInfo): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode
        else packageInfo.versionCode.toLong()
    }

    val listOfKnownStores: Map<String, String> = mapOf(
        "com.android.vending" to "https://play.google.com/store/apps/details?id=",
        "com.sec.android.app.samsungapps" to "samsungapps://ProductDetail/",
        "com.amazon.venezia" to "amzn://apps/android?p=",
        "org.fdroid.fdroid" to "fdroid.app://details?id="
    ).withDefault {
        "market://details?id="
    }

    /**
     * Check if Application is Installed
     * @param packageName
     * Package Name of App
     */
    fun isPackageInstalled(packageManager: PackageManager, packageName: String): Boolean {
        return try {
            getPackageInfo(packageManager, packageName)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Checks if app is a System app
     * @param app
     * App to check
     */
    fun isSystemApp(app: ApplicationModel): Boolean {
        return app.appFlags and ApplicationInfo.FLAG_SYSTEM == ApplicationInfo.FLAG_SYSTEM
    }

    /**
     * Formats a Date-Time string into default Locale format
     * @param mills milliseconds since January 1, 1970, 00:00:00 GMT
     * @return formatted date-time string
     */
    fun getAsFormattedDate(mills: Long): String {
        return SimpleDateFormat.getDateTimeInstance().format(Date(mills))
    }

    /**
     * Creates a String Spannable from text, that shows the position of the search word inside the String
     * @param text text that is displayed to the user
     * @return String spannable with color marked search word
     */
    fun getSpannable(text: CharSequence?, searchString: String, colorInt: Int): Spannable? {
        val spannable = text?.toSpannable()
        if (searchString.isNotBlank() && text?.contains(searchString, ignoreCase = true) == true) {
            val startIndex = text.toString().lowercase().indexOf(searchString.lowercase())
            spannable?.setSpan(
                ForegroundColorSpan(colorInt),
                startIndex,
                startIndex + searchString.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannable
    }
}