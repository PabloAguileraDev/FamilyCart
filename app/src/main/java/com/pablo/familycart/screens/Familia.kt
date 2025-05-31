package com.pablo.familycart.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pablo.familycart.R
import com.pablo.familycart.components.CustomButton
import com.pablo.familycart.components.CustomText
import com.pablo.familycart.components.CustomTextField
import com.pablo.familycart.components.Footer
import com.pablo.familycart.components.Header
import com.pablo.familycart.data.User
import com.pablo.familycart.ui.theme.Verde
import com.pablo.familycart.viewModels.FamiliaViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FamiliaScreen(navController: NavHostController, viewModel: FamiliaViewModel = viewModel()) {
    val hasFamily by viewModel.hasFamily.collectAsState()

    when (hasFamily) {
        null -> {
            // Cargando
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        false -> {
            NoFamilyUI(navController, viewModel)
        }
        true -> {
            LaunchedEffect(Unit) {
                //viewModel.loadFamilyDetails()
            }
            FamilyDetails(navController, viewModel)
        }
    }
}

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
            CustomText(text = "No perteneces a ninguna familia", color = Verde)
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
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear Familia") },
        text = {
            Column {
                CustomTextField(value = password, onValueChange = { password = it }, label = "Contraseña")
            }
        },
        confirmButton = {
            CustomButton(text = "Crear", onClick = {
                val userHelper = User(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
                CoroutineScope(Dispatchers.IO).launch {
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
            CustomButton(text = "Cancelar", onClick = onDismiss)
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

@Composable
fun FamilyDetails(navController: NavHostController, viewModel: FamiliaViewModel = viewModel()) {
    val familyData by viewModel.familyData.collectAsState()
    val members by viewModel.members.collectAsState()

    if (familyData == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

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
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            CustomText(text = "Código: ${familyData?.code ?: "Cargando..."}", color = Verde)
            Spacer(modifier = Modifier.height(8.dp))

            CustomText(text = "Dueño: ${familyData?.ownerId ?: "Cargando..."}", color = Verde)
            Spacer(modifier = Modifier.height(8.dp))

            CustomText(text = "Miembros:", color = Verde)
            Spacer(modifier = Modifier.height(8.dp))

            members.forEach { member ->
                Text("- ${member.nombre} ${member.apellidos} (${member.email})")
            }
        }

        Footer(navController, family = R.drawable.family_fill)
    }
}
