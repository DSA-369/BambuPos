package com.bambufoodtruck.pos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Base de datos SQLite gestionada por Room para Bambú POS.
 */
@Database(
    entities = [ProductEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
}