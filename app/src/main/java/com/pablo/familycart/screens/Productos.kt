package com.pablo.familycart.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.pablo.familycart.R
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.pablo.familycart.components.CustomText
import com.pablo.familycart.components.Footer
import com.pablo.familycart.components.Header
import com.pablo.familycart.navigation.DetallesProducto
import com.pablo.familycart.ui.theme.Verde
import com.pablo.familycart.viewModels.ProductosViewModel

@Composable
fun ProductosScreen(navController: NavController, viewModel: ProductosViewModel) {
    val subcatsConProductos by viewModel.subcatsConProductos.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Header(navController)

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            subcatsConProductos.forEach { subcat ->
                item {
                    CustomText(
                        text = subcat.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 22.sp
                    )
                }

                items(subcat.products) { producto ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {navController.navigate(DetallesProducto(producto.id))}
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

                        Column(
                            modifier = Modifier
                                .weight(1f)
                        ) {
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

                                    producto.price_instructions.previous_unit_price?.takeIf { it.isNotEmpty() }?.let { previousPrice ->
                                        CustomText(
                                            text = "${previousPrice.trim()} €",
                                            fontSize = 16.sp,
                                            color = Color.Red,
                                            style = TextStyle(
                                                textDecoration = TextDecoration.LineThrough
                                            )
                                        )
                                    }
                                }

                                Image(
                                    painter = painterResource(id = R.drawable.add_cart),
                                    contentDescription = "añadir al carrito",
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clickable {
                                            // Añadir producto a la lista
                                        },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

            }
        }
        Footer(navController, home = R.drawable.home_fill)
    }
}
