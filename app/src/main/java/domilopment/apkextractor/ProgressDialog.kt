package domilopment.apkextractor

import android.app.AlertDialog
import android.content.Context
import android.widget.ProgressBar
import com.google.android.material.textview.MaterialTextView
import domilopment.apkextractor.databinding.ProgressDialogBinding

class ProgressDialog(context: Context, max: Int) : AlertDialog(context) {
    private val binding = ProgressDialogBinding.inflate(this.layoutInflater)
    private val progressBar: ProgressBar
    private val textPercentages: MaterialTextView
    private val textValue: MaterialTextView
    private val currentProcess: MaterialTextView

    var isShown: Boolean = false
        private set

    init {
        setView(binding.root)
        progressBar = binding.progressHorizontal
        textPercentages = binding.progressPercentages
        textValue = binding.progressValue
        currentProcess = binding.currentProcess
        setCancelable(false)
        progressBar.max = max
        textPercentages.text = getPercentageString(0F)
        textValue.text = getValueString(0)
    }

    fun updateProgress(progress: Int): Int {
        progressBar.progress = progress
        textPercentages.text = getPercentageString()
        textValue.text = getValueString()
        return progress
    }

    /**
     * Increment Dialog Progress by one
     * @return
     * New Progress of Dialog
     */
    fun incrementProgress(): Int {
        progressBar.incrementProgressBy(1)
        textPercentages.text = getPercentageString()
        textValue.text = getValueString()
        return progressBar.progress
    }

    private fun getPercentageString(
        percentage: Float = (progressBar.progress.toFloat() / progressBar.max) * 100
    ) = context.getString(R.string.progress_dialog_percentage, percentage)

    private fun getValueString(progress: Int = progressBar.progress) =
        context.getString(R.string.progress_dialog_value, progress, progressBar.max)

    fun setProcess(processName: String) {
        currentProcess.text = processName
    }

    fun setTasksMax(tasks: Int) {
        progressBar.max = tasks
    }

    override fun show() {
        super.show()
        isShown = true
    }

    override fun dismiss() {
        super.dismiss()
        isShown = false
    }
}