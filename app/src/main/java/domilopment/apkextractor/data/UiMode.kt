package domilopment.apkextractor.data

sealed interface UiMode {
    data object Home: UiMode
    data object Search: UiMode
    data class Action(val prevMode: UiMode): UiMode
}