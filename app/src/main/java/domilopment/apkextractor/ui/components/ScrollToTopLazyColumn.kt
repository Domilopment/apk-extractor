package domilopment.apkextractor.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
private fun ScrollToTopButton(
    visible: Boolean, modifier: Modifier = Modifier, onScrollToTop: () -> Unit
) {
    AnimatedVisibility(
        visible = visible, modifier = modifier, enter = fadeIn(), exit = fadeOut()
    ) {
        IconButton(
            onClick = onScrollToTop,
            modifier = Modifier
                .padding(8.dp)
                .shadow(elevation = 16.dp, shape = CircleShape),
            colors = IconButtonDefaults.iconButtonColors().copy(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "ScrollToTop Button"
            )
        }
    }
}

@Composable
fun ScrollToTopLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    scope: CoroutineScope = rememberCoroutineScope(),
    content: LazyListScope.() -> Unit
) {
    val scrollToTop by remember {
        derivedStateOf {
            state.firstVisibleItemIndex > 0
        }
    }

    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state,
            contentPadding = PaddingValues(bottom = 54.dp)
        ) {
            this.content()
        }

        ScrollToTopButton(
            visible = scrollToTop, modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            scope.launch { state.scrollToItem(0) }
        }
    }
}

@Preview
@Composable
private fun ScrollToTopLazyColumnPreview() {
    val list = remember { (1..20).toList() }
    ScrollToTopLazyColumn {
        items(list) {
            ListItem(headlineContent = { Text(text = "Item $it") })
        }
    }
}