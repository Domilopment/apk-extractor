package domilopment.apkextractor.utils.eventHandler

interface Observer {
    val key: String
    fun onEventReceived(event: Event<*>)
}