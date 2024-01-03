package domilopment.apkextractor.ui.dialogs

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.StayPrimaryPortrait
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import domilopment.apkextractor.R
import domilopment.apkextractor.ui.components.SegmentedButtonColumn
import domilopment.apkextractor.ui.components.SegmentedButtonColumnScope
import domilopment.apkextractor.utils.Constants
import domilopment.apkextractor.utils.appFilterOptions.AppFilter
import domilopment.apkextractor.utils.appFilterOptions.AppFilterCategories
import domilopment.apkextractor.utils.appFilterOptions.AppFilterInstaller
import domilopment.apkextractor.utils.appFilterOptions.AppFilterOthers
import domilopment.apkextractor.utils.conditional
import domilopment.apkextractor.utils.settings.AppSortOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppFilterBottomSheet(
    updatedSystemApps: Boolean,
    systemApps: Boolean,
    userApps: Boolean,
    sortOrder: Boolean,
    sort: AppSortOptions,
    prefSortFavorites: Boolean,
    installationSource: String?,
    appCategory: String?,
    otherFilters: Set<String>,
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    changeSelection: (String, Boolean) -> Unit,
    setSortOrder: (Boolean) -> Unit,
    sortApps: (Int) -> Unit,
    sortFavorites: (Boolean) -> Unit,
    setInstallationSource: (String?) -> Unit,
    setCategory: (String?) -> Unit,
    setFilterOthers: (Set<String>) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        AppFilterAppType(updatedSystemApps, systemApps, userApps, changeSelection)
        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.verticalScroll(state = rememberScrollState())) {
            AppFilterSort(
                sortOrder, sort, prefSortFavorites, setSortOrder, sortApps, sortFavorites
            )
            Spacer(modifier = Modifier.height(8.dp))
            AppFilterApps(
                installationSource,
                appCategory,
                otherFilters,
                setInstallationSource,
                setCategory,
                setFilterOthers
            )
        }
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
private fun AppFilterCategoryHeader(header: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            Modifier
                .padding(4.dp, 4.dp)
                .weight(1f)
        )
        Text(
            text = header,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .padding(6.dp, 8.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider(
            Modifier
                .padding(4.dp, 4.dp)
                .weight(1f)
        )
    }
}

@Composable
private fun FilterChip(
    prefSet: Set<String>, filterOptions: AppFilter, onClick: (Set<String>) -> Unit
) {
    val selected = prefSet.contains(filterOptions.name)
    FilterChip(selected = selected, onClick = {
        val filter = prefSet.toMutableSet()
        if (filterOptions.name != null && !selected) filter.add(filterOptions.name!!) else filter.remove(
            filterOptions.name
        )
        onClick(filter)
    }, label = {
        Text(
            text = AppFilterOthers.FAVORITES.getTitleString(LocalContext.current).toString()
        )
    }, leadingIcon = {
        if (selected) Icon(
            imageVector = Icons.Default.Check, contentDescription = null
        )
    })
}

@Composable
private fun <T : AppFilter> FilterMenuChip(
    prefString: String?,
    filterOptions: Array<T>,
    menuTitle: String,
    neutralMenuOptionTitle: String,
    onSelectItem: (String) -> Unit,
    onDeselectItem: () -> Unit
) {
    val density = LocalDensity.current
    var expanded by remember { mutableStateOf(false) }
    var dialogHeight by remember { mutableStateOf(0.dp) }
    val selected = prefString != null
    Box {
        FilterChip(selected = selected, onClick = { expanded = true }, label = {
            Text(text = filterOptions.find { it.name == prefString }
                ?.getTitleString(LocalContext.current)?.toString() ?: menuTitle)
        }, leadingIcon = {
            if (selected) Icon(
                imageVector = Icons.Default.Check, contentDescription = null
            )
        }, trailingIcon = {
            Icon(
                imageVector = if (expanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowDropUp,
                contentDescription = null
            )
        })
        DropdownMenu(expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.onGloballyPositioned {
                dialogHeight = with(density) { it.size.height.toDp() }
            },
            offset = DpOffset(0.dp, -dialogHeight - 46.dp)
        ) {
            DropdownMenuItem(text = { Text(text = neutralMenuOptionTitle) }, onClick = {
                onDeselectItem()
                expanded = false
            })
            filterOptions.forEach {
                val title = it.getTitleString(LocalContext.current)
                if (title != null) {
                    DropdownMenuItem(text = { Text(text = title.toString()) }, onClick = {
                        onSelectItem(it.name.toString())
                        expanded = false
                    })
                }
            }
        }
    }
}

@Composable
private fun AppFilterApps(
    installationSource: String?,
    appCategory: String?,
    otherFilters: Set<String>,
    setInstallationSource: (String?) -> Unit,
    setCategory: (String?) -> Unit,
    setFilterOthers: (Set<String>) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppFilterCategoryHeader(header = stringResource(id = R.string.filter_title))
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(state = rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterMenuChip(prefString = installationSource,
                filterOptions = AppFilterInstaller.entries.toTypedArray(),
                menuTitle = stringResource(id = R.string.installation_sources),
                neutralMenuOptionTitle = stringResource(id = R.string.all_sources),
                onSelectItem = setInstallationSource,
                onDeselectItem = {
                    setInstallationSource(null)
                })
            FilterMenuChip(prefString = appCategory,
                filterOptions = AppFilterCategories.entries.toTypedArray(),
                menuTitle = stringResource(id = R.string.app_categories),
                neutralMenuOptionTitle = stringResource(id = R.string.filter_category_all),
                onSelectItem = setCategory,
                onDeselectItem = {
                    setCategory(null)
                })
            FilterChip(
                prefSet = otherFilters,
                filterOptions = AppFilterOthers.FAVORITES,
                onClick = setFilterOthers
            )
        }
    }
}

