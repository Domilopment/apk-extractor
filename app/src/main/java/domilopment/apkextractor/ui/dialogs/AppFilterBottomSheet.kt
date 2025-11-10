package domilopment.apkextractor.ui.dialogs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import domilopment.apkextractor.R
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import domilopment.apkextractor.utils.appFilterOptions.AppFilter
import domilopment.apkextractor.utils.appFilterOptions.AppFilterCategories
import domilopment.apkextractor.utils.appFilterOptions.AppFilterInstaller
import domilopment.apkextractor.utils.appFilterOptions.AppFilterOthers
import domilopment.apkextractor.utils.fadingEnd
import domilopment.apkextractor.utils.fadingStart
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
        onDismissRequest = onDismissRequest, sheetState = sheetState
    ) {
        AppFilterAppType(updatedSystemApps, systemApps, userApps, changeSelection)
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .verticalScroll(state = rememberScrollState())
                .weight(weight = 1f, fill = false)
        ) {
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
    var expanded by remember { mutableStateOf(false) }
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
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
        val scrollState = rememberScrollState()

        AppFilterCategoryHeader(header = stringResource(id = R.string.filter_title))
        Row(
            Modifier
                .fillMaxWidth()
                .fadingStart(visible = scrollState.canScrollBackward)
                .fadingEnd(visible = scrollState.canScrollForward)
                .horizontalScroll(state = scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterMenuChip(
                prefString = installationSource,
                filterOptions = AppFilterInstaller.entries.toTypedArray(),
                menuTitle = stringResource(id = R.string.installation_sources),
                neutralMenuOptionTitle = stringResource(id = R.string.all_sources),
                onSelectItem = setInstallationSource,
                onDeselectItem = {
                    setInstallationSource(null)
                })
            FilterMenuChip(
                prefString = appCategory,
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Max)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    setSortOrder(!sortOrder)
                }, modifier = Modifier
                    .fillMaxHeight()
                    .padding(8.dp, 0.dp)
            ) {
                AnimatedContent(targetState = sortOrder) { isAscending ->
                    Icon(
                        imageVector = if (isAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Default.Sort, contentDescription = null
                )
            }
            Column(
                modifier = Modifier
                    .padding(8.dp, 0.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy((-6).dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ToggleButton(
                    checked = sort == AppSortOptions.SORT_BY_NAME,
                    onCheckedChange = {
                        sortApps(AppSortOptions.SORT_BY_NAME.ordinal)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shapes = ToggleButtonDefaults.shapes(
                        shape = (ButtonGroupDefaults.connectedMiddleButtonShapes().shape as RoundedCornerShape).copy(
                            topStart = CornerSize(100), topEnd = CornerSize(100)
                        ),
                        checkedShape = ButtonGroupDefaults.connectedButtonCheckedShape,
                    ),
                ) {
                    AnimatedVisibility(visible = sort == AppSortOptions.SORT_BY_NAME) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                    }
                    Text(text = stringResource(id = R.string.menu_sort_app_name))
                }
                ToggleButton(
                    checked = sort == AppSortOptions.SORT_BY_PACKAGE,
                    onCheckedChange = {
                        sortApps(AppSortOptions.SORT_BY_PACKAGE.ordinal)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shapes = ToggleButtonDefaults.shapes(
                        shape = ButtonGroupDefaults.connectedMiddleButtonShapes().shape,
                        checkedShape = ButtonGroupDefaults.connectedButtonCheckedShape,
                    ),
                ) {
                    AnimatedVisibility(visible = sort == AppSortOptions.SORT_BY_PACKAGE) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                    }
                    Text(text = stringResource(id = R.string.menu_sort_app_package))
                }
                ToggleButton(
                    checked = sort == AppSortOptions.SORT_BY_INSTALL_TIME,
                    onCheckedChange = {
                        sortApps(AppSortOptions.SORT_BY_INSTALL_TIME.ordinal)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shapes = ToggleButtonDefaults.shapes(
                        shape = ButtonGroupDefaults.connectedMiddleButtonShapes().shape,
                        checkedShape = ButtonGroupDefaults.connectedButtonCheckedShape,
                    ),
                ) {
                    AnimatedVisibility(visible = sort == AppSortOptions.SORT_BY_INSTALL_TIME) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                    }
                    Text(text = stringResource(id = R.string.menu_sort_app_install))
                }
                ToggleButton(
                    checked = sort == AppSortOptions.SORT_BY_UPDATE_TIME,
                    onCheckedChange = {
                        sortApps(AppSortOptions.SORT_BY_UPDATE_TIME.ordinal)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shapes = ToggleButtonDefaults.shapes(
                        shape = ButtonGroupDefaults.connectedMiddleButtonShapes().shape,
                        checkedShape = ButtonGroupDefaults.connectedButtonCheckedShape,
                    ),
                ) {
                    AnimatedVisibility(visible = sort == AppSortOptions.SORT_BY_UPDATE_TIME) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                    }
                    Text(text = stringResource(id = R.string.menu_sort_app_update))
                }
                ToggleButton(
                    checked = sort == AppSortOptions.SORT_BY_APK_SIZE,
                    onCheckedChange = {
                        sortApps(AppSortOptions.SORT_BY_APK_SIZE.ordinal)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shapes = ToggleButtonDefaults.shapes(
                        shape = (ButtonGroupDefaults.connectedMiddleButtonShapes().shape as RoundedCornerShape).copy(
                            bottomStart = CornerSize(100), bottomEnd = CornerSize(100)
                        ),
                        checkedShape = ButtonGroupDefaults.connectedButtonCheckedShape,
                    ),
                ) {
                    AnimatedVisibility(visible = sort == AppSortOptions.SORT_BY_APK_SIZE) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                    }
                    Text(text = stringResource(id = R.string.menu_sort_app_apk_size))
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 0.dp)
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
        Row(
            Modifier.height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToggleButton(
                checked = updatedSystemApps,
                onCheckedChange = {
                    changeSelection(
                        PreferenceRepository.PreferencesKeys.UPDATED_SYSTEM_APPS.name, it
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
            ) {
                Icon(
                    imageVector = Icons.Default.SystemUpdate,
                    contentDescription = "Localized description",
                )
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                Text(
                    text = stringResource(id = R.string.app_type_system_updated),
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
            }
            ToggleButton(
                checked = systemApps,
                onCheckedChange = {
                    changeSelection(
                        PreferenceRepository.PreferencesKeys.SYSTEM_APPS.name, it
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                enabled = updatedSystemApps,
                shapes = ButtonGroupDefaults.connectedMiddleButtonShapes(),
            ) {
                Icon(
                    imageVector = Icons.Default.StayPrimaryPortrait,
                    contentDescription = "Localized description",
                )
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                Text(
                    text = stringResource(id = R.string.app_type_system),
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )

            }
            ToggleButton(
                checked = userApps,
                onCheckedChange = {
                    changeSelection(
                        PreferenceRepository.PreferencesKeys.USER_APPS.name, it
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Localized description",
                )
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                Text(
                    text = stringResource(id = R.string.app_type_user),
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun AppFilterBottomSheetPreview() {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    AppFilterBottomSheet(
        updatedSystemApps = false,
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