package com.nothing.card.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loyalty_cards")
data class LoyaltyCard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val cardNumber: String,
    val barcodeFormat: String, // E.g., QR_CODE, CODE_128, etc.
    val colorHex: String = "#333333",
    val ownerName: String = "",
    val usageCount: Int = 0,
    val color: Int = 0xFF000000.toInt(), // Default black
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
