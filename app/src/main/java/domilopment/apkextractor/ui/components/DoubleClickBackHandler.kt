package domilopment.apkextractor.ui.components

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun DoubleClickBackHandler(
    toastText: String, enabled: Boolean = true, pressDelay: Long = 2500L, onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var back by remember {
        mutableStateOf(false)
    }
    BackHandler(enabled && !back) {
        if (back) {
            Timber.tag("DoubleClickBackHandler").d("Second back pressed")
            onBack()
        } else {
            Timber.tag("DoubleClickBackHandler").d("First back pressed")
            back = true
            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
            scope.launch {
                delay(pressDelay) // Is set to Toast.LENGTH_SHORT as default
                Timber.tag("DoubleClickBackHandler").d("reset")
                back = false
            }
        }
    }
}