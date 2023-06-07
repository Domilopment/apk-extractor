package domilopment.apkextractor.utils.eventHandler

object EventDispatcher {
    private val observers: Map<EventType, MutableList<Observer>> = mapOf(
        EventType.SAVED to mutableListOf(),
        EventType.DELETED to mutableListOf(),
        EventType.INSTALLED to mutableListOf(),
        EventType.UNINSTALLED to mutableListOf()
    )

    fun registerObserver(observer: Observer, eventType: EventType) {
        if (eventType == EventType.ANY) observers.forEach { (_, u) -> u.addObserver(observer) }
        else observers[eventType]!!.addObserver(observer)
    }

    fun registerObserver(observer: Observer, vararg eventType: EventType) {
        eventType.forEach { registerObserver(observer, it) }
    }

    fun unregisterObserver(observer: Observer, eventType: EventType) {
        if (eventType == EventType.ANY) observers.forEach { (_, u) -> u.removeObserver(observer) }
        else observers[eventType]!!.removeObserver(observer)
    }

    fun emitEvent(event: Event<*>) {
        if (event.eventType == EventType.ANY) observers.forEach { (_, observer) ->
            observer.forEach {
                it.onEventReceived(event)
            }
        } else observers[event.eventType]!!.forEach { observer ->
            observer.onEventReceived(event)
        }
    }
}

private fun MutableList<Observer>.removeObserver(observer: Observer) {
    this.removeIf { it.key == observer.key }
}

private fun MutableList<Observer>.addObserver(observer: Observer) {
    if (this.any { it.key == observer.key }) return
    else this.add(observer)
}