package domilopment.apkextractor.utils.settings

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import domilopment.apkextractor.data.apkList.PackageArchiveModel
import java.io.File

object PackageArchiveUtils {
    /**
     * Sorts Data by user selected Order
     * @param data Unsorted List of APKs
     * @return Sorted List of APKs
     */
    fun sortApkData(
        data: List<PackageArchiveModel>, sortMode: ApkSortOptions
    ): List<PackageArchiveModel> {
        return data.sortedWith(sortMode.comparator)
    }

    fun getPackageInfoFromApkFile(packageManager: PackageManager, apkFile: File): PackageInfo? {
        val archiveInfo =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) packageManager.getPackageArchiveInfo(
                apkFile.path, PackageManager.PackageInfoFlags.of(0L)
            ) else packageManager.getPackageArchiveInfo(apkFile.path, 0)
         return archiveInfo?.apply {
            applicationInfo.sourceDir = apkFile.path
            applicationInfo.publicSourceDir = apkFile.path
        }
    }
}