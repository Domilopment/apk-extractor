package domilopment.apkextractor.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import domilopment.apkextractor.ui.components.ApkExtractorBottomSheet
import domilopment.apkextractor.utils.settings.ApkSortOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApkSortMenu(
    sortOrder: ApkSortOptions,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState,
    sort: (ApkSortOptions) -> Unit
) {
    if (expanded) ApkExtractorBottomSheet(
        onDismissRequest = onDismissRequest, modifier = modifier, sheetState = sheetState
    ) {
        ApkSortOptions.entries.forEach {
            ListItem(headlineContent = { Text(text = stringResource(id = it.displayNameRes)) },
                modifier = Modifier.clickable {
                    sort(it)
                },
                leadingContent = {
                    RadioButton(
                        selected = sortOrder == it, onClick = null
                    )
                })
        }
    }
}