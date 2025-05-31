package com.pablo.familycart.viewModels

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.familycart.models.*
import com.pablo.familycart.utils.apiUtils.MercadonaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductosViewModel(savedStateHandle: Bundle) : ViewModel() {
    private val subcatId: Any = checkNotNull(savedStateHandle["subcatId"])

    private val _subcatsConProductos = MutableStateFlow<List<SubCategoryWithProducts>>(emptyList())
    val subcatsConProductos: StateFlow<List<SubCategoryWithProducts>> = _subcatsConProductos

    init {
        loadProductos()
    }

    private fun loadProductos() {
        viewModelScope.launch {
            val response = MercadonaRepository.getCategoryWithProducts(subcatId)
            _subcatsConProductos.value = response.categories
        }
    }
}

