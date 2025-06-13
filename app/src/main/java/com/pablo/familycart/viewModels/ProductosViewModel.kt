package com.pablo.familycart.viewModels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.familycart.utils.firebaseUtils.User
import com.pablo.familycart.models.SubCategoryWithProducts
import com.pablo.familycart.utils.apiUtils.MercadonaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsable de la pantalla de productos.
 * Carga los productos y permite añadirlos a una lista
 */
class ProductosViewModel(
    savedStateHandle: SavedStateHandle,
    private val user: User
) : ViewModel() {

    private val subcatId: String = savedStateHandle.get<Int>("subcatId")?.toString() ?: ""

    private val _subcatsConProductos = MutableStateFlow<List<SubCategoryWithProducts>>(emptyList())
    val subcatsConProductos: StateFlow<List<SubCategoryWithProducts>> = _subcatsConProductos

    private val _listas = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val listas: StateFlow<List<Pair<String, String>>> = _listas

    init {
        loadProductos()
        loadListas()
    }

    /**
     * Carga los productos correspondientes a la subcategoría actual.
     */
    private fun loadProductos() {
        viewModelScope.launch {
            val response = MercadonaRepository.getCategoryWithProducts(subcatId)
            _subcatsConProductos.value = response.categories
        }
    }

    /**
     * Carga las listas de la familia.
     */
    private fun loadListas() {
        viewModelScope.launch {
            val listasResult = user.getUserLists()
            if (listasResult.isSuccess) {
                _listas.value = listasResult.getOrNull() ?: emptyList()
            } else {
                println("Error cargando listas: ${listasResult.exceptionOrNull()?.message}")
                _listas.value = emptyList()
            }
        }
    }

    /**
     * Añade un producto a una lista.
     */
    fun addProductToList(
        listId: String,
        productId: String,
        nota: String,
        cantidad: Int,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val result = user.addProductToList(listId, productId, nota, cantidad)
            onResult(result)
        }
    }
}
