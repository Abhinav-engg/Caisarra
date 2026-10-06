package com.abhinav.caisarra.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.abhinav.caisarra.data.local.dao.GameDao
import com.abhinav.caisarra.data.local.entity.GameEntity


@Database(
    entities = [GameEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ChessDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var instance: ChessDatabase? = null

        fun get(context: Context): ChessDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ChessDatabase::class.java,
                    "chess.db"
                ).build().also { instance = it }
            }
    }
}