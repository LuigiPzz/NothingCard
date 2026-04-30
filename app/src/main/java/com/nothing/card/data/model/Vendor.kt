package com.nothing.card.data.model

import com.google.gson.annotations.SerializedName

data class Vendor(
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("color") val colorHex: String
)
