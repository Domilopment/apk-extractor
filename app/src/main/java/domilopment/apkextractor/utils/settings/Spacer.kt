package domilopment.apkextractor.utils.settings

import androidx.annotation.StringRes
import domilopment.apkextractor.R

enum class Spacer(val symbol: Char) {
    SPACE(' '),
    UNDERSCORE('_'),
    HYPHEN('-'),
    DOT('.');

    companion object {
        /**
         * Get enum entry by name
         * @param name Name of enum entry
         * @return enum with Spacer option
         */
        fun fromName(name: String): Spacer? {
            return entries.find { it.name == name }
        }
    }
}

@StringRes
fun Spacer.getNameResId(): Int {
    return when (this) {
        Spacer.SPACE -> R.string.app_save_name_part_separator_space
        Spacer.UNDERSCORE -> R.string.app_save_name_part_separator_underscore
        Spacer.HYPHEN -> R.string.app_save_name_part_separator_hyphen
        Spacer.DOT -> R.string.app_save_name_part_separator_dot
    }
}