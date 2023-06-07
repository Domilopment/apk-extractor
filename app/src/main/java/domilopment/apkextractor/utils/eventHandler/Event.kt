package domilopment.apkextractor.utils.eventHandler

data class Event<T>(val eventType: EventType, val data: T)