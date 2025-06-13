package com.pablo.familycart.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pablo.familycart.components.CustomText
import com.pablo.familycart.ui.theme.Verde
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pablo.familycart.R
import com.pablo.familycart.components.Footer
import com.pablo.familycart.components.Header
import com.pablo.familycart.navigation.DetallesProducto
import com.pablo.familycart.ui.theme.Amarillo
import com.pablo.familycart.viewModels.DetallesCompraViewModel

/**
 * Pantalla que muestra los detalles de una compra previamente realizada.
 */
@Composable
fun DetallesCompraScreen(
    navController: NavController,
    viewModel: DetallesCompraViewModel
) {
    val compra by viewModel.compra.collectAsState()
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(compra) {
        if (compra != null) showContent = true
    }

    when {
        compra == null && !showContent -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CustomText("Cargando compra...", fontSize = 20.sp)
            }
        }

        compra == null && showContent -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CustomText("No se pudo cargar la compra", fontSize = 20.sp)
            }
        }

        compra != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(WindowInsets.systemBars.asPaddingValues())
            ) {
                Header(navController)

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = "volver",
                            modifier = Modifier
                                .size(45.dp)
                                .clickable { navController.popBackStack() },
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(16.dp))
                        CustomText("Detalles de la Compra", fontSize = 25.sp)
                    }

                    CustomText("Lista • ${compra!!.nombre_lista}", fontSize = 25.sp, color = Verde)
                    CustomText(compra!!.fecha.toDate().toLocaleString(), fontSize = 20.sp)

                    Spacer(Modifier.height(16.dp))
                    CustomText("Productos:", fontSize = 25.sp, color = Verde)

                    Spacer(Modifier.height(8.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        LazyColumn {
                            items(compra!!.productos) { producto ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            navController.navigate(DetallesProducto(producto.productId))
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(2.dp, Verde),
                                    shape = MaterialTheme.shapes.medium,
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = producto.nombreProducto ?: producto.productId,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "${producto.cantidad} x ${"%.2f".format(producto.precio)} €",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Amarillo
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CustomText(
                            text = "Total: ${"%.2f".format(compra!!.precio_total)} €",
                            fontSize = 23.sp,
                            color = Amarillo
                        )
                    }
                }

                Footer(navController, heart = R.drawable.heart_fill)
            }
        }
    }
}
