package com.example.personalfinancetracker.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.personalfinancetracker.R

class SplashActivity : AppCompatActivity() {
    
    companion object {
        private const val SPLASH_DELAY: Long = 2000 // 2 seconds
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Hide the action bar
        supportActionBar?.hide()

        // Use Handler to delay navigation to MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Finish the splash activity so user can't go back to it
        }, SPLASH_DELAY)
    }
} 