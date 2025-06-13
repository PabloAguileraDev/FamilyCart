package com.pablo.familycart.viewModels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.familycart.utils.firebaseUtils.User
import com.pablo.familycart.models.ProductoCompleto
import com.pablo.familycart.utils.apiUtils.MercadonaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de manejar los productos dentro de una lista de compras específica.
 * Obtiene y muestra los productos de las listas.
 */
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
        loadProductosFromList()
    }

    /**
     * Carga los productos actuales de la lista,
     * recupera los datos del producto desde la API
     * y los combina con los datos de cantidad y nota.
     */
    private fun loadProductosFromList() {
        viewModelScope.launch {
            try {
                val productoListas = user.getProductosFromLista(familyId, listId)

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

    /**
     * Elimina un producto de una lista.
     **/
    fun removeProducto(productId: String) {
        viewModelScope.launch {
            try {
                user.removeProductFromList(listId, productId)
                loadProductosFromList()
            } catch (e: Exception) {
                println("Error al eliminar producto: ${e.message}")
            }
        }
    }
}
