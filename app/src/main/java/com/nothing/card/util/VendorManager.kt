package com.nothing.card.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nothing.card.data.model.Vendor
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VendorManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()
    private var vendors: List<Vendor> = emptyList()

    init {
        loadVendors()
    }

    private fun loadVendors() {
        try {
            val jsonString = context.assets.open("vendors.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<Vendor>>() {}.type
            vendors = gson.fromJson(jsonString, listType)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getSuggestions(query: String): List<Vendor> {
        if (query.isBlank()) return vendors
        return vendors.filter { it.name.contains(query, ignoreCase = true) }
    }

    fun getAllVendors(): List<Vendor> = vendors
}
