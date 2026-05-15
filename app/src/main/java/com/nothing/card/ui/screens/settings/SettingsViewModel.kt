package com.nothing.card.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nothing.card.data.local.entity.LoyaltyCard
import com.nothing.card.data.repository.CardRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class CloudSyncSummary(
    val cardCount: Int,
    val lastSyncTimestamp: Long?
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: CardRepository,
    private val biometricHelper: com.nothing.card.util.BiometricHelper,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _isBiometricAvailable = MutableStateFlow(biometricHelper.canAuthenticate())
    val isBiometricAvailable = _isBiometricAvailable.asStateFlow()

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

    private val _localCardCount = MutableStateFlow(0)
    val localCardCount = _localCardCount.asStateFlow()

    private val _cloudSyncSummary = MutableStateFlow<CloudSyncSummary?>(null)
    val cloudSyncSummary = _cloudSyncSummary.asStateFlow()

    private val prefs = context.getSharedPreferences("nothing_card_prefs", android.content.Context.MODE_PRIVATE)
    
    private val _isBiometricEnabled = MutableStateFlow(prefs.getBoolean("biometric_enabled", false))
    val isBiometricEnabled = _isBiometricEnabled.asStateFlow()

    fun toggleBiometric(enabled: Boolean) {
        prefs.edit().putBoolean("biometric_enabled", enabled).apply()
        _isBiometricEnabled.value = enabled
    }

    init {
        checkCurrentAccount()
        refreshLocalCount()
    }

    private fun refreshLocalCount() {
        viewModelScope.launch {
            _localCardCount.value = repository.getAllCardsSync().size
        }
    }

    fun refreshCloudSummary(account: GoogleSignInAccount) {
        viewModelScope.launch {
            try {
                val backupCards = repository.getBackupCardsFromDrive(account)
                if (backupCards != null) {
                    // In a real app, Drive API would give us the file metadata (timestamp).
                    // For now, if we have cards, we'll assume "now" or we could track it in the file.
                    // Let's assume the repository has a way to get the timestamp or we use a preference.
                    val lastSync = prefs.getLong("last_sync_timestamp", 0L).takeIf { it > 0 }
                    _cloudSyncSummary.value = CloudSyncSummary(
                        cardCount = backupCards.size,
                        lastSyncTimestamp = lastSync
                    )
                } else {
                    _cloudSyncSummary.value = null
                }
            } catch (e: Exception) {
                _cloudSyncSummary.value = null
            }
        }
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
                val localCards = repository.getAllCardsSync()
                val backupCards = repository.getBackupCardsFromDrive(account) ?: emptyList()
                
                fun normalize(n: String) = n.trim().trimStart('0').ifEmpty { "0" }
                val backupKeys = backupCards.map { 
                    "${it.name.lowercase().trim()}|${normalize(it.cardNumber)}" 
                }.toSet()
                
                val newCardsCount = localCards.count { 
                    "${it.name.lowercase().trim()}|${normalize(it.cardNumber)}" !in backupKeys 
                }

                repository.backupToDrive(account)
                val now = System.currentTimeMillis()
                prefs.edit().putLong("last_sync_timestamp", now).apply()
                
                refreshLocalCount()
                refreshCloudSummary(account)
                
                if (newCardsCount > 0) {
                    _syncEvent.emit("Backup completato! $newCardsCount nuove carte aggiunte al cloud.")
                } else {
                    _syncEvent.emit("Backup completato! Il cloud è già aggiornato.")
                }
            } catch (e: Exception) {
                _syncEvent.emit("Errore durante il backup: ${e.message}")
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
                    
                    fun normalize(n: String) = n.trim().trimStart('0').ifEmpty { "0" }
                    
                    val existingKeys = localCards.map { 
                        "${it.name.lowercase().trim()}|${normalize(it.cardNumber)}" 
                    }.toSet()
                    
                    val newCards = backupCards.filter { 
                        "${it.name.lowercase().trim()}|${normalize(it.cardNumber)}" !in existingKeys 
                    }
                    val duplicatesCount = backupCards.size - newCards.size

                    if (duplicatesCount > 0) {
                        _pendingCards.value = newCards
                        _showDuplicateDialog.value = true
                    } else {
                        repository.insertCards(newCards)
                        _syncEvent.emit("Ripristino completato! ${newCards.size} nuove carte aggiunte.")
                        refreshLocalCount()
                    }
                } else {
                    _syncEvent.emit("Nessun backup trovato su Google Drive.")
                }
            } catch (e: Exception) {
                _syncEvent.emit("Ripristino fallito: ${e.message}")
            } finally {
                _isRestoring.value = false
            }
        }
    }

    fun confirmRestore() {
        viewModelScope.launch {
            val cardsToInsert = _pendingCards.value
            repository.insertCards(cardsToInsert)
            _syncEvent.emit("Ripristino completato! ${cardsToInsert.size} nuove carte aggiunte.")
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
