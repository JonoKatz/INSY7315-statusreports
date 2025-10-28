package com.vcsd.leaselogic.landlord

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.vcsd.leaselogic.auth.LoginActivity
import com.vcsd.leaselogic.databinding.ActivityLandlordSettingsBinding
import com.vcsd.leaselogic.R

class LandlordSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandlordSettingsBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandlordSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setSupportActionBar(binding.toolbarSettings)
        binding.toolbarSettings.setNavigationOnClickListener { finish() }

        // Logout button
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity() // Clears activity stack
        }
    }
}
