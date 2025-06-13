package com.pablo.familycart.viewModels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.familycart.data.User
import com.pablo.familycart.models.HistorialCompra
import com.pablo.familycart.models.ProductoHistorial
import com.pablo.familycart.utils.apiUtils.MercadonaRepository.getProductById
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel encargado de cargar y mostrar los detalles de una compra específica.
 */
class DetallesCompraViewModel(
    private val compraId: String,
    private val userRepository: User
) : ViewModel() {

    private val _compra = MutableStateFlow<HistorialCompra?>(null)
    val compra: StateFlow<HistorialCompra?> = _compra

    init {
        viewModelScope.launch {
            loadCompra()
        }
    }

    private suspend fun loadCompra() {
        try {
            val familyId = userRepository.getFamilyId() ?: return
            val docSnapshot = userRepository.db
                .collection("groups")
                .document(familyId)
                .collection("historial")
                .document(compraId)
                .get()
                .await()

            Log.d("Firestore", "Historial raw: ${docSnapshot.data}")

            val compraBase = docSnapshot.toObject(HistorialCompra::class.java)
            if (compraBase != null) {
                val productosDetallados = compraBase.productos.map { producto ->
                    val productoApi = getProductById(producto.productId)
                    ProductoHistorial(
                        productId = producto.productId,
                        cantidad = producto.cantidad,
                        precio = producto.precio,
                        nombreProducto = productoApi?.display_name,
                    )
                }

                _compra.value = HistorialCompra(
                    id = docSnapshot.id,
                    fecha = compraBase.fecha,
                    precio_total = compraBase.precio_total,
                    nombre_lista = compraBase.nombre_lista,
                    productos = productosDetallados
                )
            }
            Log.d("Firestore", "Compra deserializada: $compra")

        } catch (e: Exception) {
            Log.e("DetallesCompraVM", "Error cargando compra: ${e.message}")
        }
    }
}
