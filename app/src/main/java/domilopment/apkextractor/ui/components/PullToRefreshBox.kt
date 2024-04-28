package domilopment.apkextractor.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

@Preview
@Composable
private fun PullToRefreshBoxPreview() {
    val list = remember { mutableListOf(*(1..10).toList().toTypedArray()) }
    var isRefreshing by remember {
        mutableStateOf(false)
    }
    val scope = rememberCoroutineScope()

    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = {
        scope.launch {
            isRefreshing = true
            delay(1000)
            for (i in list.size + 1..list.size + 10) list.add(i)
            isRefreshing = false
        }
    }) {
        LazyColumn {
            items(list) {
                ListItem(headlineContent = { Text(text = "Item $it") })
            }
        }
    }
}