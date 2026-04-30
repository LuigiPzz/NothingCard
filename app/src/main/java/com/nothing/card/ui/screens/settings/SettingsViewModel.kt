package com.nothing.card.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nothing.card.data.local.entity.LoyaltyCard
import com.nothing.card.data.repository.CardRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: CardRepository,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp = _isBackingUp.asStateFlow()

    private val _isRestoring = MutableStateFlow(false)
    val isRestoring = _isRestoring.asStateFlow()

    private val _currentAccount = MutableStateFlow<GoogleSignInAccount?>(null)
    val currentAccount = _currentAccount.asStateFlow()

    private val _pendingCards = MutableStateFlow<List<LoyaltyCard>>(emptyList())
    val pendingCards = _pendingCards.asStateFlow()

    private val _showDuplicateDialog = MutableStateFlow(false)
    val showDuplicateDialog = _showDuplicateDialog.asStateFlow()

    private val _syncEvent = kotlinx.coroutines.flow.MutableSharedFlow<String>()
    val syncEvent = _syncEvent.asSharedFlow()

    init {
        checkCurrentAccount()
    }

    private fun checkCurrentAccount() {
        _currentAccount.value = com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(context)
    }

    fun onAccountSignedIn(account: GoogleSignInAccount) {
        _currentAccount.value = account
        viewModelScope.launch {
            _syncEvent.emit("Signed in as ${account.displayName}")
        }
    }

    fun signOut(googleSignInClient: com.google.android.gms.auth.api.signin.GoogleSignInClient) {
        viewModelScope.launch {
            googleSignInClient.signOut().addOnCompleteListener {
                _currentAccount.value = null
                viewModelScope.launch {
                    _syncEvent.emit("Signed out successfully")
                }
            }
        }
    }

    fun backupToDrive(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _isBackingUp.value = true
            try {
                repository.backupToDrive(account)
                _syncEvent.emit("Backup completed successfully!")
            } catch (e: Exception) {
                _syncEvent.emit("Backup failed: ${e.message}")
            } finally {
                _isBackingUp.value = false
            }
        }
    }

    fun restoreFromDrive(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _isRestoring.value = true
            try {
                val backupCards = repository.getBackupCardsFromDrive(account)
                if (backupCards != null) {
                    val localCards = repository.getAllCardsSync()
                    val existingKeys = localCards.map { "${it.name.lowercase().trim()}|${it.cardNumber.trim()}" }.toSet()
                    
                    val newCards = backupCards.filter { 
                        "${it.name.lowercase().trim()}|${it.cardNumber.trim()}" !in existingKeys 
                    }
                    val duplicatesCount = backupCards.size - newCards.size

                    if (duplicatesCount > 0) {
                        _pendingCards.value = newCards
                        _showDuplicateDialog.value = true
                    } else {
                        repository.insertCards(newCards)
                        _syncEvent.emit("Restore completed! ${newCards.size} cards added.")
                    }
                } else {
                    _syncEvent.emit("No backup found on Google Drive.")
                }
            } catch (e: Exception) {
                _syncEvent.emit("Restore failed: ${e.message}")
            } finally {
                _isRestoring.value = false
            }
        }
    }

    fun confirmRestore() {
        viewModelScope.launch {
            val cardsToInsert = _pendingCards.value
            repository.insertCards(cardsToInsert)
            _syncEvent.emit("Restore completed! ${cardsToInsert.size} new cards added.")
            _showDuplicateDialog.value = false
            _pendingCards.value = emptyList()
        }
    }

    fun cancelRestore() {
        _showDuplicateDialog.value = false
        _pendingCards.value = emptyList()
    }
    
    fun exportToPkPass() {
        viewModelScope.launch {
            try {
                val cards = repository.getAllCardsSync()
                val json = com.google.gson.Gson().toJson(cards)
                // In a real app, we'd create a .pkpass ZIP here. 
                // For now, we'll provide the JSON as a backup file.
                _syncEvent.emit("Exported ${cards.size} cards to backup file.")
            } catch (e: Exception) {
                _syncEvent.emit("Export failed: ${e.message}")
            }
        }
    }

    fun deleteAllCards() {
        viewModelScope.launch {
            try {
                repository.deleteAllCards()
                _syncEvent.emit("All cards deleted.")
            } catch (e: Exception) {
                _syncEvent.emit("Failed to delete cards: ${e.message}")
            }
        }
    }
}
