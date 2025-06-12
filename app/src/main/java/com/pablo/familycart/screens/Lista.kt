package com.pablo.familycart.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.pablo.familycart.R
import com.pablo.familycart.components.*
import com.pablo.familycart.ui.theme.Verde
import com.pablo.familycart.viewModels.ListaViewModel
import kotlinx.coroutines.tasks.await

@Composable
fun ListaScreen(
    navController: NavController,
    viewModel: ListaViewModel = viewModel()
) {
    val listas by viewModel.listas.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val familyId by viewModel.familyId.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var listToDelete by remember { mutableStateOf<Pair<String, String>?>(null) }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // HEADER
            Header(navController)

            if (familyId == null) {
                // CUERPO: usuario sin familia
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CustomText("No perteneces a ninguna familia", fontSize = 22.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        CustomText("Para poder ver o crear listas debes unirte a una familia.",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                }
            } else {
                // CUERPO: usuario con familia
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomText("Listas", fontSize = 32.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            CustomButton(text = "Crear nueva lista", onClick = { showCreateDialog = true })
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (listas.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CustomText("No hay listas en tu familia")
                                    Spacer(modifier = Modifier.height(16.dp))
                                    CustomButton(text = "Crear Lista", onClick = { showCreateDialog = true })
                                }
                            }
                        }
                    } else {
                        items(listas.toList()) { (listId, listData) ->
                            ListaItem(
                                listId = listId,
                                listData = listData,
                                familyId = familyId,
                                onDelete = {
                                    val name = listData["name"] as? String ?: "Sin nombre"
                                    listToDelete = Pair(listId, name)
                                },
                                navController = navController
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            // FOOTER
            Footer(navController, cart = R.drawable.cart_fill)
        }
    }

    if (showCreateDialog) {
        CrearListaDialog(onDismiss = { showCreateDialog = false }) { listName ->
            viewModel.createNewList(listName) {
                showCreateDialog = false
            }
        }
    }

    listToDelete?.let { (id, name) ->
        ConfirmDeleteListDialog(
            show = true,
            onDismiss = { listToDelete = null },
            onConfirm = {
                viewModel.deleteList(id) {
                    Toast.makeText(
                        navController.context,
                        "Lista eliminada",
                        Toast.LENGTH_SHORT
                    ).show()
                    listToDelete = null
                }
            },
            listName = name
        )
    }
}


@Composable
fun ListaItem(
    listId: String,
    listData: Map<String, Any>,
    familyId: String?,
    onDelete: () -> Unit,
    navController: NavController
) {
    var productos by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    LaunchedEffect(listId, familyId) {
        try {
            if (familyId != null) {
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("groups").document(familyId)
                    .collection("lists").document(listId)
                    .collection("items").get().await()
                productos = snapshot.documents.mapNotNull { it.data }
            }
        } catch (e: Exception) {
            productos = emptyList()
        }
    }

    Row(
        modifier = Modifier
            .clickable {navController.navigate("productos_lista/${familyId}/${listId}") }
            .fillMaxWidth()
            .height(120.dp)
            .background(Color.White)
            .border(2.dp, Verde, RoundedCornerShape(10.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            CustomText(
                text = listData["name"] as? String ?: "Sin nombre",
                fontSize = 25.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            CustomText(
                text = "${productos.size} producto(s)",
                fontSize = 18.sp,
                color = Color.Gray
            )
        }

        IconButton(onClick = onDelete) {
            Image(
                painter = painterResource(id = R.drawable.delete),
                contentDescription = "Eliminar lista",
                modifier = Modifier.size(30.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun CrearListaDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            CustomButton("Crear", onClick = { if (name.isNotBlank()) onCreate(name) })
        },
        dismissButton = {
            CustomButton("Cancelar", onClick = onDismiss)
        },
        text = {
            Column(
                modifier = Modifier
                    .background(Color.White)
            ) {
                CustomText("Nueva Lista", fontSize = 28.sp)
                Spacer(modifier = Modifier.height(12.dp))
                CustomTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre de la lista"
                )
            }
        },
        containerColor = Color.White
    )

}

@Composable
fun ConfirmDeleteListDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    listName: String
) {
    if (!show) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            CustomTitulo("Eliminar lista", fontSize = 28.sp)
        },
        text = {
            CustomText(
                text = "¿Estás seguro de que deseas eliminar la lista \"$listName\"? Esta acción no se puede deshacer.",
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


@Composable
fun ListaProductosDeLista(familyId: String, listId: String) {
    val db = FirebaseFirestore.getInstance()
    var productos by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    LaunchedEffect(listId, familyId) {
        try {
            val snapshot = db.collection("groups").document(familyId)
                .collection("lists").document(listId)
                .collection("items").get().await()
            productos = snapshot.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            productos = emptyList()
        }
    }

    Column {
        productos.forEach { producto ->
            CustomText("• ${producto["productId"]} x${producto["cantidad"]} - ${producto["nota"] ?: ""}")
        }
    }
}
