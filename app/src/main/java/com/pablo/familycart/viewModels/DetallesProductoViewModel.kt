package com.pablo.familycart.viewModels

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.familycart.data.User
import com.pablo.familycart.models.Product
import com.pablo.familycart.utils.apiUtils.MercadonaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DetallesProductoViewModel(args: Bundle, private val user: User) : ViewModel() {
    val producto = MutableStateFlow<Product?>(null)

    private val _esFavorito = MutableStateFlow(false)
    val esFavorito: StateFlow<Boolean> = _esFavorito.asStateFlow()

    init {
        val productoId = args.getString("productoId") ?: ""
        loadProducto(productoId)
    }

    private fun loadProducto(id: String) {
        viewModelScope.launch {
            val p = MercadonaRepository.getProductById(id)
            producto.value = p

            p?.let {
                val familyId = user.getFamilyId()
                if (familyId != null) {
                    _esFavorito.value = user.esFavorito(familyId, it.id)
                }
            }
        }
    }

    fun alternarFavorito(onNoFamily: () -> Unit) {
        val p = producto.value ?: return
        val productId = p.id

        viewModelScope.launch {
            val familyId = user.getFamilyId()
            if (familyId == null) {
                onNoFamily()
                return@launch
            }

            if (_esFavorito.value) {
                user.eliminarDeFavoritos(familyId, productId)
            } else {
                user.agregarAFavoritos(familyId, productId)
            }

            _esFavorito.update { !_esFavorito.value }
        }
    }

}



