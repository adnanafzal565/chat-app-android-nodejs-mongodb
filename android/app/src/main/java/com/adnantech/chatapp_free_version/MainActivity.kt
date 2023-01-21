package com.adnantech.chatapp_free_version

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate
import com.adnantech.chatapp_free_version.utils.MySharedPreference
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity() {

    private val mySharedPreference: MySharedPreference = MySharedPreference()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        Timer().schedule(1000) {
            val accessToken: String = mySharedPreference.getAccessToken(applicationContext)
            if (accessToken.isEmpty()) {
                startActivity(Intent(applicationContext, WelcomeActivity::class.java))
            } else {
                startActivity(Intent(applicationContext, HomeActivity::class.java))
            }
            finish()
        }
    }
}
