package domilopment.apkextractor

import android.app.AlertDialog
import android.content.Context
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import domilopment.apkextractor.databinding.ProgressDialogBinding

class ProgressDialog(context: Context, max: Int) : AlertDialog(context) {
    private val binding = ProgressDialogBinding.inflate(this.layoutInflater)
    private val progressBar: ProgressBar
    private val text: TextView

    init {
        setView(binding.root)
        progressBar = binding.progressHorizontal
        text = binding.progressValue
        setCancelable(false)
        setTitle("Progress Dialog")
        progressBar.max = max
        text.text = getPercentageString(0F)
    }

    fun updateProgress(progress: Int) {
        progressBar.progress = progress
        text.text = getPercentageString()

    }

    fun incrementProgress() {
        progressBar.incrementProgressBy(1)
        text.text = getPercentageString()
    }

    private fun getPercentageString(
        percentage: Float = (progressBar.progress.toFloat() / progressBar.max) * 100
    ) = context.getString(R.string.progress_dialog_percentage, percentage)

    override fun show() {
        super.show()
        this.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    fun setTitle(title: String) {
        super.setTitle(title)
    }
}