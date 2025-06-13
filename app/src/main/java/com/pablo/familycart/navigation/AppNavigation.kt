package com.pablo.familycart.navigation

import DetallesProductoListaViewModel
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.savedstate.SavedStateRegistryOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pablo.familycart.data.User
import com.pablo.familycart.screens.*
import com.pablo.familycart.viewModels.*

/**
 * Composable principal de navegación.
 * Define la estructura de rutas y cómo se gestionan los ViewModels en cada destino.
 */
@Composable
fun AppNavigation(auth: FirebaseAuth, db: FirebaseFirestore) {
    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser

    val startDestination: Any = if (currentUser != null) Categorias else Login

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable<Login> {
            LoginScreen(navController, viewModel = LoginViewModel(auth, db))
        }
        composable<Registro> {
            RegistroScreen(navController, viewModel = RegistroViewModel(auth, db))
        }
        composable<Categorias> {
            CategoriasScreen(navController)
        }
        composable<Productos> { backStackEntry ->
            val viewModel: ProductosViewModel = viewModel(
                factory = ProductosViewModelFactory(User(auth, db), backStackEntry, backStackEntry.arguments)
            )
            ProductosScreen(navController, viewModel)
        }
        composable<DetallesProducto> { backStackEntry ->
            val viewModel: DetallesProductoViewModel = viewModel(
                factory = DetallesProductoViewModelFactory(User(auth, db), backStackEntry.arguments!!)
            )
            DetallesProductoScreen(navController, viewModel)
        }
        composable<Familia> { FamiliaScreen(navController) }
        composable<Perfil> { PerfilScreen(navController) }
        composable<Lista> { ListaScreen(navController) }
        composable(
            route = "productos_lista/{familyId}/{listId}",
            arguments = listOf(
                navArgument("familyId") { type = NavType.StringType },
                navArgument("listId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val viewModel: ProductosListaViewModel = viewModel(
                factory = ProductosListaViewModelFactory(User(auth, db), backStackEntry, backStackEntry.arguments)
            )
            ProductosListaScreen(navController, viewModel)
        }
        composable(
            route = "detalles_producto_lista/{productoId}/{listId}",
            arguments = listOf(
                navArgument("productoId") { type = NavType.StringType },
                navArgument("listId") { type = NavType.StringType }
            )
        ) {
            DetallesProductoListaScreen(navController)
        }
        composable(
            route = "compra/{familyId}/{listId}",
            arguments = listOf(
                navArgument("familyId") { type = NavType.StringType },
                navArgument("listId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val viewModel: CompraViewModel = viewModel(
                factory = CompraViewModelFactory(User(auth, db), backStackEntry, backStackEntry.arguments)
            )
            CompraScreen(navController, viewModel)
        }
        composable<Favoritos> { FavoritosScreen(navController) }
        composable(
            route = "detallesCompra/{compraId}",
            arguments = listOf(
                navArgument("compraId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val compraId = backStackEntry.arguments?.getString("compraId") ?: ""
            val viewModel: DetallesCompraViewModel = viewModel(
                factory = DetallesCompraViewModelFactory(compraId, User(auth, db))
            )
            DetallesCompraScreen(navController, viewModel)
        }

    }
}

/**
 * Factory para ProductosViewModel.
 */
class ProductosViewModelFactory(
    private val user: User,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        return ProductosViewModel(handle, user) as T
    }
}

/**
 * Factory para DetallesProductoViewModel.
 */
class DetallesProductoViewModelFactory(
    private val user: User,
    private val args: Bundle
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DetallesProductoViewModel(args, user) as T
    }
}

/**
 * Factory para ProductosListaViewModel.
 */
class ProductosListaViewModelFactory(
    private val user: User,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        return ProductosListaViewModel(handle, user) as T
    }
}

/**
 * Factory para CompraViewModel.
 */
class CompraViewModelFactory(
    private val user: User,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        return CompraViewModel(handle, user) as T
    }
}

/**
 * Factory para DetallesCompraViewModel.
 */
class DetallesCompraViewModelFactory(
    private val compraId: String,
    private val user: User
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DetallesCompraViewModel(compraId, user) as T
    }
}
