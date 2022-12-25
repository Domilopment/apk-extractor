package domilopment.apkextractor

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event
 */
open class Event<out T>(private val content: T) {
    var hasBeenHandaled = false
        private set

    /**
     * Returns the content and prevents its use again
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandaled)
            null
        else {
            hasBeenHandaled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled
     */
    fun peekContent(): T = content
}