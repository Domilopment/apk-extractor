package domilopment.apkextractor.activitys

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import domilopment.apkextractor.data.ListOfAPKs

class SplashScreenActivity : AppCompatActivity() {
    companion object {
        private var init = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (init) {
            ListOfAPKs().init(packageManager)
            init = false
        }
        Intent(this, MainActivity::class.java).also {
            startActivity(it)
        }
        finish()
    }
}
