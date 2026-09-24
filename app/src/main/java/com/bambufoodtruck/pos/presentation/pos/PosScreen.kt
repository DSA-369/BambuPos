package com.bambufoodtruck.pos.presentation.pos

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import android.content.ActivityNotFoundException
import android.widget.Toast
import com.bambufoodtruck.pos.domain.Product
import java.net.URLEncoder
import java.util.Locale

@Composable
fun PosScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val filteredProducts = state.products.filter {
        it.name.contains(state.searchQuery, ignoreCase = true) ||
                it.category.contains(state.searchQuery, ignoreCase = true)
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(0.55f)
                .fillMaxHeight()
        ) {
            Text(text = "Catálogo Bambú POS", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("Buscar producto o categoría...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 130.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredProducts) { product ->
                    ProductCard(
                        product = product,
                        onClick = { viewModel.addToCart(product) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(0.45f)
                .fillMaxHeight()
        ) {
            Text(text = "Orden Actual", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(state.cart) { item ->
                    CartItemRow(
                        item = item,
                        onIncrease = { viewModel.addToCart(item.product) },
                        onDecrease = { viewModel.decreaseQuantity(item.product.id) },
                        onRemove = { viewModel.removeFromCart(item.product.id) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            OutlinedTextField(
                value = state.exchangeRateVES.toString(),
                onValueChange = { newValue ->
                    newValue.toDoubleOrNull()?.let { viewModel.updateExchangeRate(it) }
                },
                label = { Text("Tasa de Cambio (Bs./USD)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Total USD: $${String.format(Locale.US, "%.2f", state.totalUSD)}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Total VES: Bs. ${String.format(Locale.US, "%.2f", state.totalVES)}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { viewModel.clearCart() },
                    modifier = Modifier.weight(1f),
                    enabled = state.cart.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Limpiar")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { viewModel.openCheckoutDialog() },
                    modifier = Modifier.weight(1f),
                    enabled = state.cart.isNotEmpty()
                ) {
                    Text("Cobrar")
                }
            }
        }
    }

    if (state.isCheckoutDialogVisible) {
        CheckoutDialog(
            state = state,
            onDismiss = { viewModel.closeCheckoutDialog() },
            onUsdCashChanged = { viewModel.updatePaidUsdCash(it) },
            onVesCashChanged = { viewModel.updatePaidVesCash(it) },
            onVesCardChanged = { viewModel.updatePaidVesCard(it) },
            onVesPagoMovilChanged = { viewModel.updatePaidVesPagoMovil(it) },
            onAutoFillVesCash = { viewModel.autoFillRemainingVesCash() },
            onAutoFillVesCard = { viewModel.autoFillRemainingVesCard() },
            onAutoFillVesPagoMovil = { viewModel.autoFillRemainingVesPagoMovil() },
            onTogglePrint = { viewModel.togglePrintReceipt(it) },
            onToggleWhatsApp = { viewModel.toggleSendWhatsApp(it) },
            onPhoneChanged = { viewModel.updateCustomerPhone(it) },
            onNameChanged = { viewModel.updateCustomerName(it) },
            onConfirmPayment = {
                viewModel.completeTransaction { phone, message ->
                    val cleanPhone = phone.replace("+", "").replace(" ", "").trim()
                    val encodedMsg = URLEncoder.encode(message, "UTF-8")
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        "whatsapp://send?phone=$cleanPhone&text=$encodedMsg".toUri()
                    )

                    try {
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(
                            context,
                            "WhatsApp no está instalado en este dispositivo. Orden cobrada con éxito.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        )
    }
}

@Composable
fun CheckoutDialog(
    state: PosUiState,
    onDismiss: () -> Unit,
    onUsdCashChanged: (Double) -> Unit,
    onVesCashChanged: (Double) -> Unit,
    onVesCardChanged: (Double) -> Unit,
    onVesPagoMovilChanged: (Double) -> Unit,
    onAutoFillVesCash: () -> Unit,
    onAutoFillVesCard: () -> Unit,
    onAutoFillVesPagoMovil: () -> Unit,
    onTogglePrint: (Boolean) -> Unit,
    onToggleWhatsApp: (Boolean) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onNameChanged: (String) -> Unit,
    onConfirmPayment: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cobro Mixto Multimoneda") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Total Orden: $${String.format(Locale.US, "%.2f", state.totalUSD)} | Bs. ${String.format(Locale.US, "%.2f", state.totalVES)}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (state.remainingUSD > 0.001) {
                            Text(
                                text = "RESTANTE: $${String.format(Locale.US, "%.2f", state.remainingUSD)} / Bs. ${String.format(Locale.US, "%.2f", state.remainingVES)}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                text = "¡Monto Cubierto!",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = if (state.paidUsdCash == 0.0) "" else state.paidUsdCash.toString(),
                    onValueChange = { it.toDoubleOrNull()?.let(onUsdCashChanged) ?: onUsdCashChanged(0.0) },
                    label = { Text("Efectivo ($ USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = if (state.paidVesCash == 0.0) "" else state.paidVesCash.toString(),
                        onValueChange = { it.toDoubleOrNull()?.let(onVesCashChanged) ?: onVesCashChanged(0.0) },
                        label = { Text("Efectivo (Bs. VES Cash)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    if (state.remainingVES > 0.01) {
                        Spacer(modifier = Modifier.width(4.dp))
                        TextButton(onClick = onAutoFillVesCash) { Text("+ Restante") }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = if (state.paidVesCard == 0.0) "" else state.paidVesCard.toString(),
                        onValueChange = { it.toDoubleOrNull()?.let(onVesCardChanged) ?: onVesCardChanged(0.0) },
                        label = { Text("Tarjeta / Punto (Bs. VES)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    if (state.remainingVES > 0.01) {
                        Spacer(modifier = Modifier.width(4.dp))
                        TextButton(onClick = onAutoFillVesCard) { Text("+ Restante") }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = if (state.paidVesPagoMovil == 0.0) "" else state.paidVesPagoMovil.toString(),
                        onValueChange = { it.toDoubleOrNull()?.let(onVesPagoMovilChanged) ?: onVesPagoMovilChanged(0.0) },
                        label = { Text("Pago Móvil (Bs. VES)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    if (state.remainingVES > 0.01) {
                        Spacer(modifier = Modifier.width(4.dp))
                        TextButton(onClick = onAutoFillVesPagoMovil) { Text("+ Restante") }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (state.changeUSD > 0.01) {
                    Text("Vuelto USD: $${String.format(Locale.US, "%.2f", state.changeUSD)}", style = MaterialTheme.typography.bodyLarge)
                    Text("Vuelto VES: Bs. ${String.format(Locale.US, "%.2f", state.changeVES)}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = state.shouldPrintReceipt, onCheckedChange = onTogglePrint)
                    Text("Ticket Térmico")
                    Spacer(modifier = Modifier.width(8.dp))
                    Checkbox(checked = state.shouldSendWhatsApp, onCheckedChange = onToggleWhatsApp)
                    Text("WhatsApp")
                }

                if (state.shouldSendWhatsApp) {
                    OutlinedTextField(
                        value = state.customerName,
                        onValueChange = onNameChanged,
                        label = { Text("Nombre Cliente") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = state.customerPhone,
                        onValueChange = onPhoneChanged,
                        label = { Text("Teléfono WhatsApp (+58...)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmPayment,
                enabled = state.isPaymentComplete
            ) {
                Text("Finalizar y Cobrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun ProductCard(product: Product, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(95.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = product.name, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "$${product.priceUSD}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.product.name, style = MaterialTheme.typography.bodyMedium)
            Text(text = "$${String.format(Locale.US, "%.2f", item.subtotalUSD)}", style = MaterialTheme.typography.bodySmall)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrease) { Text("-", style = MaterialTheme.typography.titleMedium) }
            Text(text = "${item.quantity.toInt()}", modifier = Modifier.padding(horizontal = 4.dp))
            IconButton(onClick = onIncrease) { Text("+", style = MaterialTheme.typography.titleMedium) }
            IconButton(onClick = onRemove) { Text("✕", color = MaterialTheme.colorScheme.error) }
        }
    }
}