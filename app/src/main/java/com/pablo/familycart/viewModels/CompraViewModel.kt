package com.pablo.familycart.viewModels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.familycart.data.User
import com.pablo.familycart.models.Product
import com.pablo.familycart.models.ProductoLista
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductoCompra(
    val producto: Product,
    val productoLista: ProductoLista,
    val anadido: Boolean = false
)

class CompraViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val user: User
) : ViewModel() {
    private val _productos = MutableStateFlow<List<ProductoCompra>>(emptyList())
    val productos = _productos.asStateFlow()

    init {
        val familyId = requireNotNull(savedStateHandle["familyId"]) { "familyId no puede ser null" }
        val listId = requireNotNull(savedStateHandle["listId"]) { "listId no puede ser null" }

        cargarProductos(familyId.toString(), listId.toString())
    }

    fun cargarProductos(familyId: String, listId: String) {
        viewModelScope.launch {
            val lista = user.getProductosDeListaConDetalles(familyId, listId)
            _productos.value = lista.map {
                ProductoCompra(it.producto, it.productoLista, anadido = false)
            }
        }
    }


    fun marcarComoAnadido(productId: String) {
        _productos.update { lista ->
            lista.map {
                if (it.producto.id == productId) it.copy(anadido = true) else it
            }
        }
    }

    fun terminarCompra(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val productosComprados = productosAnadidos()

            if (productosComprados.isEmpty()) {
                onSuccess()
                return@launch
            }

            val familyId = requireNotNull(savedStateHandle["familyId"]).toString()
            val listId = requireNotNull(savedStateHandle["listId"]).toString()

            // Obtener el nombre de la lista
            val nombreLista = user.obtenerNombreLista(familyId, listId) ?: "Lista sin nombre"

            // Guardar historial
            user.guardarCompra(familyId, productosComprados, nombreLista)

            // Eliminar productos añadidos de la lista
            user.eliminarProductosDeLista(familyId, listId, productosComprados)

            // Recargar la lista de productos para actualizar la UI
            cargarProductos(familyId, listId)

            onSuccess()
        }
    }

    fun productosAnadidos(): List<ProductoCompra> =_productos.value.filter { it.anadido }


}