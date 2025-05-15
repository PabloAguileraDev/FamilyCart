package com.pablo.familycart.navigation

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pablo.familycart.screens.LoginScreen
import com.pablo.familycart.screens.CategoriasScreen
import com.pablo.familycart.screens.DetallesProductoScreen
import com.pablo.familycart.screens.FamiliaScreen
import com.pablo.familycart.screens.ProductosScreen
import com.pablo.familycart.screens.RegistroScreen
import com.pablo.familycart.viewModels.LoginViewModel
import com.pablo.familycart.viewModels.CategoriasViewModel
import com.pablo.familycart.viewModels.DetallesProductoViewModel
import com.pablo.familycart.viewModels.FamiliaViewModel
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
            val viewModel: ProductosViewModel = viewModel(
                factory = ProductosViewModelFactory(backStackEntry.arguments!!)
            )

            ProductosScreen(navController = navController, viewModel = viewModel)
        }
        composable<DetallesProducto> { backStackEntry ->
            val viewModel: DetallesProductoViewModel = viewModel(
                factory = DetallesProductoViewModelFactory(backStackEntry.arguments!!)
            )
            DetallesProductoScreen(navController = navController, viewModel = viewModel)
        }
        composable<Familia> { FamiliaScreen(navController, viewModel = FamiliaViewModel()) }
    }
}

private class ProductosViewModelFactory(
    private val savedStateHandle: Bundle
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProductosViewModel(savedStateHandle) as T
    }
}

private class DetallesProductoViewModelFactory(
    private val savedStateHandle: Bundle
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DetallesProductoViewModel(savedStateHandle) as T
    }
}
