package com.vcsd.leaselogic.tenant

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.vcsd.leaselogic.databinding.ActivityTenantHomeBinding
import com.google.android.material.snackbar.Snackbar


class TenantHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTenantHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTenantHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardMaintenance.setOnClickListener {
            startActivity(Intent(this, TenantMaintenanceRequestActivity::class.java))
        }

        binding.cardViewLease.setOnClickListener {
            Snackbar.make(binding.root, "Lease screen coming soon", Snackbar.LENGTH_SHORT).show()
        }

        binding.cardPayRent.setOnClickListener {
            Snackbar.make(binding.root, "Rent payments coming soon", Snackbar.LENGTH_SHORT).show()
        }

        binding.btnAssistant.setOnClickListener {
            Snackbar.make(binding.root, "AI Assistant coming soon", Snackbar.LENGTH_SHORT).show()
        }

        // View submitted maintenance requests
        binding.cardViewRequests.setOnClickListener {
            startActivity(Intent(this, ViewRequestsActivity::class.java))
        }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, TenantSettingsActivity::class.java))
        }

    }
}
