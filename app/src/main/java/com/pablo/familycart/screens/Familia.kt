package com.pablo.familycart.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pablo.familycart.R
import com.pablo.familycart.components.CustomButton
import com.pablo.familycart.components.CustomText
import com.pablo.familycart.components.CustomTextField
import com.pablo.familycart.components.CustomTitulo
import com.pablo.familycart.components.Footer
import com.pablo.familycart.components.Header
import com.pablo.familycart.data.User
import com.pablo.familycart.ui.theme.Verde
import com.pablo.familycart.viewModels.FamiliaViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Pantalla principal que muestra el estado de la familia.
 */
@Composable
fun FamiliaScreen(
    navController: NavHostController,
    viewModel: FamiliaViewModel = viewModel()
) {
    val hasFamily by viewModel.hasFamily.collectAsState()

    LaunchedEffect(hasFamily) {
        if (hasFamily == null) {
            viewModel.checkFamilyStatus()
        }
    }

    when (hasFamily) {
        null -> LoadingScreen()
        false -> NoFamilyUI(navController, viewModel)
        true -> FamilyDetails(navController, viewModel)
    }
}

/**
 * Pantalla que muestra un indicador de carga mientras se obtiene el estado.
 */
@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * UI mostrada cuando el usuario no pertenece a ninguna familia.
 * Permite crear una nueva familia o unirse a una existente mediante diálogos.
 */
@Composable
fun NoFamilyUI(navController: NavHostController, viewModel: FamiliaViewModel) {
    var showJoinDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Header(navController)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomText(text = "No perteneces a ninguna familia")
            Spacer(modifier = Modifier.height(36.dp))
            CustomButton(text = "Crear familia", onClick = { showCreateDialog = true })
            Spacer(modifier = Modifier.height(16.dp))
            CustomButton(text = "Unirse a una familia", onClick = { showJoinDialog = true })
        }

        Footer(navController, family = R.drawable.family_fill)
    }

    if (showCreateDialog) {
        CreateFamilyDialog(
            onDismiss = { showCreateDialog = false },
            onCreated = { viewModel.setHasFamily(true) }
        )
    }

    if (showJoinDialog) {
        JoinFamilyDialog(
            onDismiss = { showJoinDialog = false },
            onJoined = { viewModel.setHasFamily(true) }
        )
    }
}

/**
 * Diálogo para crear una familia con una contraseña.
 */
