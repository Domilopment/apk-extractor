package domilopment.apkextractor.data.repository.analytics

interface AnalyticsRepository {
    fun setAnalyticsCollectionEnabled(boolean: Boolean)
    fun setCrashlyticsCollectionEnabled(boolean: Boolean)
    fun setPerformanceCollectionEnabled(boolean: Boolean)
    suspend fun delete()
}
