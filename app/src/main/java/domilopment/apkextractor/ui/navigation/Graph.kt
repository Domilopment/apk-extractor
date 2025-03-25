package domilopment.apkextractor.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Graph {
    @Serializable
    data object Settings : Graph
}