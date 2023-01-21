package domilopment.apkextractor

import android.animation.ObjectAnimator
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import com.google.android.material.textview.MaterialTextView

class ExpandableTextView(context: Context, attributesSet: AttributeSet) :
    MaterialTextView(context, attributesSet) {

    init {
        setOnClickListener {
            cycleTextViewExpansion()
        }
    }

    /**
     * if maxLines is 1 set maxLines to 5 and remove ellipsize
     * else maxLines to 1 and ellipsize to middle
     */
    private fun cycleTextViewExpansion() {
        val collapsedMaxLines = 1
        val animation = ObjectAnimator.ofInt(
            this, "maxLines",
            if (maxLines == collapsedMaxLines) {
                ellipsize = null
                10
            } else {
                ellipsize = TextUtils.TruncateAt.MIDDLE
                collapsedMaxLines
            }
        )
        animation.setDuration(200).start()
    }
}