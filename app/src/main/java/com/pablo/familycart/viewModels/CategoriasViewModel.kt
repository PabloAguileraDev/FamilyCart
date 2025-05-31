package com.pablo.familycart.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.familycart.models.*
import com.pablo.familycart.utils.apiUtils.MercadonaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoriasViewModel : ViewModel(){
    private val _categorias = MutableStateFlow<List<Category>>(emptyList())
    val categorias: StateFlow<List<Category>> = _categorias

    private val _expandedCategoryIds = MutableStateFlow<Set<Int>>(emptySet())
    val expandedCategoryIds: StateFlow<Set<Int>> = _expandedCategoryIds

    init {
        loadCategorias()
    }

    private fun loadCategorias() {
        viewModelScope.launch {
            MercadonaRepository.loadCategories()
            _categorias.value = MercadonaRepository.categoryList
        }
    }

    fun toggleCategoriaExpandida(id: Int) {
        _expandedCategoryIds.value = if (_expandedCategoryIds.value.contains(id)) {
            _expandedCategoryIds.value - id
        } else {
            _expandedCategoryIds.value + id
        }
    }
}