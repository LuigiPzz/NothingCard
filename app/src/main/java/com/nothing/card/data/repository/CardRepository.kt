package com.nothing.card.data.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nothing.card.data.local.dao.CardDao
import com.nothing.card.data.local.entity.LoyaltyCard
import com.nothing.card.data.remote.google.GoogleDriveService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepository @Inject constructor(
    private val cardDao: CardDao,
    private val googleDriveService: GoogleDriveService
) {
    val allCards: Flow<List<LoyaltyCard>> = cardDao.getAllCards()

    suspend fun getAllCardsSync(): List<LoyaltyCard> = cardDao.getAllCardsSync()

    suspend fun getCardById(id: Long): LoyaltyCard? = cardDao.getCardById(id)

    suspend fun getCardByNumber(number: String): LoyaltyCard? = cardDao.getCardByNumber(number)
    
    suspend fun getCardByNormalizedNumber(number: String): LoyaltyCard? = cardDao.getCardByNormalizedNumber(number)

    suspend fun insertCard(card: LoyaltyCard) = cardDao.insertCard(card)
    suspend fun updateCard(card: LoyaltyCard) = cardDao.updateCard(card)
    suspend fun incrementUsageCount(id: Long) = cardDao.incrementUsageCount(id)
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) = cardDao.updateFavoriteStatus(id, isFavorite)
    suspend fun deleteCard(card: LoyaltyCard) = cardDao.deleteCard(card)

    suspend fun backupToDrive(account: GoogleSignInAccount) {
        val cards = cardDao.getAllCardsSync()
        val json = Gson().toJson(cards)
        googleDriveService.uploadSyncFile(account, json)
    }

    suspend fun getBackupCardsFromDrive(account: GoogleSignInAccount): List<LoyaltyCard>? {
        val json = googleDriveService.downloadSyncFile(account)
        return if (json != null) {
            val type = object : TypeToken<List<LoyaltyCard>>() {}.type
            Gson().fromJson(json, type)
        } else {
            null
        }
    }

    suspend fun insertCards(cards: List<LoyaltyCard>) {
        cards.forEach {
            cardDao.insertCard(it.copy(id = 0))
        }
    }

    suspend fun deleteAllCards() = cardDao.deleteAllCards()
}
