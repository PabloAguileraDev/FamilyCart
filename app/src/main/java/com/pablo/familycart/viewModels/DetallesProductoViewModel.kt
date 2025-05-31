package com.pablo.familycart.viewModels

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.familycart.models.Product
import com.pablo.familycart.utils.apiUtils.MercadonaRepository
import com.pablo.familycart.utils.apiUtils.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class DetallesProductoViewModel(args: Bundle) : ViewModel() {
    val producto = MutableStateFlow<Product?>(null)

    init {
        val productoId = args.getInt("productoId")
        loadProducto(productoId)
    }

    private fun loadProducto(id: Int) {
        viewModelScope.launch {
            producto.value = MercadonaRepository.getProductById(id)
        }
    }
}


