import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object VersionUtils {
    fun buildDate(): String = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())
}