@Composable
fun CreateFamilyDialog(onDismiss: () -> Unit, onCreated: () -> Unit) {
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = {
            password = ""
            onDismiss()
        },
        title = { CustomText("Crear Familia") },
        text = {
            Column {
                CustomTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Contraseña"
                )
            }
        },
        confirmButton = {
            CustomButton(text = "Crear", onClick = {
                val userHelper = User(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
                coroutineScope.launch {
                    val result = userHelper.createGroup(password)
                    withContext(Dispatchers.Main) {
                        if (result.isSuccess) {
                            Toast.makeText(context, "Familia creada", Toast.LENGTH_SHORT).show()
                            onDismiss()
                            onCreated()
                        } else {
                            Toast.makeText(context, result.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            })
        },
        dismissButton = {
            CustomButton(text = "Cancelar", onClick = {
                password = ""
                onDismiss()
            })
        },
        containerColor = Color.White
    )
}

/**
 * Diálogo para unirse a una familia mediante código y contraseña.
 */
@Composable
fun JoinFamilyDialog(onDismiss: () -> Unit, onJoined: () -> Unit) {
    var code by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = {
            code = ""
            password = ""
            onDismiss()
        },
        title = { CustomText("Unirse a Familia") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CustomTextField(value = code, onValueChange = { code = it }, label = "Código")
                CustomTextField(value = password, onValueChange = { password = it }, label = "Contraseña")
            }
        },
        confirmButton = {
            CustomButton(text = "Unirse", onClick = {
                val userHelper = User(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
                coroutineScope.launch {
                    val joinResult = userHelper.joinGroupByCode(code, password)
                    withContext(Dispatchers.Main) {
                        if (joinResult.isSuccess) {
                            Toast.makeText(context, "Te has unido a la familia", Toast.LENGTH_SHORT).show()
                            onDismiss()
                            onJoined()
                        } else {
                            Toast.makeText(context, joinResult.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            })
        },
        dismissButton = {
            CustomButton(text = "Cancelar", onClick = {
                code = ""
                password = ""
                onDismiss()
            })
        },
        containerColor = Color.White
    )
}

/**
 * Pantalla que muestra los detalles de la familia a la que pertenece el usuario,
 * Permite salir de la familia.
 */
@Composable
fun FamilyDetails(navController: NavHostController, viewModel: FamiliaViewModel) {
    val context = LocalContext.current
    var showLeaveDialog by remember { mutableStateOf(false) }
    val miembros by viewModel.miembros.collectAsState()
    val ownerNombre by viewModel.ownerNombre.collectAsState()
    val familyData by viewModel.familyData.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Header(navController)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            familyData?.let {
                CustomText("Código", color = Verde, fontSize = 28.sp)
                Divider(modifier = Modifier.padding(vertical = 5.dp).width(88.dp), color = Verde)
                CustomText(text = it.code, fontSize = 25.sp)

                Spacer(modifier = Modifier.padding(vertical = 8.dp))

                CustomText("Contraseña", color = Verde, fontSize = 28.sp)
                Divider(modifier = Modifier.padding(vertical = 5.dp).width(140.dp), color = Verde)
                CustomText(text = it.password, fontSize = 25.sp)

                Spacer(modifier = Modifier.padding(vertical = 8.dp))

                CustomText("Creador", color = Verde, fontSize = 28.sp)
                Divider(modifier = Modifier.padding(vertical = 5.dp).width(93.dp), color = Verde)
                if (ownerNombre != null) {
                    CustomText(text = ownerNombre!!, fontSize = 25.sp)
                }

                Spacer(modifier = Modifier.padding(vertical = 8.dp))

                CustomText("Miembros", color = Verde, fontSize = 28.sp)
                Divider(modifier = Modifier.padding(vertical = 5.dp).width(125.dp), color = Verde)
                miembros.forEach { miembro ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Image(
                            painter = painterResource(id = getAvatarResId(miembro.foto)),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(45.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        CustomText(text = miembro.nombre, fontSize = 25.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            } ?: CustomText(text = "Cargando datos de la familia...")

            Spacer(modifier = Modifier.height(32.dp))
            CustomButton(text = "Salir de la familia", onClick = { showLeaveDialog = true })

            if (showLeaveDialog) {
                ConfirmLeaveFamilyDialog(
                    onDismiss = { showLeaveDialog = false },
                    onConfirm = {
                        leaveFamily(context, viewModel)
                        showLeaveDialog = false
                    }
                )
            }
        }

        Footer(navController, family = R.drawable.family_fill)
    }
}

/**
 * Devuelve el recurso drawable del avatar según el nombre del avatar.
 */
fun getAvatarResId(nombreAvatar: String): Int {
    return when (nombreAvatar) {
        "pan" -> R.drawable.pan
        "zanahoria" -> R.drawable.zanahoria
        "leche" -> R.drawable.leche
        "chocolate" -> R.drawable.chocolate
        "account_verde" -> R.drawable.account_verde
        else -> R.drawable.account_amarillo
    }
}

/**
 * Diálogo que confirma si el usuario desea salir de la familia.
 */
@Composable
fun ConfirmLeaveFamilyDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            CustomTitulo("Salir de la familia", fontSize = 28.sp)
        },
        text = {
            CustomText("¿Estás seguro de que deseas salir de la familia? Esta acción no se puede deshacer.", fontSize = 20.sp)
        },
        confirmButton = {
            CustomButton(
                text = "Salir",
                onClick = { onConfirm() }
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

/**
 * Función que realiza la operación de salir de la familia,
 */
private fun leaveFamily(context: Context, viewModel: FamiliaViewModel) {
    val userHelper = User(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
    CoroutineScope(Dispatchers.IO).launch {
        val result = userHelper.leaveGroup()
        withContext(Dispatchers.Main) {
            if (result.isSuccess) {
                Toast.makeText(context, "Has salido de la familia", Toast.LENGTH_SHORT).show()
                viewModel.setHasFamily(false)
            } else {
                Toast.makeText(context, result.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
            }
        }
    }
}
