package domilopment.apkextractor.utils

import androidx.compose.ui.Modifier

fun Modifier.conditional(
    condition: Boolean, ifTrue: Modifier.() -> Modifier, ifFalse: (Modifier.() -> Modifier)? = null
): Modifier {
    return if (condition) {
        then(ifTrue(Modifier))
    } else if (ifFalse != null) {
        then(ifFalse(Modifier))
    } else {
        this
    }
}

fun Boolean.toInt() = if (this) 1 else 0
