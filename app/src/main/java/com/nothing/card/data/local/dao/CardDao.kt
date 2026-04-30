package com.nothing.card.data.local.dao

import androidx.room.*
import com.nothing.card.data.local.entity.LoyaltyCard
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM loyalty_cards ORDER BY isFavorite DESC, createdAt DESC")
    fun getAllCards(): Flow<List<LoyaltyCard>>

    @Query("SELECT * FROM loyalty_cards WHERE id = :id")
    suspend fun getCardById(id: Long): LoyaltyCard?

    @Query("SELECT * FROM loyalty_cards WHERE cardNumber = :cardNumber LIMIT 1")
    suspend fun getCardByNumber(cardNumber: String): LoyaltyCard?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: LoyaltyCard)

    @Update
    suspend fun updateCard(card: LoyaltyCard)

    @Query("UPDATE loyalty_cards SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Query("UPDATE loyalty_cards SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementUsageCount(id: Long)

    @Delete
    suspend fun deleteCard(card: LoyaltyCard)

    @Query("SELECT * FROM loyalty_cards ORDER BY isFavorite DESC, createdAt DESC")
    suspend fun getAllCardsSync(): List<LoyaltyCard>

    @Query("DELETE FROM loyalty_cards")
    suspend fun deleteAllCards()
}
