import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object VersionUtils {
    /**
     * Get the current build date in the format of yyyy.MM.dd
     */
    @OptIn(ExperimentalTime::class)
    fun buildDate(): String = Clock.System.todayIn(TimeZone.currentSystemDefault()).format(
            LocalDate.Format {
                year()
                char('.')
                monthNumber()
                char('.')
                day()
            })
}