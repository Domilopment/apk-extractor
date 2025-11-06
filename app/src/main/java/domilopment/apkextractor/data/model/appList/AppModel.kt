package domilopment.apkextractor.data.model.appList

sealed class AppModel(open val packageName: String) {
    data class SystemApp(override val packageName: String): AppModel(packageName)
    data class UpdatedSystemApps(override val packageName: String): AppModel(packageName)
    data class UserApp(override val packageName: String): AppModel(packageName)
}