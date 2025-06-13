package com.pablo.familycart.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.familycart.models.*
import com.pablo.familycart.utils.apiUtils.MercadonaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de gestionar el estado de las categorías, subcategorías y su expansión
 */
class CategoriasViewModel : ViewModel() {

    private val _categorias = MutableStateFlow<List<Category>>(emptyList())
    val categorias: StateFlow<List<Category>> = _categorias

    private val _expandedCategoryIds = MutableStateFlow<Set<Int>>(emptySet())
    val expandedCategoryIds: StateFlow<Set<Int>> = _expandedCategoryIds

    /**
     * Al inicializar el ViewModel se cargan las categorías desde la API
     */
    init {
        loadCategorias()
    }

    /**
     * Carga las categorías y actualiza el estado
     */
    private fun loadCategorias() {
        viewModelScope.launch {
            MercadonaRepository.loadCategories()
            _categorias.value = MercadonaRepository.categoryList
        }
    }

    /**
     * Expande o colapsa una categoría según su estado actual
     */
    fun switchCategoriaExpandida(id: Int) {
        _expandedCategoryIds.value = if (_expandedCategoryIds.value.contains(id)) {
            _expandedCategoryIds.value - id
        } else {
            _expandedCategoryIds.value + id
        }
    }
}
