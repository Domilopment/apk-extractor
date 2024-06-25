package domilopment.apkextractor.utils.settings

import androidx.annotation.StringRes
import domilopment.apkextractor.R
import domilopment.apkextractor.data.room.entities.PackageArchiveEntity

enum class ApkSortOptions {
    SORT_BY_FILE_NAME_ASC {
        override val comparator = compareBy(PackageArchiveEntity::fileName)
        override val displayNameRes: Int = R.string.menu_sort_apk_file_name_asc
    },
    SORT_BY_FILE_NAME_DESC {
        override val comparator = compareByDescending(PackageArchiveEntity::fileName)
        override val displayNameRes: Int = R.string.menu_sort_apk_file_name_desc
    },
    SORT_BY_LAST_MODIFIED_DESC {
        override val comparator = compareByDescending(PackageArchiveEntity::fileLastModified)
        override val displayNameRes: Int = R.string.menu_sort_apk_file_mod_date_desc
    },
    SORT_BY_LAST_MODIFIED_ASC {
        override val comparator = compareBy(PackageArchiveEntity::fileLastModified)
        override val displayNameRes: Int = R.string.menu_sort_apk_file_mod_date_asc
    },
    SORT_BY_FILE_SIZE_DESC {
        override val comparator = compareByDescending(PackageArchiveEntity::fileSize)
        override val displayNameRes: Int = R.string.menu_sort_apk_file_size_desc
    },
    SORT_BY_FILE_SIZE_ASC {
        override val comparator = compareBy(PackageArchiveEntity::fileSize)
        override val displayNameRes: Int = R.string.menu_sort_apk_file_size_asc
    };

    abstract val comparator: Comparator<PackageArchiveEntity>

    @get:StringRes
    abstract val displayNameRes: Int

    companion object {
        private val apkSortOptionsArray =
            entries.associateBy { it.name }.withDefault { SORT_BY_LAST_MODIFIED_DESC }

        operator fun get(sortMode: String?): ApkSortOptions {
            return apkSortOptionsArray.getValue(sortMode.toString())
        }
    }
}