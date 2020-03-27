package domilopment.apkextractor

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import domilopment.apkextractor.activitys.MainActivity
import domilopment.apkextractor.data.ListofAPKs

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ListofAPKs.init(packageManager)
        startActivity(Intent(this, MainActivity::class.java))
    }
}
