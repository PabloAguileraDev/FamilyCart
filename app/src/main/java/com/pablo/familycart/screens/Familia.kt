package com.pablo.familycart.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pablo.familycart.components.CustomText
import com.pablo.familycart.data.User
import com.pablo.familycart.viewModels.FamiliaViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FamiliaScreen(navController: NavHostController, viewModel: FamiliaViewModel = viewModel()) {
    val hasFamily by viewModel.hasFamily.collectAsState()

    // Lógica de verificación al iniciar
    LaunchedEffect(Unit) {
        viewModel.checkFamilyStatus()
    }

    when (hasFamily) {
        null -> {
            // Cargando
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        false -> {
            NoFamilyUI(viewModel)
        }
        true -> {
            // UI para usuarios con familia (por implementar)
            CustomText("Ya perteneces a una familia")
        }
    }
}

@Composable
fun NoFamilyUI(viewModel: FamiliaViewModel) {
    var showJoinDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { showCreateDialog = true }) {
            Text("Crear familia")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { showJoinDialog = true }) {
            Text("Unirse a una familia")
        }
    }

    if (showCreateDialog) {
        CreateFamilyDialog(
            onDismiss = { showCreateDialog = false },
            onCreated = {
                viewModel.checkFamilyStatus()
            }
        )
    }

    if (showJoinDialog) {
        JoinFamilyDialog(
            onDismiss = { showJoinDialog = false },
            onJoined = { viewModel.checkFamilyStatus() }
        )
    }
}

@Composable
fun CreateFamilyDialog(onDismiss: () -> Unit, onCreated: () -> Unit) {
    var code by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear Familia") },
        text = {
            Column {
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Código") })
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val userHelper = User(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
                CoroutineScope(Dispatchers.IO).launch {
                    val result = userHelper.createGroup(code, password)
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
            }) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun JoinFamilyDialog(onDismiss: () -> Unit, onJoined: () -> Unit) {
    var code by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Unirse a Familia") },
        text = {
            Column {
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Código") })
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val userHelper = User(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
                CoroutineScope(Dispatchers.IO).launch {
                    val result = userHelper.joinGroupByCode(code, password)
                    withContext(Dispatchers.Main) {
                        if (result.isSuccess) {
                            Toast.makeText(context, "Unido a la familia", Toast.LENGTH_SHORT).show()
                            onDismiss()
                            onJoined()
                        } else {
                            Toast.makeText(context, result.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }) {
                Text("Unirse")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}


