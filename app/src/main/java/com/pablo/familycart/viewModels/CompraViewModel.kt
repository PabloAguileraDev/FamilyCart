package com.pablo.familycart.viewModels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.familycart.utils.firebaseUtils.User
import com.pablo.familycart.models.ProductoCompra
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de compra.
 * Administra el estado de los productos y maneja la lógica de añadir, terminar compra, etc.
 */
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

    /**
     * Carga los productos de la lista desde el usuario y los guarda en el estado.
     */
    fun cargarProductos(familyId: String, listId: String) {
        viewModelScope.launch {
            val lista = user.getProductosDeListaConDetalles(familyId, listId)
            _productos.value = lista.map {
                ProductoCompra(it.producto, it.productoLista, anadido = false)
            }
        }
    }

    /**
     * Marca un producto como añadido en la lista.
     */
    fun marcarComoAnadido(productId: String) {
        _productos.update { lista ->
            lista.map {
                if (it.producto.id == productId) it.copy(anadido = true) else it
            }
        }
    }

    /**
     * Finaliza la compra: guarda el historial, elimina los productos añadidos y recarga la lista.
     */
    fun terminarCompra(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val productosComprados = productosAnadidos()

            if (productosComprados.isEmpty()) {
                onSuccess()
                return@launch
            }

            val familyId = requireNotNull(savedStateHandle["familyId"]).toString()
            val listId = requireNotNull(savedStateHandle["listId"]).toString()
            val nombreLista = user.getNombreLista(familyId, listId) ?: "Lista sin nombre"

            user.guardarCompra(familyId, productosComprados, nombreLista)
            user.removeProductsFromList(familyId, listId, productosComprados)
            cargarProductos(familyId, listId)

            onSuccess()
        }
    }

    /**
     * Devuelve la lista de productos que han sido marcados como añadidos.
     */
    fun productosAnadidos(): List<ProductoCompra> = _productos.value.filter { it.anadido }
}
