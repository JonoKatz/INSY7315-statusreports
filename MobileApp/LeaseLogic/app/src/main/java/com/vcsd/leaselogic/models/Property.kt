package com.vcsd.leaselogic.models

data class Property(
    var id: String = "",
    val landlordId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val size: String = "",
    val location: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val status: String = "Available",
    var deletedAt: Long? = null,
    var isRented: Boolean = false,
    var dateCreated: Long = System.currentTimeMillis(),
    var imageUrls: List<String> = emptyList()
)
