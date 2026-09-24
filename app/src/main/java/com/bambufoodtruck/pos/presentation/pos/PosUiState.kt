package com.bambufoodtruck.pos.presentation.pos

import com.bambufoodtruck.pos.domain.Product

/**
 * Representa un ítem dentro del carrito de compra.
 */
data class CartItem(
    val product: Product,
    val quantity: Double = 1.0,
    val notes: String = ""
) {
    val subtotalUSD: Double get() = product.priceUSD * quantity
}

/**
 * Estado reactivo completo de la pantalla del TPV.
 */
enum class PaymentMethod { CASH, PAGO_MOVIL, CARD, MIXED }

data class PosUiState(
    val products: List<Product> = emptyList(),
    val cart: List<CartItem> = emptyList(),
    val exchangeRateVES: Double = 36.5,
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val isCheckoutDialogVisible: Boolean = false,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.MIXED,

    // Desglose de pagos mixtos
    val paidUsdCash: Double = 0.0,
    val paidVesCash: Double = 0.0,
    val paidVesCard: Double = 0.0,
    val paidVesPagoMovil: Double = 0.0,

    // Opciones de recibo
    val shouldPrintReceipt: Boolean = true,
    val shouldSendWhatsApp: Boolean = false,
    val customerPhone: String = "",
    val customerName: String = ""
) {
    val totalUSD: Double get() = cart.sumOf { it.subtotalUSD }
    val totalVES: Double get() = totalUSD * exchangeRateVES

    // Conversión total recibida a USD
    val totalPaidUSD: Double get() = paidUsdCash + ((paidVesCash + paidVesCard + paidVesPagoMovil) / exchangeRateVES)

    // Saldo pendiente por cobrar
    val remainingUSD: Double get() = (totalUSD - totalPaidUSD).coerceAtLeast(0.0)
    val remainingVES: Double get() = remainingUSD * exchangeRateVES

    // Vuelto/Cambio
    val changeUSD: Double get() = (totalPaidUSD - totalUSD).coerceAtLeast(0.0)
    val changeVES: Double get() = changeUSD * exchangeRateVES

    // Validación de cobro completo
    val isPaymentComplete: Boolean get() = totalPaidUSD >= (totalUSD - 0.001)
}