package com.nothing.card.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nothing.card.data.local.entity.LoyaltyCard
import com.nothing.card.data.repository.CardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: CardRepository
) : ViewModel() {

    private val _card = MutableStateFlow<LoyaltyCard?>(null)
    val card = _card.asStateFlow()

    private val _barcodeBitmap = MutableStateFlow<android.graphics.Bitmap?>(null)
    val barcodeBitmap = _barcodeBitmap.asStateFlow()

    fun loadCard(id: Long) {
        viewModelScope.launch {
            repository.incrementUsageCount(id)
            val fetchedCard = repository.getCardById(id)
            _card.value = fetchedCard
            
            // Pre-generate barcode
            fetchedCard?.let { card ->
                val zxingFormat = com.nothing.card.util.BarcodeGenerator.mapToZXingFormat(card.barcodeFormat)
                val isQrCode = zxingFormat == com.google.zxing.BarcodeFormat.QR_CODE
                val width = if (isQrCode) 800 else 1000
                val height = if (isQrCode) 800 else 400
                _barcodeBitmap.value = com.nothing.card.util.BarcodeGenerator.generateBarcode(card.cardNumber, zxingFormat, width, height)
            }
        }
    }

    fun updateCard(name: String, cardNumber: String, colorHex: String, ownerName: String, category: String) {
        viewModelScope.launch {
            _card.value?.let { currentCard ->
                val updatedCard = currentCard.copy(
                    name = name,
                    cardNumber = cardNumber,
                    colorHex = colorHex,
                    ownerName = ownerName,
                    category = category
                )
                repository.updateCard(updatedCard)
                _card.value = updatedCard
                
                // Re-generate barcode if number/format changed
                val zxingFormat = com.nothing.card.util.BarcodeGenerator.mapToZXingFormat(updatedCard.barcodeFormat)
                val isQrCode = zxingFormat == com.google.zxing.BarcodeFormat.QR_CODE
                val width = if (isQrCode) 800 else 1000
                val height = if (isQrCode) 800 else 400
                _barcodeBitmap.value = com.nothing.card.util.BarcodeGenerator.generateBarcode(updatedCard.cardNumber, zxingFormat, width, height)
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            _card.value?.let { currentCard ->
                val newStatus = !currentCard.isFavorite
                repository.toggleFavorite(currentCard.id, newStatus)
                _card.value = currentCard.copy(isFavorite = newStatus)
            }
        }
    }

    fun deleteCard() {
        viewModelScope.launch {
            _card.value?.let {
                repository.deleteCard(it)
            }
        }
    }
}
