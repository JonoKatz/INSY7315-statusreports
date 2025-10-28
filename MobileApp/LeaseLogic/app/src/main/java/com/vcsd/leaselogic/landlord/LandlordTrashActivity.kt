package com.vcsd.leaselogic.landlord

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vcsd.leaselogic.databinding.ActivityLandlordTrashBinding
import com.vcsd.leaselogic.databinding.ItemPropertyBinding
import com.vcsd.leaselogic.models.Property

class LandlordTrashActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandlordTrashBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val trashList = mutableListOf<Pair<String, Property>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandlordTrashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setSupportActionBar(binding.toolbarTrash)
        binding.toolbarTrash.setNavigationOnClickListener { finish() }

        binding.recyclerTrash.layoutManager = LinearLayoutManager(this)

        purgeOldTrash() // 🧹 Remove 30-day-old items
        loadTrash()
    }


    private fun loadTrash() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("trash")
            .whereEqualTo("deletedBy", userId)
            .get()
            .addOnSuccessListener { result ->
                trashList.clear()
                for (doc in result) {
                    val property = doc.get("propertyData", Property::class.java) ?: continue
                    property.deletedAt = doc.getLong("deletedAt") // 🕓 attach timestamp
                    trashList.add(Pair(doc.id, property))
                }

                binding.recyclerTrash.adapter = TrashAdapter()
            }
    }

    inner class TrashAdapter :
        androidx.recyclerview.widget.RecyclerView.Adapter<TrashAdapter.TrashViewHolder>() {

        inner class TrashViewHolder(val itemBinding: ItemPropertyBinding) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrashViewHolder {
            val b = ItemPropertyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return TrashViewHolder(b)
        }

        override fun onBindViewHolder(holder: TrashViewHolder, position: Int) {
            val (docId, property) = trashList[position]
            holder.itemBinding.txtPropertyName.text = property.name
            holder.itemBinding.txtPropertyPrice.text = "R${property.price}"
            holder.itemBinding.txtPropertyLocation.text = property.location

            // 🕓 Display "Deleted X days ago"
            val deletedAt = property.deletedAt ?: 0L
            if (deletedAt > 0) {
                val timeAgo = getTimeAgo(deletedAt)
                holder.itemBinding.txtDeletedInfo.text = "Deleted $timeAgo"
                holder.itemBinding.txtDeletedInfo.visibility = View.VISIBLE
            }

            holder.itemBinding.btnRestore.visibility = View.VISIBLE
            holder.itemBinding.btnRestore.setOnClickListener {
                restoreProperty(docId, property)
            }
        }


        override fun getItemCount() = trashList.size
    }

    private fun restoreProperty(docId: String, property: Property) {
        val mainRef = db.collection("properties").document(docId)
        val trashRef = db.collection("trash").document(docId)

        mainRef.set(property)
            .addOnSuccessListener {
                trashRef.delete()
                Snackbar.make(binding.root, "Property restored", Snackbar.LENGTH_SHORT).show()
                loadTrash()
            }
            .addOnFailureListener {
                Snackbar.make(binding.root, "Failed to restore property", Snackbar.LENGTH_SHORT)
                    .show()
            }
    }

    private fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val days = diff / (1000 * 60 * 60 * 24)
        val hours = diff / (1000 * 60 * 60)
        val minutes = diff / (1000 * 60)

        return when {
            days > 1 -> "$days days ago"
            days == 1L -> "1 day ago"
            hours >= 1 -> "$hours hours ago"
            minutes >= 1 -> "$minutes minutes ago"
            else -> "just now"
        }
    }

    private fun purgeOldTrash() {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)

        db.collection("trash")
            .whereLessThan("deletedAt", thirtyDaysAgo)
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    db.collection("trash").document(doc.id).delete()
                }
            }
    }


}
