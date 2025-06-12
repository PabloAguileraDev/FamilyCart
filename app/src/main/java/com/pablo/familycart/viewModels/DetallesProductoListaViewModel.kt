import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.familycart.models.Product
import com.pablo.familycart.models.ProductoLista
import com.pablo.familycart.data.User
import com.pablo.familycart.navigation.DetallesProductoLista
import com.pablo.familycart.utils.apiUtils.MercadonaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DetallesProductoListaViewModel(
    savedStateHandle: SavedStateHandle,
    private val userRepository: User
) : ViewModel() {
    val listId: String = savedStateHandle["listId"] ?: ""
    val productoId: String = savedStateHandle["productoId"] ?: ""

    private val _productoLista = MutableStateFlow<ProductoLista?>(null)
    val productoLista: StateFlow<ProductoLista?> = _productoLista

    private val _producto = MutableStateFlow<Product?>(null)
    val producto: StateFlow<Product?> = _producto

    private val _esFavorito = MutableStateFlow(false)
    val esFavorito: StateFlow<Boolean> = _esFavorito.asStateFlow()

    init {
        loadProductoListaYProducto()
    }

    private fun loadProductoListaYProducto() {
        viewModelScope.launch {
            // 1. Obtener familyId del usuario actual
            val user = userRepository.auth.currentUser
            if (user == null) {
                // Usuario no autenticado
                _productoLista.value = null
                _producto.value = null
                return@launch
            }

            val userDoc = userRepository.db.collection("users").document(user.uid).get().await()
            val familyId = userDoc.getString("familyId") ?: run {
                _productoLista.value = null
                _producto.value = null
                return@launch
            }

            // 2. Obtener la lista de productos de Firebase para la familia y lista dada
            val productosLista = userRepository.getProductosDeLista(familyId, listId)

            // 3. Buscar el productoLista que coincida con el productoId
            val productoListaEncontrado = productosLista.find { it.productId == productoId }

            _productoLista.value = productoListaEncontrado

            // 4. Si existe el productoLista, llamar a la API para sacar Product
            if (productoListaEncontrado != null) {
                val productoApi = MercadonaRepository.getProductById(productoId)
                _producto.value = productoApi
            } else {
                _producto.value = null
            }
        }
    }

    fun alternarFavorito() {
        val p = producto.value ?: return
        val productId = p.id

        viewModelScope.launch {
            val familyId = userRepository.getFamilyId()
            if (familyId == null) return@launch

            if (_esFavorito.value) {
                userRepository.eliminarDeFavoritos(familyId, productId)
            } else {
                userRepository.agregarAFavoritos(familyId, productId)
            }

            _esFavorito.update { !it }
        }
    }
}
