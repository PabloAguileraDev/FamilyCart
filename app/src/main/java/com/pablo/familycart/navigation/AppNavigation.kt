package com.pablo.familycart.navigation

import DetallesProductoListaViewModel
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.SavedStateRegistryOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pablo.familycart.data.User
import com.pablo.familycart.models.HistorialCompra
import com.pablo.familycart.screens.LoginScreen
import com.pablo.familycart.screens.CategoriasScreen
import com.pablo.familycart.screens.CompraScreen
import com.pablo.familycart.screens.DetallesCompraScreen
import com.pablo.familycart.screens.DetallesProductoListaScreen
import com.pablo.familycart.screens.DetallesProductoScreen
import com.pablo.familycart.screens.FamiliaScreen
import com.pablo.familycart.screens.FavoritosScreen
import com.pablo.familycart.screens.ListaScreen
import com.pablo.familycart.screens.PerfilScreen
import com.pablo.familycart.screens.ProductosListaScreen
import com.pablo.familycart.screens.ProductosScreen
import com.pablo.familycart.screens.RegistroScreen
import com.pablo.familycart.viewModels.LoginViewModel
import com.pablo.familycart.viewModels.CategoriasViewModel
import com.pablo.familycart.viewModels.CompraViewModel
import com.pablo.familycart.viewModels.DetallesCompraViewModel
import com.pablo.familycart.viewModels.DetallesProductoViewModel
import com.pablo.familycart.viewModels.FamiliaViewModel
import com.pablo.familycart.viewModels.FavoritosViewModel
import com.pablo.familycart.viewModels.ListaViewModel
import com.pablo.familycart.viewModels.PerfilViewModel
import com.pablo.familycart.viewModels.ProductosListaViewModel
import com.pablo.familycart.viewModels.ProductosViewModel
import com.pablo.familycart.viewModels.RegistroViewModel

@Composable
fun AppNavigation(auth: FirebaseAuth, db: FirebaseFirestore){
    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDestination: Any = if (currentUser != null) {
        Categorias
    } else {
        Login
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Login> { LoginScreen(navController, viewModel = LoginViewModel(auth, db)) }
        composable<Registro> { RegistroScreen(navController, viewModel = RegistroViewModel(auth, db)) }
        composable<Categorias> { CategoriasScreen(navController, viewModel = CategoriasViewModel()) }
        composable<Productos> { backStackEntry ->
            val user = User(auth, db)
            val viewModel: ProductosViewModel = viewModel(
                factory = ProductosViewModelFactory(user, backStackEntry, backStackEntry.arguments)
            )
            ProductosScreen(navController = navController, viewModel = viewModel)
        }
        composable<DetallesProducto> { backStackEntry ->
            val user = User(auth, db)
            val viewModel: DetallesProductoViewModel = viewModel(
                factory = DetallesProductoViewModelFactory(user, backStackEntry.arguments!!)
            )
            DetallesProductoScreen(navController = navController, viewModel = viewModel)
        }
        composable<Familia> { FamiliaScreen(navController, viewModel = FamiliaViewModel()) }
        composable<Perfil> { PerfilScreen(navController, viewModel = PerfilViewModel()) }
        composable<Lista> { ListaScreen(navController, viewModel = ListaViewModel()) }
        composable(
            route = "productos_lista/{familyId}/{listId}",
            arguments = listOf(
                navArgument("familyId") { type = NavType.StringType },
                navArgument("listId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val user = User(auth, db)
            val viewModel: ProductosListaViewModel = viewModel(
                factory = ProductosListaViewModelFactory(user, backStackEntry, backStackEntry.arguments)
            )
            ProductosListaScreen(navController = navController, viewModel = viewModel)
        }


        composable(
            route = "detalles_producto_lista/{productoId}/{listId}",
            arguments = listOf(
                navArgument("productoId") { type = NavType.StringType },
                navArgument("listId") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val user = User(auth, db)

            val viewModel: DetallesProductoListaViewModel = viewModel(
                factory = DetallesProductoListaViewModelFactory(
                    user,
                    backStackEntry,
                    backStackEntry.arguments
                )
            )

            DetallesProductoListaScreen(navController = navController, viewModel = viewModel)
        }
        composable(
            route = "compra/{familyId}/{listId}",
            arguments = listOf(
                navArgument("familyId") { type = NavType.StringType },
                navArgument("listId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val user = User(auth, db)
            val viewModel: CompraViewModel = viewModel(
                factory = CompraViewModelFactory(user, backStackEntry, backStackEntry.arguments)
            )
            CompraScreen(navController, viewModel)
        }
        composable<Favoritos> { FavoritosScreen(navController) }
        composable(
            "detallesCompra/{compraId}",
            arguments = listOf(navArgument("compraId") { type = NavType.StringType })
        ) { backStackEntry ->
            val compraId = backStackEntry.arguments?.getString("compraId") ?: ""
            val viewModel: DetallesCompraViewModel = viewModel(
                factory = DetallesCompraViewModelFactory(compraId)
            )
            DetallesCompraScreen(navController, viewModel)
        }
    }
}

class ProductosViewModelFactory(
    private val user: User,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return ProductosViewModel(handle, user) as T
    }
}

class DetallesProductoViewModelFactory(
    private val user: User,
    private val args: Bundle
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DetallesProductoViewModel(args, user) as T
    }
}


class ProductosListaViewModelFactory(
    private val user: User,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return ProductosListaViewModel(handle, user) as T
    }
}

class DetallesProductoListaViewModelFactory(
    private val user: User,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return DetallesProductoListaViewModel(handle, user) as T
    }
}

class CompraViewModelFactory(
    private val user: User,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return CompraViewModel(handle, user) as T
    }
}

class DetallesCompraViewModelFactory(
    private val compraId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetallesCompraViewModel::class.java)) {
            val userRepo = User(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
            @Suppress("UNCHECKED_CAST")
            return DetallesCompraViewModel(compraId, userRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
