package com.bambufoodtruck.pos.domain

import java.util.UUID

/**
 * Modelo de dominio principal para los productos de Bambú POS.
 * Diseñado para operar de forma multimoneda, con lectura de barras/peso y control de inventario.
 */
data class Product(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val priceUSD: Double,
    val category: String,
    val barcode: String? = null,
    val imageUrl: String? = null,
    val isAvailable: Boolean = true,
    val trackStock: Boolean = false,
    val stockQuantity: Double = 0.0,
    val isSoldByWeight: Boolean = false
)