package domilopment.apkextractor.ui.components

import android.R
import android.content.Context
import android.util.AttributeSet
import android.widget.Checkable
import android.widget.RelativeLayout

class CheckableRelativeLayout(
    context: Context, attributeSet: AttributeSet
) : RelativeLayout(context, attributeSet), Checkable {
    private var isChecked = false

    companion object {
        private val STATE_CHECKED = intArrayOf(R.attr.state_checked)
    }

    override fun setChecked(checked: Boolean) {
        isChecked = checked
        refreshDrawableState()
    }

    override fun isChecked(): Boolean {
        return isChecked
    }

    override fun toggle() {
        isChecked = !isChecked
        refreshDrawableState()
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray? {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (isChecked) mergeDrawableStates(drawableState, STATE_CHECKED)
        return drawableState
    }
}