package com.bambufoodtruck.pos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bambufoodtruck.pos.domain.Product

/**
 * Tabla "products" dentro de la base de datos local SQLite (Room).
 */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val priceUSD: Double,
    val category: String,
    val barcode: String?,
    val imageUrl: String?,
    val isAvailable: Boolean,
    val trackStock: Boolean,
    val stockQuantity: Double,
    val isSoldByWeight: Boolean
)

// Mapeadores para convertir entre la base de datos (Data) y el negocio (Domain)
fun ProductEntity.toDomain(): Product = Product(
    id = id,
    name = name,
    description = description,
    priceUSD = priceUSD,
    category = category,
    barcode = barcode,
    imageUrl = imageUrl,
    isAvailable = isAvailable,
    trackStock = trackStock,
    stockQuantity = stockQuantity,
    isSoldByWeight = isSoldByWeight
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    name = name,
    description = description,
    priceUSD = priceUSD,
    category = category,
    barcode = barcode,
    imageUrl = imageUrl,
    isAvailable = isAvailable,
    trackStock = trackStock,
    stockQuantity = stockQuantity,
    isSoldByWeight = isSoldByWeight
)