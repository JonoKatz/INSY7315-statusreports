package com.vcsd.leaselogic.auth

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vcsd.leaselogic.databinding.ActivityRegisterBinding
import com.vcsd.leaselogic.landlord.LandlordDashboardActivity
import com.vcsd.leaselogic.tenant.TenantHomeActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Role dropdown setup
        val roles = listOf("Tenant", "Landlord")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        binding.roleDropdown.setAdapter(adapter)

        binding.roleDropdown.setOnClickListener {
            binding.roleDropdown.showDropDown()
        }

        // Register button
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val selectedRole = binding.roleDropdown.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || selectedRole.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val userId = result.user?.uid ?: return@addOnSuccessListener
                    val userData = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "role" to selectedRole,
                        "dateCreated" to System.currentTimeMillis()
                    )

                    db.collection("users").document(userId).set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Welcome to LeaseLogic, $name!", Toast.LENGTH_SHORT).show()

                            // Redirect based on role
                            val intent = when (selectedRole) {
                                "Landlord" -> Intent(this, LandlordDashboardActivity::class.java)
                                "Tenant" -> Intent(this, TenantHomeActivity::class.java)
                                else -> Intent(this, LoginActivity::class.java)
                            }

                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Firestore error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Auth error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Redirect to login
        binding.txtLoginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
