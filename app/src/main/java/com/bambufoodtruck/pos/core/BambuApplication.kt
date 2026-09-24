package com.bambufoodtruck.pos.core

import android.app.Application
import androidx.room.Room
import com.bambufoodtruck.pos.data.local.AppDatabase
import com.bambufoodtruck.pos.data.repository.ProductRepositoryImpl
import com.bambufoodtruck.pos.domain.repository.ProductRepository

/**
 * Contenedor principal de la aplicación para inicializar singletons locales.
 */
class BambuApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var productRepository: ProductRepository
        private set

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "bambu_pos_db"
        ).build()

        productRepository = ProductRepositoryImpl(database.productDao())
    }
}