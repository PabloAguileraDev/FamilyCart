package com.pablo.familycart.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pablo.familycart.utils.firebaseUtils.User
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
) : ViewModel() {
    val user = User(auth, db)

    private val _favoritos = MutableStateFlow<List<Product>>(emptyList())
    val favoritos: StateFlow<List<Product>> = _favoritos

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _historial = MutableStateFlow<List<HistorialCompra>>(emptyList())
    val historial: StateFlow<List<HistorialCompra>> = _historial

    private val _listas = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val listas: StateFlow<List<Pair<String, String>>> = _listas

    private val _productosDeListas = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val productosDeListas: StateFlow<Map<String, List<String>>> = _productosDeListas

    init {
        loadListas()
    }

    /**
     * Carga la lista de productos favoritos para la familia indicada.
     * Obtiene los IDs de los productos favoritos desde Firestore y luego
     * recupera los detalles completos de cada producto mediante la API.
     */
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

    /**
     * Carga el historial de compras de la familia.
     */
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

    /**
     * Carga las listas de la familia
     */
    private fun loadListas() {
        viewModelScope.launch {
            val listasResult = user.getUserLists()
            _listas.value = listasResult.getOrNull() ?: emptyList()

            loadProductosDeListas()
        }
    }

    /**
     * Carga los productos de las listas
     */
    fun loadProductosDeListas() {
        viewModelScope.launch {
            val listasIds = _listas.value.map { it.first }
            val resultMap = mutableMapOf<String, List<String>>()

            for (listId in listasIds) {
                try {
                    val snapshot = db.collection("listas")
                        .document(listId)
                        .collection("productos")
                        .get()
                        .await()

                    val productos = snapshot.documents.mapNotNull { it.getString("productId") }
                    resultMap[listId] = productos
                } catch (e: Exception) {
                    e.printStackTrace()
                    resultMap[listId] = emptyList()
                }
            }

            _productosDeListas.value = resultMap
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
