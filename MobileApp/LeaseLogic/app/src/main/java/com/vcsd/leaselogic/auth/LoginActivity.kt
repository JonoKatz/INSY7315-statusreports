package com.vcsd.leaselogic.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vcsd.leaselogic.auth.RegisterActivity
import com.vcsd.leaselogic.databinding.ActivityLoginBinding
import com.vcsd.leaselogic.landlord.LandlordDashboardActivity
import com.vcsd.leaselogic.tenant.TenantHomeActivity
import com.vcsd.leaselogic.utils.Constants
import com.vcsd.leaselogic.auth.ForgotPasswordActivity



class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val userId = result.user?.uid ?: return@addOnSuccessListener

                    db.collection("users").document(userId).get()
                        .addOnSuccessListener { doc ->
                            if (doc.exists()) {
                                val role = doc.getString("role")
                                val name = doc.getString("name")
                                Toast.makeText(this, "Welcome, $name", Toast.LENGTH_SHORT).show()

                                if (role == "Landlord") {
                                    startActivity(Intent(this, LandlordDashboardActivity::class.java))
                                } else {
                                    startActivity(Intent(this, TenantHomeActivity::class.java))
                                }
                                finish()
                            } else {
                                Toast.makeText(this, "User record not found in Firestore.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error fetching user data: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Login failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }

        }
        binding.txtForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }


        binding.txtRegisterRedirect.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
