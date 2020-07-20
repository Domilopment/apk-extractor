package domilopment.apkextractor.activitys

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import domilopment.apkextractor.AppListAdapter
import domilopment.apkextractor.SettingsManager
import domilopment.apkextractor.data.ListOfAPKs

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ListOfAPKs(packageManager)
        AppListAdapter.myDataset.addAll(SettingsManager(this).selectedAppTypes())
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
