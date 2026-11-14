package com.abhinav.caisarra.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.abhinav.caisarra.core.database.CachedResponseDao
import com.abhinav.caisarra.core.database.CachedResponseEntity
import com.abhinav.caisarra.data.local.dao.GameDao
import com.abhinav.caisarra.data.local.entity.GameEntity


@Database(
    entities = [GameEntity::class, CachedResponseEntity::class],
    version = 3,
    exportSchema = false
)
abstract class ChessDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao

    abstract fun cachedResponseDao(): CachedResponseDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {

            override fun migrate(
                database: SupportSQLiteDatabase
            ) {
                database.execSQL(
                    """
                    ALTER TABLE games
                    ADD COLUMN moveTimes TEXT NOT NULL DEFAULT ''
                    """.trimIndent()
                )
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {

            override fun migrate(
                database: SupportSQLiteDatabase
            ) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `cached_responses` (`key` TEXT NOT NULL, `json` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`key`))"
                )
            }
        }

        @Volatile
        private var instance: ChessDatabase? = null

        fun get(context: Context): ChessDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ChessDatabase::class.java,
                    "chess.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build().also { instance = it }
            }
    }
}