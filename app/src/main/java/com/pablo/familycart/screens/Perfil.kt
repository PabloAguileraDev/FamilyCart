package com.pablo.familycart.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.pablo.familycart.R
import com.pablo.familycart.components.CustomButton
import com.pablo.familycart.components.CustomText
import com.pablo.familycart.components.CustomTextField
import com.pablo.familycart.components.Footer
import com.pablo.familycart.components.Header
import com.pablo.familycart.navigation.Login
import com.pablo.familycart.navigation.Perfil
import com.pablo.familycart.ui.theme.Verde
import com.pablo.familycart.viewModels.PerfilViewModel

@Composable
fun PerfilScreen(
    navController: NavController,
    viewModel: PerfilViewModel = viewModel()
) {
    val userData by viewModel.userData.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var showPhotoPicker by remember { mutableStateOf(false) }

    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }

    val context = LocalContext.current
    val user = userData
    var fotoSeleccionada by remember { mutableStateOf("account_amarillo") }

    LaunchedEffect(user) {
        user?.foto?.let { fotoSeleccionada = it }
    }

    val imageResId = remember(fotoSeleccionada) {
        context.resources.getIdentifier(fotoSeleccionada, "drawable", context.packageName)
    }
    val availablePhotos = listOf(
        "account_amarillo","account_verde", "pan", "chocolate", "leche", "zanahoria"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
    ) {
        Header(navController = navController)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (userData != null) {

                if (isEditing) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .clickable { showPhotoPicker = true }
                    ) {
                        Image(
                            painter = painterResource(id = imageResId),
                            contentDescription = "foto editable",
                            modifier = Modifier
                                .matchParentSize()
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.6f)),
                            contentScale = ContentScale.Crop
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.edit),
                            contentDescription = "editar",
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(30.dp)
                        )
                    }
                } else {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = "cuenta",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                CustomText(text = "Nombre", color = Verde, fontSize = 28.sp)
                if (isEditing) {
                    CustomTextField(
                        value = nombre,
                        label = "",
                        onValueChange = { nombre = it }
                    )
                } else {
                    CustomText(text = userData!!.nombre, fontSize = 25.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))

                CustomText(text = "Apellidos", color = Verde, fontSize = 28.sp)
                if (isEditing) {
                    CustomTextField(
                        value = apellidos,
                        label = "",
                        onValueChange = { apellidos = it }
                    )
                } else {
                    CustomText(text = userData!!.apellidos, fontSize = 25.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))

                CustomText(text = "Email", color = Verde, fontSize = 28.sp)
                CustomText(text = userData!!.email, fontSize = 25.sp)

                Spacer(modifier = Modifier.height(40.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isEditing) Arrangement.SpaceEvenly else Arrangement.Center
                ) {
                    CustomButton(
                        onClick = {
                            if (!isEditing) {
                                nombre = userData!!.nombre
                                apellidos = userData!!.apellidos
                                fotoSeleccionada = userData!!.foto
                                isEditing = true
                            } else {
                                viewModel.updateUserProfile(nombre, apellidos, fotoSeleccionada)
                                isEditing = false
                            }
                        },
                        text = if (isEditing) "Guardar cambios" else "Editar perfil",
                        modifier = if (isEditing) Modifier.weight(1f) else Modifier.wrapContentWidth()
                    )

                    if (isEditing) {
                        Spacer(modifier = Modifier.width(8.dp))
                        CustomButton(
                            onClick = { isEditing = false },
                            text = "Cancelar",
                        )
                    }
                }



                Spacer(modifier = Modifier.height(16.dp))

                CustomButton(onClick = { showDialog = true }, text = "Cerrar sesión")
            } else {
                Text("Cargando usuario...")
            }
        }

        Footer(navController = navController)
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { CustomText("Cerrar sesión", color = Verde, fontSize = 26.sp) },
            text = { CustomText("¿Estás seguro de que quieres cerrar sesión?", fontSize = 18.sp) },
            confirmButton = {
                CustomButton(onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Login)
                }, text = "Sí")
            },
            dismissButton = {
                CustomButton(onClick = { showDialog = false }, text = "Cancelar")
            }
        )
    }

    if (showPhotoPicker) {
        AlertDialog(
            onDismissRequest = { showPhotoPicker = false },
            title = { Text("Selecciona una foto") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    availablePhotos.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            row.forEach { foto ->
                                val resId = context.resources.getIdentifier(foto, "drawable", context.packageName)
                                Image(
                                    painter = painterResource(id = resId),
                                    contentDescription = foto,
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            fotoSeleccionada = foto
                                            showPhotoPicker = false
                                        },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                CustomButton(onClick = { showPhotoPicker = false }, text = "Cancelar")
            }
        )
    }

}


