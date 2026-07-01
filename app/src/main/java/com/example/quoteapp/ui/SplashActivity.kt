package com.example.quoteapp.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.quoteapp.MainActivity
import com.example.quoteapp.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val iconAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in_icon)
        val titleAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in_text)
        val subtitleAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in_subtitle)

        findViewById<android.widget.TextView>(R.id.tvSplashIcon).startAnimation(iconAnim)
        findViewById<android.widget.TextView>(R.id.tvSplashTitle).startAnimation(titleAnim)
        findViewById<android.view.View>(R.id.viewSplashDivider).startAnimation(subtitleAnim)
        findViewById<android.widget.TextView>(R.id.tvSplashSubtitle).startAnimation(subtitleAnim)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2200)
    }
}