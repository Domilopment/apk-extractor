package domilopment.apkextractor.data.repository.tasks

import domilopment.apkextractor.data.model.tasks.ProgressError
import domilopment.apkextractor.data.model.tasks.ProgressResult
import domilopment.apkextractor.data.model.tasks.TaskFailure
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
    private val queue = Channel<Uuid>(capacity = 64)
    private val activeTasks = ConcurrentHashMap<Uuid, TaskActor<*>>()

    companion object {
        private const val MAX_CONCURRENT_TASKS = 3
        private const val TAG = "TaskEngine"
    }

    init {
        repeat(MAX_CONCURRENT_TASKS) {
            scope.launch {
                for (taskId in queue) {
                    activeTasks[taskId]?.let { task -> execute(task) }
                }
            }
        }
    }

    suspend fun <T> launch(
        initial: TaskState.Progress,
        executionPolicy: TaskExecutionPolicy = TaskExecutionPolicy.QUEUED,
        block: suspend TaskScope.() -> ProgressResult<T>,
    ): Uuid {
        val id = Uuid.random()
        Timber.tag(TAG).d("Launching task $id with policy $executionPolicy")

        val task = TaskActor(
            id = id,
            initial = initial,
            store = store,
            block = block,
        )

        store.set(id, TaskState.Pending)
        activeTasks[id] = task

        try {
            when (executionPolicy) {
                TaskExecutionPolicy.QUEUED -> {
                    queue.send(task.id)
                    Timber.tag(TAG).d("Task $id enqueued")
                }

                TaskExecutionPolicy.IMMEDIATE -> {
                    Timber.tag(TAG).d("Executing task $id immediately")
                    scope.launch { execute(task) }
                }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to dispatch task $id")
            task.cancel()
            activeTasks.remove(id, task)
            throw e
        }

        return id
    }

    private suspend fun <T> execute(task: TaskActor<T>) {
        try {
            task.run()
        } catch (e: CancellationException) {
            Timber.tag(TAG).d("Task ${task.id} execution canceled")
            throw e
        } catch (e: Exception) {
            // Fallback for unexpected exceptions escaping run()
            Timber.tag(TAG).e(e, "Unexpected failure in execute() for task ${task.id}")
        } finally {
            activeTasks.remove(task.id, task)
        }
    }

    suspend fun cancel(id: Uuid) {
        Timber.tag(TAG).d("Requesting cancellation for task $id")
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

                override fun fail(failure: TaskFailure): Nothing {
                    throw TaskFailureException(failure)
                }
            }

            try {
                Timber.tag(TAG).d("Task $id started running block")
                val result = block(taskScope)
                currentCoroutineContext().ensureActive()
                synchronized(lock) {
                    if (phase == TaskPhase.RUNNING) {
                        store.transition(id) {
                            TaskState.Result.Success(result)
                        }
                        phase = TaskPhase.FINISHED
                        Timber.tag(TAG).d("Task $id completed successfully")
                    }
                }
            } catch (e: CancellationException) {
                Timber.tag(TAG).d("Task $id was cancelled during execution")
                synchronized(lock) {
                    if (phase == TaskPhase.RUNNING) {
                        store.transition(id) {
                            TaskState.Canceled
                        }
                        phase = TaskPhase.FINISHED
                    }
                }
                throw e
            } catch (e: TaskFailureException) {
                Timber.tag(TAG).w(e, "Task $id failed with domain error: ${e.failure.errorMessage}")
                synchronized(lock) {
                    if (phase == TaskPhase.RUNNING) {
                        store.transition(id) {
                            TaskState.Result.Error(e.failure)
                        }
                        phase = TaskPhase.FINISHED
                    }
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Task $id failed with unexpected exception")
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
                Timber.tag(TAG).d("Task $id successfully cancelled and joined")
            }
        }
    }
}