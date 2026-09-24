package com.bambufoodtruck.pos.presentation.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bambufoodtruck.pos.domain.Product
import com.bambufoodtruck.pos.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de la lógica del Módulo TPV Core.
 */
class PosViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PosUiState())
    val uiState: StateFlow<PosUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            productRepository.getAllProducts().collect { productList ->
                if (productList.isEmpty()) {
                    seedInitialData()
                } else {
                    _uiState.update { it.copy(products = productList) }
                }
            }
        }
    }

    private suspend fun seedInitialData() {
        val demoProducts = listOf(
            Product(name = "Waffle Bambú Clásico", priceUSD = 4.5, category = "Waffles"),
            Product(name = "Oblea Especial Arequipe", priceUSD = 2.5, category = "Obleas"),
            Product(name = "Helado Soft 2 Bolas", priceUSD = 3.0, category = "Helados")
        )
        demoProducts.forEach { productRepository.insertProduct(it) }
    }

    fun addToCart(product: Product) {
        _uiState.update { currentState ->
            val existingIndex = currentState.cart.indexOfFirst { it.product.id == product.id }
            val updatedCart = if (existingIndex >= 0) {
                currentState.cart.mapIndexed { index, item ->
                    if (index == existingIndex) item.copy(quantity = item.quantity + 1.0) else item
                }
            } else {
                currentState.cart + CartItem(product = product)
            }
            currentState.copy(cart = updatedCart)
        }
    }

    fun decreaseQuantity(productId: String) {
        _uiState.update { currentState ->
            val updatedCart = currentState.cart.mapNotNull { item ->
                if (item.product.id == productId) {
                    if (item.quantity > 1.0) item.copy(quantity = item.quantity - 1.0) else null
                } else item
            }
            currentState.copy(cart = updatedCart)
        }
    }

    fun removeFromCart(productId: String) {
        _uiState.update { currentState ->
            currentState.copy(cart = currentState.cart.filterNot { it.product.id == productId })
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun updateExchangeRate(newRate: Double) {
        _uiState.update { it.copy(exchangeRateVES = newRate) }
    }

    fun clearCart() {
        _uiState.update { it.copy(cart = emptyList()) }
    }

    fun openCheckoutDialog() {
        _uiState.update {
            it.copy(
                isCheckoutDialogVisible = true,
                paidUsdCash = it.totalUSD,
                paidVesCash = 0.0,
                paidVesCard = 0.0,
                paidVesPagoMovil = 0.0
            )
        }
    }

    fun closeCheckoutDialog() {
        _uiState.update { it.copy(isCheckoutDialogVisible = false) }
    }

    fun setPaymentMethod(method: PaymentMethod) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }

    fun updatePaidUsdCash(amount: Double) {
        _uiState.update { it.copy(paidUsdCash = amount) }
    }

    fun updatePaidVesCash(amount: Double) {
        _uiState.update { it.copy(paidVesCash = amount) }
    }

    fun updatePaidVesCard(amount: Double) {
        _uiState.update { it.copy(paidVesCard = amount) }
    }

    fun updatePaidVesPagoMovil(amount: Double) {
        _uiState.update { it.copy(paidVesPagoMovil = amount) }
    }

    fun autoFillRemainingVesCash() {
        _uiState.update { it.copy(paidVesCash = it.paidVesCash + it.remainingVES) }
    }

    fun autoFillRemainingVesCard() {
        _uiState.update { it.copy(paidVesCard = it.paidVesCard + it.remainingVES) }
    }

    fun autoFillRemainingVesPagoMovil() {
        _uiState.update { it.copy(paidVesPagoMovil = it.paidVesPagoMovil + it.remainingVES) }
    }

    fun togglePrintReceipt(enabled: Boolean) {
        _uiState.update { it.copy(shouldPrintReceipt = enabled) }
    }

    fun toggleSendWhatsApp(enabled: Boolean) {
        _uiState.update { it.copy(shouldSendWhatsApp = enabled) }
    }

    fun updateCustomerPhone(phone: String) {
        _uiState.update { it.copy(customerPhone = phone) }
    }

    fun updateCustomerName(name: String) {
        _uiState.update { it.copy(customerName = name) }
    }

    fun completeTransaction(onWhatsAppMessageReady: (String, String) -> Unit) {
        val state = _uiState.value

        if (state.shouldSendWhatsApp && state.customerPhone.isNotBlank()) {
            val message = buildWhatsAppReceiptText(state)
            onWhatsAppMessageReady(state.customerPhone, message)
        }

        _uiState.update {
            it.copy(
                cart = emptyList(),
                isCheckoutDialogVisible = false,
                paidUsdCash = 0.0,
                paidVesCash = 0.0,
                paidVesCard = 0.0,
                paidVesPagoMovil = 0.0,
                customerPhone = "",
                customerName = ""
            )
        }
    }

    private fun buildWhatsAppReceiptText(state: PosUiState): String {
        val itemsList = state.cart.joinToString("\n") { "• ${it.product.name} x${it.quantity.toInt()} - $${String.format(java.util.Locale.US, "%.2f", it.subtotalUSD)}" }
        return """
            *¡Gracias por tu compra en Bambú Foodtruck!* 🧇🍦
            
            *Resumen de la Orden:*
            $itemsList
            
            *Total Pagado:* $${String.format(java.util.Locale.US, "%.2f", state.totalUSD)} / Bs. ${String.format(java.util.Locale.US, "%.2f", state.totalVES)}
            
            *Desglose de Pago:*
            ${if (state.paidUsdCash > 0) "• Efectivo USD: $${String.format(java.util.Locale.US, "%.2f", state.paidUsdCash)}\n" else ""}${if (state.paidVesCash > 0) "• Efectivo Bs.: Bs. ${String.format(java.util.Locale.US, "%.2f", state.paidVesCash)}\n" else ""}${if (state.paidVesCard > 0) "• Punto de Venta: Bs. ${String.format(java.util.Locale.US, "%.2f", state.paidVesCard)}\n" else ""}${if (state.paidVesPagoMovil > 0) "• Pago Móvil: Bs. ${String.format(java.util.Locale.US, "%.2f", state.paidVesPagoMovil)}\n" else ""}
            ¡Escanea nuestro QR en caja para acumular puntos de fidelidad!
            
            _Sigue nuestras promociones en Instagram: @bambufoodtruck_
        """.trimIndent()
    }
}