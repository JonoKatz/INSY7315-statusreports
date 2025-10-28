package com.vcsd.leaselogic

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vcsd.leaselogic.auth.LoginActivity
import com.vcsd.leaselogic.landlord.LandlordDashboardActivity
import com.vcsd.leaselogic.tenant.TenantHomeActivity

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val userId = currentUser.uid
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    when (doc.getString("role")) {
                        "Landlord" -> startActivity(Intent(this, LandlordDashboardActivity::class.java))
                        "Tenant" -> startActivity(Intent(this, TenantHomeActivity::class.java))
                        else -> startActivity(Intent(this, LoginActivity::class.java))
                    }
                } else {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                finish()
            }
            .addOnFailureListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
    }
}
