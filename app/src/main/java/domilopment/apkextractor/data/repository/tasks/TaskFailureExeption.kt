package domilopment.apkextractor.data.repository.tasks

import domilopment.apkextractor.data.model.tasks.TaskFailure

internal class TaskFailureException(val failure: TaskFailure) :
    Exception(failure.errorMessage, failure.throwable)