package domilopment.apkextractor.data.repository.tasks

import domilopment.apkextractor.data.model.tasks.ProgressError
import domilopment.apkextractor.data.model.tasks.ProgressResult
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.Uuid

@Singleton
class TaskEngine @Inject constructor(
    private val store: TaskStore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val queue = Channel<TaskActor<*>>(capacity = 64)
    private val activeTasks = ConcurrentHashMap<Uuid, TaskActor<*>>()

    companion object {
        private const val MAX_CONCURRENT_TASKS = 3
    }

    init {
        repeat(MAX_CONCURRENT_TASKS) {
            scope.launch {
                for (task in queue) {
                    execute(task)
                }
            }
        }
    }

    suspend fun <T> launch(
        initial: TaskState.Progress,
        block: suspend TaskScope.() -> ProgressResult<T>,
    ): Uuid {
        val id = Uuid.random()
        val task = TaskActor(
            id = id,
            initial = initial,
            store = store,
            block = block,
        )

        store.set(id, TaskState.Pending)
        activeTasks[id] = task

        try {
            queue.send(task)
        } catch (e: Exception) {
            task.cancel()
            activeTasks.remove(id, task)
            throw e
        }

        return id
    }

    private suspend fun execute(task: TaskActor<*>) {
        try {
            task.run()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.tag("TaskEngine").e(e, "Unexpected failure for task ${task.id}")
        } finally {
            activeTasks.remove(task.id, task)
        }
    }

    suspend fun cancel(id: Uuid) {
        activeTasks[id]?.cancel()
        activeTasks.remove(id)
    }

    private enum class TaskPhase {
        PENDING, RUNNING, FINISHED
    }

    private inner class TaskActor<T>(
        val id: Uuid,
        private val initial: TaskState.Progress,
        private val store: TaskStore,
        private val block: suspend TaskScope.() -> ProgressResult<T>,
    ) {
        private val lock = Any()
        private var phase = TaskPhase.PENDING

        private val job = scope.launch(start = CoroutineStart.LAZY) {
            val taskScope = object : TaskScope {
                override suspend fun updateProgress(
                    update: (TaskState.Progress) -> TaskState.Progress
                ) {
                    currentCoroutineContext().ensureActive()
                    synchronized(lock) {
                        check(phase == TaskPhase.RUNNING) {
                            "Task $id is not running: $phase"
                        }

                        store.transition(id) { current ->
                            val progress = current as? TaskState.Progress
                            progress?.let(update) ?: current
                        }
                    }
                    currentCoroutineContext().ensureActive()
                }
            }

            try {
                val result = block(taskScope)
                currentCoroutineContext().ensureActive()
                synchronized(lock) {
                    if (phase == TaskPhase.RUNNING) {
                        store.transition(id) {
                            TaskState.Result.Success(result)
                        }
                        phase = TaskPhase.FINISHED
                    }
                }
            } catch (e: CancellationException) {
                synchronized(lock) {
                    if (phase == TaskPhase.RUNNING) {
                        store.transition(id) {
                            TaskState.Suspended
                        }
                        phase = TaskPhase.FINISHED
                    }
                }
                throw e
            } catch (e: Exception) {
                synchronized(lock) {
                    if (phase == TaskPhase.RUNNING) {
                        store.transition(id) {
                            TaskState.Result.Error(
                                ProgressError(e.message, e)
                            )
                        }
                        phase = TaskPhase.FINISHED
                    }
                }
            } finally {
                synchronized(lock) {
                    phase = TaskPhase.FINISHED
                }
            }
        }

        suspend fun run() {
            synchronized(lock) {
                if (phase != TaskPhase.PENDING) return

                store.transition(id) {
                    initial
                }

                phase = TaskPhase.RUNNING
                job.start()
            }

            job.join()
        }

        suspend fun cancel() {
            val shouldCancel = synchronized(lock) {
                if (phase == TaskPhase.FINISHED) {
                    false
                } else {
                    phase = TaskPhase.FINISHED
                    store.remove(id)
                    true
                }
            }

            if (shouldCancel) {
                job.cancelAndJoin()
            }
        }
    }
}