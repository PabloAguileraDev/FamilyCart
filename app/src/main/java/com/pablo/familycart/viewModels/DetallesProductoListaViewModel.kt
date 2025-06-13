import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.familycart.models.Product
import com.pablo.familycart.models.ProductoLista
import com.pablo.familycart.utils.firebaseUtils.User
import com.pablo.familycart.utils.apiUtils.MercadonaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel para manejar la pantalla de detalles de un producto dentro de una lista.
 *
 * Se encarga de cargar la información del producto desde Firebase y la API,
 *
 * @param savedStateHandle Contiene parámetros pasados a esta pantalla.
 * @param userRepository Repositorio que maneja la información del usuario y acceso a Firebase.
 */
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
        loadDetallesProducto()
    }

    /**
     * Carga la información del producto en la lista y los detalles del producto desde la API.
     *
     * 1. Obtiene el familyId del usuario actual.
     * 2. Recupera los productos en la lista desde Firebase.
     * 3. Encuentra el producto específico en la lista.
     * 4. Llama la API para obtener detalles del producto.
     */
    private fun loadDetallesProducto() {
        viewModelScope.launch {
            val user = userRepository.auth.currentUser
            if (user == null) {
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

            val productosLista = userRepository.getProductosFromLista(familyId, listId)

            val productoListaEncontrado = productosLista.find { it.productId == productoId }

            _productoLista.value = productoListaEncontrado

            if (productoListaEncontrado != null) {
                val productoApi = MercadonaRepository.getProductById(productoId)
                _producto.value = productoApi
            } else {
                _producto.value = null
            }
        }
    }

    /**
     * Alterna el estado de favorito del producto para la familia actual.
     *
     * Si está en favoritos lo elimina, si no lo añade.
     */
    fun alternarFavorito() {
        val p = producto.value ?: return
        val productId = p.id

        viewModelScope.launch {
            val familyId = userRepository.getFamilyId()
            if (familyId == null) return@launch

            if (_esFavorito.value) {
                userRepository.removeFromFavoritos(familyId, productId)
            } else {
                userRepository.addToFavoritos(familyId, productId)
            }

            _esFavorito.update { !it }
        }
    }
}
