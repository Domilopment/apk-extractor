package domilopment.apkextractor.data.repository.tasks

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.Uuid

@Singleton
class TaskStore @Inject constructor() {
    private val _state = MutableStateFlow<Map<Uuid, TaskState>>(emptyMap())
    val state = _state.asStateFlow()

    fun set(id: Uuid, initialValue: TaskState) {
        _state.update { current ->
            current + (id to initialValue)
        }
    }

    fun transition(id: Uuid, transform: (TaskState) -> TaskState) {
        _state.update { current ->
            val previous = current[id] ?: return@update current
            if (previous is TaskState.Result || previous == TaskState.Canceled) return@update current
            current + (id to transform(previous))
        }
    }

    fun remove(id: Uuid) {
        _state.update { current ->
            current - id
        }
    }

    fun peek(id: Uuid): TaskState? = _state.value[id]

    fun get(id: Uuid): Flow<TaskState?> = _state.map { it[id] }.transformWhile { value ->
        emit(value)
        value != null && value != TaskState.Canceled && value !is TaskState.Result
    }
}