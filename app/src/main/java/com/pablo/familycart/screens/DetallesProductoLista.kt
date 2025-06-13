package com.pablo.familycart.screens

import DetallesProductoListaViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.pablo.familycart.R
import com.pablo.familycart.components.CustomText
import com.pablo.familycart.components.Footer
import com.pablo.familycart.components.Header
import com.pablo.familycart.ui.theme.Amarillo
import com.pablo.familycart.ui.theme.Gris
import com.pablo.familycart.ui.theme.Verde

/**
 * Pantalla que muestra los detalles de un producto de una lista.
 *
 * Permite marcar o desmarcar el producto como favorito.
 *
 * @param navController Controlador de navegación para manejar acciones de navegación.
 * @param viewModel ViewModel que contiene la lógica y datos de la pantalla.
 */
@Composable
fun DetallesProductoListaScreen(
    navController: NavController,
    viewModel: DetallesProductoListaViewModel = viewModel(),
) {
    val productoLista by viewModel.productoLista.collectAsState()

    val producto by viewModel.producto.collectAsState()

    val esFavorito by viewModel.esFavorito.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Header(navController)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "volver",
                modifier = Modifier
                    .size(55.dp)
                    .clickable { navController.popBackStack() },
                contentScale = ContentScale.Crop
            )

            val starIcon = if (esFavorito) R.drawable.star_fill else R.drawable.star
            Image(
                painter = painterResource(id = starIcon),
                contentDescription = "favorito",
                modifier = Modifier
                    .size(55.dp)
                    .clickable { viewModel.alternarFavorito() },
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            productoLista?.let { pl ->

                if (producto != null) {
                    Image(
                        painter = rememberImagePainter(producto?.thumbnail),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CustomText(text = producto!!.display_name, fontSize = 34.sp)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            producto?.packaging?.let {
                                CustomText(text = it, color = Gris, fontSize = 28.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            CustomText(
                                text = "${producto?.price_instructions?.unit_price} €",
                                color = Verde,
                                fontSize = 28.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        CustomText(
                            text = "x${pl.cantidad}",
                            color = Amarillo,
                            fontSize = 28.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    CustomText("Nota:", fontSize = 28.sp)
                    CustomText(pl.nota ?: "")

                } else {
                    CustomText("Cargando detalles del producto...")
                }

            } ?: CustomText("Cargando producto de la lista...")
        }

        Footer(navController, cart = R.drawable.cart_fill)
    }
}
