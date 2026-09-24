package com.bambufoodtruck.pos.domain.repository

import com.bambufoodtruck.pos.domain.Product
import kotlinx.coroutines.flow.Flow

/**
 * Contrato del repositorio de productos para la capa de Dominio.
 */
interface ProductRepository {
    fun getAllProducts(): Flow<List<Product>>
    suspend fun getProductById(id: String): Product?
    suspend fun getProductByBarcode(barcode: String): Product?
    suspend fun insertProduct(product: Product)
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(product: Product)
}