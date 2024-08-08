package domilopment.apkextractor.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun DoubleBackPressDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    backPressNotice: String,
    modifier: Modifier = Modifier,
    pressDelay: Long = 2500L,
    dismissButton: @Composable() (() -> Unit)? = null,
    icon: @Composable() (() -> Unit)? = null,
    title: @Composable() (() -> Unit)? = null,
    text: @Composable() (() -> Unit)? = null,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties(dismissOnClickOutside = false),
    context: Context = LocalContext.current,
    scope: CoroutineScope = rememberCoroutineScope()
) {
    var back by remember {
        mutableStateOf(false)
    }
    AlertDialog(
        onDismissRequest = {
            if (back) {
                Timber.tag("DoubleBackPressDialog").d("Second back press")
                onDismissRequest()
            } else {
                Timber.tag("DoubleBackPressDialog").d("First back press")
                back = true
                Toast.makeText(context, backPressNotice, Toast.LENGTH_SHORT).show()
                scope.launch {
                    delay(pressDelay)
                    Timber.tag("DoubleBackPressDialog").d("reset")
                    back = false
                }
            }
        },
        confirmButton = confirmButton,
        modifier = modifier,
        dismissButton = dismissButton,
        icon = icon,
        title = title,
        text = text,
        shape = shape,
        containerColor = containerColor,
        iconContentColor = iconContentColor,
        titleContentColor = titleContentColor,
        textContentColor = textContentColor,
        tonalElevation = tonalElevation,
        properties = properties
    )
}