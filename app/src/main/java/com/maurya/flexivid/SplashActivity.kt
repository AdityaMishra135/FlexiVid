package com.maurya.flexivid

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.maurya.flexivid.databinding.ActivitySplashBinding


class SplashActivity : AppCompatActivity() {

    private lateinit var activitySplashBinding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySplashBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(activitySplashBinding.root)

        Handler(Looper.myLooper()!!).postDelayed(
            {
                val intent = Intent(
                    this@SplashActivity,
                    MainActivity::class.java
                )
                startActivity(intent)
                finish()
            },
//            2250
            20
        )


    }
}