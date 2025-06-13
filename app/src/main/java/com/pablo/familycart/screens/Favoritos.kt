package com.pablo.familycart.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pablo.familycart.R
import com.pablo.familycart.components.CustomText
import com.pablo.familycart.components.Footer
import com.pablo.familycart.components.Header
import com.pablo.familycart.navigation.DetallesProducto
import com.pablo.familycart.ui.theme.Amarillo
import com.pablo.familycart.ui.theme.Verde
import com.pablo.familycart.viewModels.FavoritosViewModel
import kotlinx.coroutines.tasks.await

/**
 * Pantalla principal que muestra las pestañas "Favoritos" y "Mis compras" de la familia.
 * Controla la carga de datos y la navegación entre pestañas.
 */
@Composable
fun FavoritosScreen(
    navController: NavController,
) {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val viewModel: FavoritosViewModel = viewModel(viewModelStoreOwner = viewModelStoreOwner)

    var familyId by remember { mutableStateOf<String?>(null) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Favoritos", "Mis compras")

    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val doc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(it.uid)
                .get()
                .await()

            familyId = doc.getString("familyId")
        }
    }

    LaunchedEffect(key1 = familyId) {
        familyId?.let {
            if (viewModel.favoritos.value.isEmpty()) {
                viewModel.loadFavoritos(it)
            }
        }
    }

    Scaffold(
        topBar = { Header(navController) },
        bottomBar = { Footer(navController, heart = R.drawable.heart_fill) },
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Verde
                    )
                },
                modifier = Modifier.background(Color.White)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { CustomText(title) }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTabIndex) {
                    0 -> {
                        if (familyId != null) {
                            FavoritosTabContent(
                                viewModel = viewModel,
                                navController = navController
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CustomText(
                                        text = "No perteneces a ninguna familia.",
                                        fontSize = 22.sp,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    CustomText(
                                        text = "Únete a una familia para poder guardar productos favoritos.",
                                        fontSize = 16.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        if (familyId != null) {
                            viewModel.loadHistorial(familyId!!)
                            MisComprasTabContent(viewModel, navController)
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CustomText(
                                        text = "No perteneces a ninguna familia.",
                                        fontSize = 22.sp,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    CustomText(
                                        text = "Únete a una familia para poder ver el historial de compras.",
                                        fontSize = 16.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Contenido de la pestaña "Favoritos".
 * Muestra la lista de productos favoritos con su información básica.
 */
@Composable
fun FavoritosTabContent(
    viewModel: FavoritosViewModel,
    navController: NavController,
) {
    val favoritos by viewModel.favoritos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Verde)
            }
        }

        favoritos.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CustomText("No hay productos favoritos aún.", fontSize = 20.sp, color = Color.Gray)
            }
        }

        else -> {
            LazyColumn {
                items(favoritos) { producto ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(DetallesProducto(producto.id)) }
                            .padding(8.dp)
                            .background(Color.White)
                            .border(2.dp, Verde, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Image(
                            painter = rememberImagePainter(producto.thumbnail),
                            contentDescription = producto.display_name,
                            modifier = Modifier
                                .size(100.dp)
                                .padding(end = 12.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            CustomText(text = producto.display_name, fontSize = 20.sp)

                            CustomText(
                                text = "${producto.packaging} • ${producto.price_instructions.unit_size}${producto.price_instructions.size_format}",
                                fontSize = 18.sp,
                                color = Color.Gray
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .align(Alignment.End),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1f)) {
                                    CustomText(
                                        text = "${producto.price_instructions.unit_price} €",
                                        fontSize = 18.sp,
                                        color = Verde,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )

                                    producto.price_instructions.previous_unit_price
                                        ?.takeIf { it.isNotEmpty() }
                                        ?.let { previousPrice ->
                                            CustomText(
                                                text = "${previousPrice.trim()} €",
                                                fontSize = 16.sp,
                                                color = Color.Red,
                                                style = TextStyle(textDecoration = TextDecoration.LineThrough)
                                            )
                                        }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Contenido de la pestaña "Mis compras".
 * Muestra el historial de compras de la familia.
 */
@Composable
fun MisComprasTabContent(viewModel: FavoritosViewModel, navController: NavController) {
    val historial by viewModel.historial.collectAsState()

    if (historial.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CustomText("No hay compras en el historial.", fontSize = 20.sp, color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.padding(8.dp)) {
            items(historial) { compra ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .border(2.dp, Verde, RoundedCornerShape(10.dp))
                        .clickable {
                            navController.currentBackStackEntry?.savedStateHandle?.set("compraId", compra.id)
                            navController.navigate("detallesCompra/${compra.id}")
                        }
                        .padding(12.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column {
                        CustomText(
                            text = "Lista: ${compra.nombre_lista}",
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        CustomText(
                            text = "Compra del ${compra.fecha.toDate().toLocaleString()}",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        CustomText(
                            text = "Total: ${"%.2f".format(compra.precio_total)} €",
                            fontSize = 18.sp,
                            color = Amarillo
                        )
                    }
                }
            }
        }
    }
}
