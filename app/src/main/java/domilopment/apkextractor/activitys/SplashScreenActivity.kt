package domilopment.apkextractor.activitys

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import domilopment.apkextractor.data.ListOfAPKs

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ListOfAPKs(packageManager)
        Intent(this, MainActivity::class.java).also {
            startActivity(it)
        }
        finish()
    }
}
