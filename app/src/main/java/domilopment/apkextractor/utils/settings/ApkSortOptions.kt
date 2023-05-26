package domilopment.apkextractor.utils.settings

import domilopment.apkextractor.data.PackageArchiveModel

enum class ApkSortOptions {
    SORT_BY_FILE_NAME_ASC {
        override val comparator = compareBy(PackageArchiveModel::fileName)
    },
    SORT_BY_FILE_NAME_DESC {
        override val comparator = compareByDescending(PackageArchiveModel::fileName)
    },
    SORT_BY_FILE_SIZE_ASC {
        override val comparator = compareBy(PackageArchiveModel::fileSize)
    },
    SORT_BY_FILE_SIZE_DESC {
        override val comparator = compareByDescending(PackageArchiveModel::fileSize)
    },
    SORT_BY_LAST_MODIFIED_ASC {
        override val comparator = compareBy(PackageArchiveModel::fileLastModified)
    },
    SORT_BY_LAST_MODIFIED_DESC {
        override val comparator = compareByDescending(PackageArchiveModel::fileLastModified)
    };

    abstract val comparator: Comparator<PackageArchiveModel>

    companion object {
        private val apkSortOptionsArray = ApkSortOptions.values().associateBy { it.name }
            .withDefault { SORT_BY_LAST_MODIFIED_DESC }

        operator fun get(sortMode: String?): ApkSortOptions {
            return apkSortOptionsArray.getValue(sortMode.toString())
        }
    }
}