@Composable
private fun AppFilterSort(
    sortOrder: Boolean,
    sort: AppSortOptions,
    prefSortFavorites: Boolean,
    setSortOrder: (Boolean) -> Unit,
    sortApps: (Int) -> Unit,
    sortFavorites: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppFilterCategoryHeader(header = stringResource(id = R.string.menu_sort_app))
        Row(modifier = Modifier.height(IntrinsicSize.Max)) {
            Button(
                onClick = {
                    setSortOrder(!sortOrder)
                }, modifier = Modifier
                    .fillMaxHeight()
                    .padding(8.dp, 0.dp)
            ) {
                Row {
                    Icon(
                        imageVector = if (sortOrder) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.Sort,
                        contentDescription = null,
                        Modifier.conditional(
                            sortOrder,
                            ifTrue = { scale(scaleX = 1f, scaleY = -1f) })
                    )
                }
            }
            SegmentedButtonColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp, 0.dp)
            ) {
                SegmentedButton(
                    selected = sort == AppSortOptions.SORT_BY_NAME,
                    onClick = {
                        sortApps(AppSortOptions.SORT_BY_NAME.ordinal)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = SegmentedButtonColumnScope.SegmentedButtonDefaults.itemShape(
                        index = 0, count = 5
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.menu_sort_app_name),
                        textAlign = TextAlign.Center,
                    )
                }
                SegmentedButton(
                    selected = sort == AppSortOptions.SORT_BY_PACKAGE,
                    onClick = {
                        sortApps(AppSortOptions.SORT_BY_PACKAGE.ordinal)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = SegmentedButtonColumnScope.SegmentedButtonDefaults.itemShape(
                        index = 1, count = 5
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.menu_sort_app_package),
                        textAlign = TextAlign.Center,
                    )
                }
                SegmentedButton(
                    selected = sort == AppSortOptions.SORT_BY_INSTALL_TIME,
                    onClick = {
                        sortApps(AppSortOptions.SORT_BY_INSTALL_TIME.ordinal)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = SegmentedButtonColumnScope.SegmentedButtonDefaults.itemShape(
                        index = 2, count = 5
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.menu_sort_app_install),
                        textAlign = TextAlign.Center,
                    )
                }
                SegmentedButton(
                    selected = sort == AppSortOptions.SORT_BY_UPDATE_TIME,
                    onClick = {
                        sortApps(AppSortOptions.SORT_BY_UPDATE_TIME.ordinal)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = SegmentedButtonColumnScope.SegmentedButtonDefaults.itemShape(
                        index = 3, count = 5
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.menu_sort_app_update),
                        textAlign = TextAlign.Center,
                    )
                }
                SegmentedButton(
                    selected = sort == AppSortOptions.SORT_BY_APK_SIZE,
                    onClick = {
                        sortApps(AppSortOptions.SORT_BY_APK_SIZE.ordinal)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = SegmentedButtonColumnScope.SegmentedButtonDefaults.itemShape(
                        index = 4, count = 5
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.menu_sort_app_apk_size),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 0.dp)
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(id = R.string.sort_favorites))
            Switch(checked = prefSortFavorites, onCheckedChange = {
                sortFavorites(it)
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppFilterAppType(
    updatedSystemApps: Boolean,
    systemApps: Boolean,
    userApps: Boolean,
    changeSelection: (String, Boolean) -> Unit
) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text(
            text = stringResource(id = R.string.app_type_header),
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp, 8.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        MultiChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            SegmentedButton(checked = updatedSystemApps,
                onCheckedChange = {
                    changeSelection(Constants.PREFERENCE_KEY_UPDATED_SYSTEM_APPS, it)
                },
                modifier = Modifier.fillMaxHeight(),
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                icon = {}) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate, contentDescription = null
                    )
                    Text(
                        text = stringResource(id = R.string.app_type_system_updated),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            SegmentedButton(checked = systemApps,
                onCheckedChange = {
                    changeSelection(Constants.PREFERENCE_KEY_SYSTEM_APPS, it)
                },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                enabled = updatedSystemApps,
                modifier = Modifier.fillMaxHeight(),
                icon = {}) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Default.StayPrimaryPortrait, contentDescription = null)
                    Text(
                        text = stringResource(id = R.string.app_type_system),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            SegmentedButton(checked = userApps,
                onCheckedChange = {
                    changeSelection(Constants.PREFERENCE_KEY_USER_APPS, it)
                },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                modifier = Modifier.fillMaxHeight(),
                icon = {}) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null)
                    Text(
                        text = stringResource(id = R.string.app_type_user),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun AppFilterBottomSheetPreview() {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    AppFilterBottomSheet(updatedSystemApps = false,
        systemApps = false,
        userApps = true,
        sortOrder = false,
        sort = AppSortOptions.SORT_BY_NAME,
        prefSortFavorites = true,
        installationSource = null,
        appCategory = null,
        otherFilters = emptySet(),
        onDismissRequest = {},
        sheetState = state,
        changeSelection = { _, _ -> },
        setSortOrder = {},
        sortApps = {},
        sortFavorites = {},
        setInstallationSource = {},
        setCategory = {},
        setFilterOthers = {})
}