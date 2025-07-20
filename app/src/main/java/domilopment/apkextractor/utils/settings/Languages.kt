package domilopment.apkextractor.utils.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import domilopment.apkextractor.R
import java.util.Locale

/**
 * List of supported languages.
 * @param languageTag The language tag of the language.
 * @param locale The locale of the language.
 */
enum class Languages(val languageTag: String, val locale: Locale) {
    SYSTEM("default", Locale.ROOT),
    ENGLISH("en", Locale.ENGLISH),
    GERMANY("de-DE", Locale.GERMANY),
    TURKISH("tr", Locale.forLanguageTag("tr"));

    companion object {
        /**
         * Get the language by its language tag.
         * @param languageTag The language tag of the language.
         * @return The language or null if not found.
         */
        fun getSupportedLanguageByTagOrNull(languageTag: String): Languages? {
            return entries.find { it.languageTag == languageTag }
        }
    }
}

/**
 * Get the display string of the language.
 * @return The display string of the language.
 */
@Composable
@ReadOnlyComposable
fun Languages.getDisplayString(): String {
    return when (this) {
        Languages.SYSTEM -> stringResource(id = R.string.locale_list_default)
        else -> this.locale.getDisplayName(this.locale)
    }
}