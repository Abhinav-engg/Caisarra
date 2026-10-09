package com.abhinav.caisarra.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.abhinav.caisarra.data.local.dao.GameDao
import com.abhinav.caisarra.data.local.entity.GameEntity


@Database(
    entities = [GameEntity::class],
    version = 2,
    exportSchema = false
)
abstract class ChessDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao

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
        @Volatile
        private var instance: ChessDatabase? = null

        fun get(context: Context): ChessDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ChessDatabase::class.java,
                    "chess.db"
                ).addMigrations(MIGRATION_1_2)
                    .build().also { instance = it }
            }
    }
}