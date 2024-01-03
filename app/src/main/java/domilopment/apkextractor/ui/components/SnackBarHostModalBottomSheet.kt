package domilopment.apkextractor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domilopment.apkextractor.utils.MySnackbarVisuals

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnackbarHostModalBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    snackbarHostState: SnackbarHostState,
    content: @Composable() (ColumnScope.() -> Unit)
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = sheetState, dragHandle = {
        DragHandle(snackbarHostState = snackbarHostState)
    }) {
        content(this)
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DragHandle(snackbarHostState: SnackbarHostState) {
    Column(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
        ) {
            val visuals = it.visuals as MySnackbarVisuals
            Snackbar(
                snackbarData = it,
                contentColor = visuals.messageColor ?: SnackbarDefaults.contentColor
            )
        }
        BottomSheetDefaults.DragHandle()
    }
}