package domilopment.apkextractor.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    isPullToRefreshEnabled: Boolean = true,
    content: @Composable() (BoxScope.() -> Unit)
) {
    val state = rememberPullToRefreshState(enabled = { isPullToRefreshEnabled })

    Box(modifier = modifier.nestedScroll(state.nestedScrollConnection)) {
        this.content()

        if (state.isRefreshing) {
            LaunchedEffect(true) {
                onRefresh()
            }
        }

        LaunchedEffect(isRefreshing) {
            if (isRefreshing) state.startRefresh()
            else state.endRefresh()
        }

        PullToRefreshContainer(state = state, modifier = Modifier.align(Alignment.TopCenter))
    }
}