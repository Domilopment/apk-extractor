package domilopment.apkextractor.activitys

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import domilopment.apkextractor.data.ListofAPKs

class SplashScreenActivity : AppCompatActivity() {
    companion object {
        private var init = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (init || ListofAPKs.isEmpty()) {
            ListofAPKs.init(packageManager)
            init = false
        }
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
