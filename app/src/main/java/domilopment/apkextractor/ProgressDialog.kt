package domilopment.apkextractor

import android.app.AlertDialog
import android.content.Context
import android.view.WindowManager
import android.widget.ProgressBar
import com.google.android.material.textview.MaterialTextView
import domilopment.apkextractor.databinding.ProgressDialogBinding

class ProgressDialog(context: Context, max: Int) : AlertDialog(context) {
    private val binding = ProgressDialogBinding.inflate(this.layoutInflater)
    private val progressBar: ProgressBar
    private val textPercentages: MaterialTextView
    private val textValue: MaterialTextView

    init {
        setView(binding.root)
        progressBar = binding.progressHorizontal
        textPercentages = binding.progressPercentages
        textValue = binding.progressValue
        setCancelable(false)
        setTitle("Progress Dialog")
        progressBar.max = max
        textPercentages.text = getPercentageString(0F)
        textValue.text = getValueString(0)
    }

    fun updateProgress(progress: Int) {
        progressBar.progress = progress
        textPercentages.text = getPercentageString()
        textValue.text = getValueString()
    }

    fun incrementProgress() {
        progressBar.incrementProgressBy(1)
        textPercentages.text = getPercentageString()
        textValue.text = getValueString()
    }

    private fun getPercentageString(
        percentage: Float = (progressBar.progress.toFloat() / progressBar.max) * 100
    ) = context.getString(R.string.progress_dialog_percentage, percentage)

    private fun getValueString(progress: Int = progressBar.progress) =
        context.getString(R.string.progress_dialog_value, progress, progressBar.max)

    override fun show() {
        super.show()
        this.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}