package domilopment.apkextractor.ui.settings.preferences

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.preferenceCategory(
    @StringRes title: Int,
    key: Any? = null,
    contentType: Any? = null,
    items: LazyListScope.() -> Unit
) {
    stickyHeader(key, contentType) {
        Column(
            Modifier
                .background(
                    MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(bottomEnd = 8.dp)
                )
                .padding(vertical = 8.dp)
                .padding(end = 8.dp)
        ) {
            Text(text = stringResource(id = title))
        }
    }
    items()
}

fun LazyListScope.preferenceCategoryItemSingle(
    key: Any? = null, contentType: Any? = null, item: @Composable LazyItemScope.() -> Unit
) {
    item(key, contentType) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
        ) {
            item()
        }
    }
}

fun LazyListScope.preferenceCategoryItemTop(
    key: Any? = null, contentType: Any? = null, item: @Composable LazyItemScope.() -> Unit
) {
    item(key, contentType) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        ) {
            item()
        }
    }
}

fun LazyListScope.preferenceCategoryItemMiddle(
    key: Any? = null, contentType: Any? = null, item: @Composable LazyItemScope.() -> Unit
) {
    item(key, contentType) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
            shape = RectangleShape,
        ) {
            item()
        }
    }
}

fun LazyListScope.preferenceCategoryItemBottom(
    key: Any? = null, contentType: Any? = null, item: @Composable LazyItemScope.() -> Unit
) {
    item(key, contentType) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(bottomEnd = 8.dp, bottomStart = 8.dp),
        ) {
            item()
        }
    }
}