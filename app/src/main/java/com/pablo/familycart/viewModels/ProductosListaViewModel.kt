package com.pablo.familycart.viewModels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.familycart.data.User
import com.pablo.familycart.models.Product
import com.pablo.familycart.models.ProductoCompleto
import com.pablo.familycart.utils.apiUtils.MercadonaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductosListaViewModel(
    savedStateHandle: SavedStateHandle,
    private val user: User
) : ViewModel() {

    val familyId: String = savedStateHandle["familyId"]
        ?: throw IllegalArgumentException("Missing 'familyId' argument")

    val listId: String = savedStateHandle["listId"]
        ?: throw IllegalArgumentException("Missing 'listId' argument")

    private val _productos = MutableStateFlow<List<ProductoCompleto>>(emptyList())
    val productos: StateFlow<List<ProductoCompleto>> = _productos

    init {
        cargarProductosDeLista()
    }

    private fun cargarProductosDeLista() {
        viewModelScope.launch {
            try {
                val productoListas = user.getProductosDeLista(familyId, listId)

                val productosCompletos = productoListas.mapNotNull { productoLista ->
                    val producto = MercadonaRepository.getProductById(productoLista.productId)
                    producto?.let {
                        ProductoCompleto(producto = it, productoLista = productoLista)
                    }
                }
                _productos.value = productosCompletos

            } catch (e: Exception) {
                println("Error al cargar productos de la lista: ${e.message}")
            }
        }
    }

    fun eliminarProducto(productId: String) {
        viewModelScope.launch {
            try {
                user.removeProductFromList(listId, productId)
                cargarProductosDeLista()
            } catch (e: Exception) {
                println("Error al eliminar producto: ${e.message}")
            }
        }
    }


}

