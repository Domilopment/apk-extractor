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
        // Set UI Mode
        SettingsManager(this).changeUIMode()
        // Load APKs
        ListOfAPKs(packageManager)
        // init Adapter Dataset
        AppListAdapter.myDataset.addAll(SettingsManager(this).selectedAppTypes())
        // Start List Activity
        startActivity(Intent(this, MainActivity::class.java))
        // Destroy Activity
        finish()
    }
}
