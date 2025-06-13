package com.pablo.familycart.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.pablo.familycart.R
import com.pablo.familycart.components.*
import com.pablo.familycart.navigation.DetallesProducto
import com.pablo.familycart.ui.theme.Verde
import com.pablo.familycart.viewModels.ProductosViewModel

/**
 * Pantalla principal que muestra los productos agrupados por subcategoría.
 */
@Composable
fun ProductosScreen(navController: NavController, viewModel: ProductosViewModel) {
    val subcatsConProductos by viewModel.subcatsConProductos.collectAsState()
    val listasDisponibles by viewModel.listas.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var selectedProductId by remember { mutableStateOf<String?>(null) }

    var showAlreadyExistsDialog by remember { mutableStateOf(false) }
    var showNoFamilyDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Header(navController)

        LazyColumn(modifier = Modifier.weight(1f)) {
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
                                .align(Alignment.CenterVertically)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            CustomText(text = producto.display_name, fontSize = 20.sp)

                            val packaging = producto.packaging ?: ""
                            val unitSize = producto.price_instructions.unit_size
                            val sizeFormat = producto.price_instructions.size_format ?: ""
                            val unitName = producto.price_instructions.unit_name
                            val packSize = producto.price_instructions.pack_size
                            val totalUnits = producto.price_instructions.total_units

                            val displayUnidades = when {
                                producto.price_instructions.is_pack && totalUnits != null && packSize != null && !unitName.isNullOrBlank() ->
                                    "$totalUnits $unitName x ${packSize}${sizeFormat}"
                                !packaging.isNullOrBlank() && unitSize != null ->
                                    "$packaging • ${unitSize}${sizeFormat}"
                                else -> ""
                            }

                            CustomText(
                                text = displayUnidades,
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

                                    producto.price_instructions.previous_unit_price?.takeIf { it.isNotEmpty() }?.let {
                                        CustomText(
                                            text = "${it.trim()} €",
                                            fontSize = 16.sp,
                                            color = Color.Red,
                                            style = TextStyle(textDecoration = TextDecoration.LineThrough)
                                        )
                                    }
                                }

                                Image(
                                    painter = painterResource(id = R.drawable.add_cart),
                                    contentDescription = "añadir al carrito",
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clickable {
                                            if (listasDisponibles.isEmpty()) {
                                                showNoFamilyDialog = true
                                            } else {
                                                selectedProductId = producto.id
                                                showDialog = true
                                            }
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

        AddProductDialog(
            show = showDialog,
            onDismiss = { showDialog = false },
            onAdd = { listId, cantidad, nota ->
                selectedProductId?.let { productId ->
                    viewModel.addProductToList(listId, productId, nota, cantidad) { result ->
                        if (result.isSuccess) {
                            println("Producto añadido correctamente.")
                        } else {
                            val msg = result.exceptionOrNull()?.message ?: ""
                            if (msg.contains("ya está en la lista", ignoreCase = true)) {
                                showAlreadyExistsDialog = true
                            } else {
                                println("Error al añadir: $msg")
                            }
                        }
                    }
                }
            },
            listas = listasDisponibles
        )

        if (showAlreadyExistsDialog) {
            AlertDialog(
                onDismissRequest = { showAlreadyExistsDialog = false },
                confirmButton = {
                    CustomButton("Cerrar", onClick = { showAlreadyExistsDialog = false })
                },
                title = {
                    CustomText("Producto ya añadido", color = Verde, fontSize = 28.sp)
                },
                text = {
                    CustomText("Este producto ya está en la lista seleccionada.", fontSize = 22.sp)
                },
                containerColor = Color.White
            )
        }

        if (showNoFamilyDialog) {
            AlertDialog(
                onDismissRequest = { showNoFamilyDialog = false },
                confirmButton = {
                    CustomButton("Entendido", onClick = { showNoFamilyDialog = false })
                },
                title = {
                    CustomText("No se pueden añadir productos", color = Verde)
                },
                text = {
                    CustomText(
                        "Para añadir productos tienes que pertenecer a una familia, y recuerda crear alguna lista!",
                        fontSize = 20.sp
                    )
                },
                containerColor = Color.White
            )
        }
    }
}

/**
 * Diálogo añadir un producto a una lista
 */
@Composable
fun AddProductDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onAdd: (listId: String, cantidad: Int, nota: String) -> Unit,
    listas: List<Pair<String, String>>
) {
    if (!show) return

    var selectedListIndex by remember { mutableStateOf(0) }
    var cantidad by remember { mutableStateOf(1) }
    var nota by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            CustomText("Añadir producto", fontSize = 30.sp)

            Spacer(Modifier.height(16.dp))
            CustomText("Selecciona una lista", fontSize = 20.sp)
            Spacer(Modifier.height(8.dp))
            DropdownMenuBox(
                options = listas.map { it.second },
                selectedIndex = selectedListIndex,
                onSelectedIndexChange = { selectedListIndex = it }
            )

            Spacer(Modifier.height(20.dp))
            CustomText("Cantidad", fontSize = 20.sp)
            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                CustomButton("-", onClick = { if (cantidad > 1) cantidad-- }, modifier = Modifier.size(50.dp))
                Spacer(Modifier.width(20.dp))
                CustomText("$cantidad", fontSize = 22.sp)
                Spacer(Modifier.width(20.dp))
                CustomButton("+", onClick = { cantidad++ }, modifier = Modifier.size(50.dp))
            }

            Spacer(Modifier.height(20.dp))
            CustomText("Nota (opcional)", fontSize = 20.sp)
            Spacer(Modifier.height(8.dp))
            CustomTextField(
                value = nota,
                onValueChange = { nota = it },
                label = "Escribe una nota",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                CustomButton("Cancelar", onClick = onDismiss, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(12.dp))
                CustomButton("Añadir", onClick = {
                    val listId = listas[selectedListIndex].first
                    onAdd(listId, cantidad, nota)
                    onDismiss()
                }, modifier = Modifier.weight(1f))
            }
        }
    }
}

/**
 * Componente que renderiza un menú desplegable para seleccionar una opción de lista
 */
@Composable
fun DropdownMenuBox(
    options: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var buttonWidth by remember { mutableStateOf(0) }

    Box {
        CustomButton(
            text = options[selectedIndex],
            onClick = { expanded = true },
            modifier = Modifier.onGloballyPositioned { coordinates ->
                buttonWidth = coordinates.size.width
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current) { buttonWidth.toDp() })
                .background(Color.White)
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { CustomText(option) },
                    onClick = {
                        onSelectedIndexChange(index)
                        expanded = false
                    }
                )
            }
        }
    }
}
