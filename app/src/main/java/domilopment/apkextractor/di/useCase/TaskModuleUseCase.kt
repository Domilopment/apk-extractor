package domilopment.apkextractor.di.useCase

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import domilopment.apkextractor.data.repository.tasks.TaskEngine
import domilopment.apkextractor.data.repository.tasks.TaskStore
import domilopment.apkextractor.domain.usecase.tasks.CancelTaskUseCase
import domilopment.apkextractor.domain.usecase.tasks.CancelTaskUseCaseImpl
import domilopment.apkextractor.domain.usecase.tasks.DismissTaskUseCase
import domilopment.apkextractor.domain.usecase.tasks.DismissTaskUseCaseImpl
import domilopment.apkextractor.domain.usecase.tasks.GetTaskProgressUseCase
import domilopment.apkextractor.domain.usecase.tasks.GetTaskProgressUseCaseImpl
import domilopment.apkextractor.domain.usecase.tasks.GetTaskResultUseCase
import domilopment.apkextractor.domain.usecase.tasks.GetTaskResultUseCaseImpl
import domilopment.apkextractor.domain.usecase.tasks.LaunchTaskUseCase
import domilopment.apkextractor.domain.usecase.tasks.LaunchTaskUseCaseImpl

@InstallIn(ActivityRetainedComponent::class)
@Module
object TaskModuleUseCase {
    @Provides
    @Reusable
    fun getGetTaskUseCase(
        registry: TaskStore
    ): GetTaskProgressUseCase {
        return GetTaskProgressUseCaseImpl(registry)
    }

    @Provides
    @Reusable
    fun getTaskResultUseCase(
        getTaskProgressUseCase: GetTaskProgressUseCase
    ): GetTaskResultUseCase {
        return GetTaskResultUseCaseImpl(getTaskProgressUseCase)
    }

    @Provides
    @Reusable
    fun getDismissTaskUseCase(
        registry: TaskStore
    ): DismissTaskUseCase {
        return DismissTaskUseCaseImpl(registry)
    }

    @Provides
    @Reusable
    fun launchTaskUseCase(
        engine: TaskEngine
    ): LaunchTaskUseCase {
        return LaunchTaskUseCaseImpl(engine)
    }

    @Provides
    @Reusable
    fun cancelTaskUseCase(
        engine: TaskEngine
    ): CancelTaskUseCase {
        return CancelTaskUseCaseImpl(engine)
    }
}