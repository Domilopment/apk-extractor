package domilopment.apkextractor.ui.dialogs

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import domilopment.apkextractor.R
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
    if (expanded) ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        windowInsets = WindowInsets(bottom = 24.dp)
    ) {
        DropdownMenuItem(text = { Text(text = stringResource(id = R.string.menu_sort_apk_file_name_asc)) },
            onClick = {
                sort(ApkSortOptions.SORT_BY_FILE_NAME_ASC)
            },
            leadingIcon = {
                RadioButton(
                    selected = sortOrder == ApkSortOptions.SORT_BY_FILE_NAME_ASC, onClick = null
                )
            })
        DropdownMenuItem(text = { Text(text = stringResource(id = R.string.menu_sort_apk_file_name_desc)) },
            onClick = {
                sort(ApkSortOptions.SORT_BY_FILE_NAME_DESC)
            },
            leadingIcon = {
                RadioButton(
                    selected = sortOrder == ApkSortOptions.SORT_BY_FILE_NAME_DESC, onClick = null
                )
            })
        DropdownMenuItem(text = { Text(text = stringResource(id = R.string.menu_sort_apk_file_mod_date_desc)) },
            onClick = {
                sort(ApkSortOptions.SORT_BY_LAST_MODIFIED_DESC)
            },
            leadingIcon = {
                RadioButton(
                    selected = sortOrder == ApkSortOptions.SORT_BY_LAST_MODIFIED_DESC,
                    onClick = null
                )
            })
        DropdownMenuItem(text = { Text(text = stringResource(id = R.string.menu_sort_apk_file_mod_date_asc)) },
            onClick = {
                sort(ApkSortOptions.SORT_BY_LAST_MODIFIED_ASC)
            },
            leadingIcon = {
                RadioButton(
                    selected = sortOrder == ApkSortOptions.SORT_BY_LAST_MODIFIED_ASC, onClick = null
                )
            })
        DropdownMenuItem(text = { Text(text = stringResource(id = R.string.menu_sort_apk_file_size_desc)) },
            onClick = {
                sort(ApkSortOptions.SORT_BY_FILE_SIZE_DESC)
            },
            leadingIcon = {
                RadioButton(
                    selected = sortOrder == ApkSortOptions.SORT_BY_FILE_SIZE_DESC, onClick = null
                )
            })
        DropdownMenuItem(text = { Text(text = stringResource(id = R.string.menu_sort_apk_file_size_asc)) },
            onClick = {
                sort(ApkSortOptions.SORT_BY_FILE_SIZE_ASC)
            },
            leadingIcon = {
                RadioButton(
                    selected = sortOrder == ApkSortOptions.SORT_BY_FILE_SIZE_ASC, onClick = null
                )
            })
    }
}