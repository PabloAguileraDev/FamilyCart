package com.pablo.familycart.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pablo.familycart.models.HistorialCompra
import com.pablo.familycart.models.Product
import com.pablo.familycart.models.ProductoHistorial
import com.pablo.familycart.utils.apiUtils.MercadonaRepository.getProductById
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FavoritosViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel(){

    // Lista de productos favoritos cargados completos
    private val _favoritos = MutableStateFlow<List<Product>>(emptyList())
    val favoritos: StateFlow<List<Product>> = _favoritos

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _historial = MutableStateFlow<List<HistorialCompra>>(emptyList())
    val historial: StateFlow<List<HistorialCompra>> = _historial

    fun loadFavoritos(familyId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val favoritosSnapshot = db.collection("groups")
                    .document(familyId)
                    .collection("favoritos")
                    .get()
                    .await()

                val productIds = favoritosSnapshot.documents.map { it.id }

                if (productIds.isEmpty()) {
                    _favoritos.value = emptyList()
                    return@launch
                }

                val productos = mutableListOf<Product>()
                for (productId in productIds) {
                    val producto = getProductById(productId)
                    producto?.let { productos.add(it.copy(id = productId)) }
                }

                _favoritos.value = productos
            } catch (e: Exception) {
                e.printStackTrace()
                _favoritos.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadHistorial(familyId: String) {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("groups")
                    .document(familyId)
                    .collection("historial")
                    .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()

                val compras = snapshot.documents.mapNotNull { doc ->
                    val fecha = doc.getTimestamp("fecha")
                    val precio = doc.getDouble("precio_total")
                    val nombreLista = doc.getString("nombre_lista") ?: "Sin nombre"
                    val productosRaw = doc.get("productos") as? List<Map<String, Any>>

                    if (fecha != null && precio != null && productosRaw != null) {
                        val productos = productosRaw.mapNotNull {
                            val id = it["productId"] as? String
                            val precioProd = (it["precio"] as? Number)?.toDouble() ?: 0.0
                            val cantidad = (it["cantidad"] as? Number)?.toInt() ?: 0

                            if (id != null) ProductoHistorial(id, precioProd, cantidad) else null
                        }

                        HistorialCompra(
                            id = doc.id,
                            fecha = fecha,
                            precio_total = precio,
                            nombre_lista = nombreLista,
                            productos = productos
                        )
                    } else null
                }


                _historial.value = compras
            } catch (e: Exception) {
                e.printStackTrace()
                _historial.value = emptyList()
            }
        }
    }

}