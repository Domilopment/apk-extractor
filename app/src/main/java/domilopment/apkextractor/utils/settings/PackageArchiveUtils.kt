package domilopment.apkextractor.utils.settings

import domilopment.apkextractor.data.PackageArchiveModel

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
}