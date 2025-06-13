package com.pablo.familycart.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.pablo.familycart.R
import com.pablo.familycart.components.*
import com.pablo.familycart.models.ProductoCompra
import com.pablo.familycart.navigation.Lista
import com.pablo.familycart.ui.theme.Amarillo
import com.pablo.familycart.viewModels.CompraViewModel

/**
 * Pantalla principal de la compra. Muestra los productos añadidos a la lista de compra
 * y permite al usuario añadirlos al carrito y realizar una compra.
 */
@Composable
fun CompraScreen(
    navController: NavController,
    viewModel: CompraViewModel
) {
    val productos by viewModel.productos.collectAsState()
    var productoSeleccionado by remember { mutableStateOf<ProductoCompra?>(null) }
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(productos) {
        if (productos.isNotEmpty()) {
            showContent = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_mercadona_completo),
                contentDescription = "logo mercadona",
                modifier = Modifier.height(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (!showContent) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                CustomText(text = "Cargando...", fontSize = 18.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                items(productos) { producto ->
                    ProductoCompraCard(producto = producto, onClick = {
                        productoSeleccionado = producto
                    })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            CustomButton(text = "Cancelar", onClick = { navController.popBackStack() })
            CustomButton(text = "Terminar compra", onClick = {
                viewModel.terminarCompra(onSuccess = {
                    navController.navigate(Lista)
                })
            })
        }

        productoSeleccionado?.let { producto ->
            DetalleProductoDialog(
                producto = producto,
                onDismiss = { productoSeleccionado = null },
                onAnadir = {
                    viewModel.marcarComoAnadido(producto.producto.id)
                    productoSeleccionado = null
                }
            )
        }
    }
}

/**
 * Componente que representa la tarjeta de un producto en la lista de compra.
 */
@Composable
fun ProductoCompraCard(
    producto: ProductoCompra,
    onClick: () -> Unit
) {
    val fondo = if (producto.anadido) Color.LightGray else Color.White
    val textoStyle = if (producto.anadido) TextDecoration.LineThrough else null

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = fondo,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        border = BorderStroke(2.dp, Color(0xFF4CAF50))
    ) {
        Row(
            modifier = Modifier.padding(10.dp)
        ) {
            CustomText(
                text = producto.producto.display_name,
                style = TextStyle(textDecoration = textoStyle),
                modifier = Modifier.weight(1f)
            )
            CustomText("x${producto.productoLista.cantidad}", color = Amarillo)
        }
    }
}

/**
 * Diálogo que muestra los detalles de un producto seleccionado
 */
@Composable
fun DetalleProductoDialog(
    producto: ProductoCompra,
    onDismiss: () -> Unit,
    onAnadir: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            CustomTitulo(producto.producto.display_name, fontSize = 28.sp)

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberImagePainter(producto.producto.thumbnail),
                    contentDescription = producto.producto.display_name,
                    modifier = Modifier.size(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            InfoRow("Cantidad", "${producto.productoLista.cantidad}")
            InfoRow("Precio unitario", "${producto.producto.price_instructions.unit_price} €")

            val total = (producto.producto.price_instructions.unit_price.toDoubleOrNull() ?: 0.0) * producto.productoLista.cantidad
            InfoRow("Precio total", "%.2f €".format(total))

            producto.productoLista.nota?.let {
                Spacer(modifier = Modifier.height(8.dp))
                CustomText("Nota:", fontSize = 18.sp)
                CustomText(it, fontSize = 16.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                CustomButton(
                    text = "Cerrar",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                CustomButton(
                    text = "Añadir al carrito",
                    onClick = onAnadir,
                    modifier = Modifier.wrapContentWidth()
                )
            }
        }
    }
}

/**
 * Fila reutilizable para mostrar una etiqueta y su valor.
 */
@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CustomText(text = label, fontSize = 18.sp)
        CustomText(text = value, fontSize = 18.sp)
    }
}
