package com.pablo.familycart.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.pablo.familycart.R
import com.pablo.familycart.components.*
import com.pablo.familycart.models.Product
import com.pablo.familycart.models.ProductoLista
import com.pablo.familycart.navigation.Categorias
import com.pablo.familycart.ui.theme.Amarillo
import com.pablo.familycart.viewModels.ProductosListaViewModel

/**
 * Pantalla principal de productos en la lista de la compra.
 * Permite ver los productos, ver sus detalles o eliminarlos de la lista.
 */
@Composable
fun ProductosListaScreen(
    navController: NavController,
    viewModel: ProductosListaViewModel = viewModel()
) {
    val productos by viewModel.productos.collectAsState()
    val familyId = viewModel.familyId
    val listId = viewModel.listId
    var productoAEliminar by remember { mutableStateOf<Product?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Header(navController = navController)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CustomButton(
                text = "Añadir productos",
                onClick = { navController.navigate(Categorias) },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            CustomButton(
                text = "Voy a comprar",
                onClick = {
                    if (productos.isNotEmpty()) {
                        navController.navigate("compra/$familyId/$listId")
                    }
                },
                enabled = productos.isNotEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )

        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            if (productos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CustomText("No hay productos en esta lista.")
                }
            } else {
                LazyColumn {
                    items(productos) { productoCompleto ->
                        ProductoCard(
                            producto = productoCompleto.producto,
                            productoLista = productoCompleto.productoLista,
                            modifier = Modifier.clickable {
                                navController.navigate("detalles_producto_lista/${productoCompleto.producto.id}/${viewModel.listId}")
                            }
                        ) {
                            productoAEliminar = productoCompleto.producto
                        }
                    }
                }
            }
        }

        productoAEliminar?.let { producto ->
            ConfirmDeleteProductDialog(
                show = true,
                producto = producto,
                onDismiss = { productoAEliminar = null },
                onConfirm = {
                    viewModel.removeProducto(producto.id.toString())
                    productoAEliminar = null
                }
            )
        }

        Footer(navController, cart = R.drawable.cart_fill)
    }
}

/**
 * Componente que muestra la información de un producto dentro de la lista de compra.
 */
@Composable
fun ProductoCard(
    producto: Product,
    productoLista: ProductoLista,
    modifier: Modifier = Modifier,
    onClickDelete: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.White)
            .border(2.dp, Color(0xFF4CAF50), RoundedCornerShape(10.dp))
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
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    CustomText(
                        text = "${producto.price_instructions.unit_price} €",
                        fontSize = 18.sp,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    producto.price_instructions.previous_unit_price
                        ?.takeIf { it.isNotEmpty() }
                        ?.let { previousPrice ->
                            CustomText(
                                text = "$previousPrice €",
                                fontSize = 16.sp,
                                color = Color.Red,
                                style = androidx.compose.ui.text.TextStyle(textDecoration = TextDecoration.LineThrough)
                            )
                        }
                }

                CustomText(
                    text = "x${productoLista.cantidad}",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(end = 8.dp),
                    color = Amarillo
                )

                Image(
                    painter = painterResource(id = R.drawable.delete),
                    contentDescription = "eliminar del carrito",
                    modifier = Modifier
                        .size(30.dp)
                        .clickable { onClickDelete() }
                )
            }
        }
    }
}

/**
 * Diálogo de confirmación para eliminar un producto de la lista.
 */
@Composable
fun ConfirmDeleteProductDialog(
    show: Boolean,
    producto: Product,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!show) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            CustomTitulo("Eliminar producto", fontSize = 28.sp)
        },
        text = {
            CustomText(
                text = "¿Estás seguro de que deseas eliminar \"${producto.display_name}\" de esta lista?",
                fontSize = 18.sp
            )
        },
        confirmButton = {
            CustomButton(
                text = "Eliminar",
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            )
        },
        dismissButton = {
            CustomButton(
                text = "Cancelar",
                onClick = onDismiss
            )
        },
        containerColor = Color.White
    )
}
