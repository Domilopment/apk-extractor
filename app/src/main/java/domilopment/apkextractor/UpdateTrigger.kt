package domilopment.apkextractor

open class UpdateTrigger(private val trigger: Boolean) {
    var hasBeenHandaled = false
        private set

    fun handleTrigger(): Boolean {
        return if (hasBeenHandaled)
            false
        else {
            hasBeenHandaled = true
            trigger
        }
    }

    fun lastState(): Boolean = trigger
}