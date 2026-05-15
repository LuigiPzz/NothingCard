package com.nothing.card.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nothing.card.data.local.dao.CardDao
import com.nothing.card.data.local.entity.LoyaltyCard

@Database(entities = [LoyaltyCard::class], version = 7, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao

    companion object {
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // SQLite doesn't have a dedicated BOOLEAN type, it uses INTEGER (0 = false, 1 = true)
                database.execSQL("ALTER TABLE loyalty_cards ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE loyalty_cards ADD COLUMN category TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("UPDATE loyalty_cards SET category = 'TECH' WHERE category = 'ENTERTAINMENT'")
            }
        }
    }
}
