package com.vcsd.leaselogic.tenant

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vcsd.leaselogic.databinding.ActivityViewRequestsBinding

class ViewRequestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewRequestsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityViewRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser ?: return
        val tenantId = currentUser.uid

        db.collection("maintenance_requests")
            .whereEqualTo("tenantId", tenantId)
            .get()
            .addOnSuccessListener { result ->
                val requests = result.documents.mapNotNull { it.getString("title") }
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, requests)
                binding.listViewRequests.adapter = adapter
            }
    }
}
