package domilopment.apkextractor.data

sealed interface MainScreenState {
    sealed interface AppBarMode : MainScreenState {
        data object Default : AppBarMode
        data class Search(val query: String = "") : AppBarMode
    }

    data class ActionMode(
        val returnTo: AppBarMode,
        val isSelectAllActive: Boolean = false,
        val selectedItemCount: Int = 0
    ) : MainScreenState
}
