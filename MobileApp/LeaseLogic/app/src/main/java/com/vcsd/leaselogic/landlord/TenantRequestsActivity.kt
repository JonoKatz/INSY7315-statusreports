package com.vcsd.leaselogic.landlord

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vcsd.leaselogic.adapters.TenantRequestAdapter
import com.vcsd.leaselogic.databinding.ActivityTenantRequestsBinding
import com.vcsd.leaselogic.models.TenantRequest

class TenantRequestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTenantRequestsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: TenantRequestAdapter
    private val requestList = mutableListOf<TenantRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTenantRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        setSupportActionBar(binding.toolbarTenantRequests)
        binding.toolbarTenantRequests.setNavigationOnClickListener { finish() }

        adapter = TenantRequestAdapter(this, requestList)
        binding.recyclerTenantRequests.layoutManager = LinearLayoutManager(this)
        binding.recyclerTenantRequests.adapter = adapter

        loadRequests()
    }

    private fun loadRequests() {
        val landlordId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("rentalRequests")
            .whereEqualTo("landlordId", landlordId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener
                requestList.clear()

                for (doc in snapshot) {
                    val request = doc.toObject(TenantRequest::class.java)
                    request.id = doc.id
                    requestList.add(request)
                }

                adapter.notifyDataSetChanged()
            }
    }
}
