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

    fun loadCard(id: Long) {
        viewModelScope.launch {
            repository.incrementUsageCount(id)
            _card.value = repository.getCardById(id)
        }
    }

    fun updateCard(name: String, cardNumber: String, colorHex: String, ownerName: String) {
        viewModelScope.launch {
            _card.value?.let { currentCard ->
                val updatedCard = currentCard.copy(
                    name = name,
                    cardNumber = cardNumber,
                    colorHex = colorHex,
                    ownerName = ownerName
                )
                repository.updateCard(updatedCard)
                _card.value = updatedCard
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
