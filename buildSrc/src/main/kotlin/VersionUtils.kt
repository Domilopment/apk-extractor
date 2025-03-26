import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object VersionUtils {
    /**
     * Get the current build date in the format of yyyy.MM.dd
     */
    fun buildDate(): String = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())
}