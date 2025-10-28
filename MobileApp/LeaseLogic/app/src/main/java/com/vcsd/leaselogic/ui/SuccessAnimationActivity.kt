package com.vcsd.leaselogic.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.vcsd.leaselogic.databinding.ActivitySuccessAnimationBinding
import com.vcsd.leaselogic.R


class SuccessAnimationActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySuccessAnimationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuccessAnimationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val message = intent.getStringExtra("message") ?: "Action Successful!"
        binding.txtMessage.text = message

        // Automatically go back after 2 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
            Handler(Looper.getMainLooper()).postDelayed({
                finish()
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }, 2000)

        }, 2500)
    }
}
