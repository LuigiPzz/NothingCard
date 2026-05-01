package com.nothing.card.ui.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nothing.card.data.local.entity.LoyaltyCard
import com.nothing.card.data.model.Vendor
import com.nothing.card.data.repository.CardRepository
import com.nothing.card.util.VendorManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddCardViewModel @Inject constructor(
    private val repository: CardRepository,
    private val vendorManager: VendorManager
) : ViewModel() {

    private val _suggestions = MutableStateFlow<List<Vendor>>(emptyList())
    
    val suggestions: StateFlow<List<Vendor>> = _suggestions.asStateFlow()

    init {
        // Load initial suggestions
        onNameChanged("")
    }

    fun onNameChanged(query: String) {
        _suggestions.value = vendorManager.getSuggestions(query)
    }

    fun getVendorCount(): Int = vendorManager.getAllVendors().size

    suspend fun checkIfCardExists(number: String): Boolean {
        return repository.getCardByNormalizedNumber(number) != null
    }

    fun saveCard(name: String, cardNumber: String, barcodeFormat: String, colorHex: String, ownerName: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.insertCard(
                LoyaltyCard(
                    name = name,
                    cardNumber = cardNumber,
                    barcodeFormat = barcodeFormat,
                    colorHex = colorHex,
                    ownerName = ownerName
                )
            )
            onComplete()
        }
    }
}
