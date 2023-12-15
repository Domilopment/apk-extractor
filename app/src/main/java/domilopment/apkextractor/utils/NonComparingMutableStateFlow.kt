package domilopment.apkextractor.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NonComparingMutableStateFlow<T>(initialValue: T) : MutableStateFlow<T> {
    private val innerFlow = MutableSharedFlow<T>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val subscriptionCount: StateFlow<Int> = innerFlow.subscriptionCount

    override var value: T = initialValue
        set(value) {
            field = value
            innerFlow.tryEmit(value)
        }

    override val replayCache: List<T> = innerFlow.replayCache

    override suspend fun emit(value: T) {
        this.value = value
    }

    override fun compareAndSet(
        expect: T, update: T
    ): Boolean {
        value = update
        return true
    }

    override suspend fun collect(collector: FlowCollector<T>): Nothing =
        innerFlow.collect(collector)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun resetReplayCache() = innerFlow.resetReplayCache()

    override fun tryEmit(value: T): Boolean {
        this.value = value
        return true
    }
}