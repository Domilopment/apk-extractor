package domilopment.apkextractor.utils

import android.app.ForegroundServiceStartNotAllowedException
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import domilopment.apkextractor.data.model.appList.ApplicationModel
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
     * Creates an Annotated from text, that shows the position of the search word inside the String
     * @param text text that is displayed to the user
     * @param searchString the text to be contained in parameter text
     * @param color color to highlight text in parameter text
     * @return Annotated String with color marked search word
     */
    fun getAnnotatedString(
        text: CharSequence?, searchString: String?, color: Color
    ): AnnotatedString? {
        return if (text == null) null
        else if (!searchString.isNullOrBlank() && text.contains(
                searchString, ignoreCase = true
            )
        ) {
            val startIndex = text.toString().lowercase().indexOf(searchString.lowercase())
            val endIndex = startIndex + searchString.length

            AnnotatedString(
                text.toString(),
                listOf(AnnotatedString.Range(SpanStyle(color), startIndex, endIndex))
            )
        } else AnnotatedString(text.toString())
    }

    val androidApiLevel: Map<Int, String> = mapOf(
        1 to "1.0",
        2 to "1.1",
        3 to "1.5",
        4 to "1.6",
        5 to "2.0",
        6 to "2.0.1",
        7 to "2.1",
        8 to "2.2",
        9 to "2.3.0 - 2.3.2",
        10 to "2.3.3 - 2.3.7",
        11 to "3.0",
        12 to "3.1",
        13 to "3.2",
        14 to "4.0.1 - 4.0.2",
        15 to "4.0.3 - 4.0.4",
        16 to "4.1",
        17 to "4.2",
        18 to "4.3",
        19 to "4.4",
        20 to "4.4W",
        21 to "5.0",
        22 to "5.1",
        23 to "6.0",
        24 to "7.0",
        25 to "7.1",
        26 to "8.0",
        27 to "8.1",
        28 to "9",
        29 to "10",
        30 to "11",
        31 to "12",
        32 to "12L",
        33 to "13",
        34 to "14",
        35 to "15",
    )

    /**
     * Start ForegroundService with intent and Catch/Notify user when StartNotAllowed exception was thrown
     * @param context
     * @param intent
     * @return Boolean who tells if Service could be Started or not
     */
    fun startForegroundService(context: Context, intent: Intent): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                context.startForegroundService(intent)
            } catch (e: ForegroundServiceStartNotAllowedException) {
                return false
            }
        } else {
            context.startForegroundService(intent)
        }

        return true
    }
}