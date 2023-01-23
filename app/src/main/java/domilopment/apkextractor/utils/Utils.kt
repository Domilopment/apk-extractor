package domilopment.apkextractor.utils

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

object Utils {
    @Throws(PackageManager.NameNotFoundException::class)
    fun getPackageInfo(packageManager: PackageManager, packageName: String): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) packageManager.getPackageInfo(
            packageName, PackageManager.PackageInfoFlags.of(0L)
        )
        else packageManager.getPackageInfo(packageName, 0)
    }
}