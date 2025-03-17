package domilopment.apkextractor.ui

import kotlinx.serialization.Serializable

sealed interface Graph {
    @Serializable
    data object Settings : Graph
}