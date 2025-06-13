package com.pablo.familycart.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.*
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.pablo.familycart.ui.theme.Amarillo
import com.pablo.familycart.R
import com.pablo.familycart.components.*
import com.pablo.familycart.navigation.Categorias
import com.pablo.familycart.navigation.Registro
import com.pablo.familycart.viewModels.LoginViewModel

/**
 * Pantalla de inicio de sesión donde el usuario puede ingresar su email y contraseña.
 */
@Composable
fun LoginScreen(navController: NavController, viewModel: LoginViewModel) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(WindowInsets.ime.asPaddingValues()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_mercadona_fondo_blanco),
                contentDescription = "logo",
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))
            CustomTitulo("Iniciar Sesión")
            Spacer(modifier = Modifier.height(20.dp))

            CustomTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = password,
                onValueChange = { password = it },
                label = "Contraseña",
                isPassword = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.login(
                            email = email,
                            password = password,
                            onSuccess = {
                                Toast.makeText(context, "Login exitoso", Toast.LENGTH_LONG).show()
                                navController.navigate(Categorias)
                            },
                            onFailure = {
                                dialogMessage = it
                                showDialog = true
                            }
                        )
                    }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomButton(
                text = "Iniciar Sesión",
                onClick = {
                    focusManager.clearFocus()
                    viewModel.login(
                        email = email,
                        password = password,
                        onSuccess = {
                            Toast.makeText(context, "Login exitoso", Toast.LENGTH_LONG).show()
                            navController.navigate(Categorias)
                        },
                        onFailure = {
                            dialogMessage = it
                            showDialog = true
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            CustomText("¿No tienes cuenta? ", color = Amarillo)
            Spacer(modifier = Modifier.height(8.dp))
            CustomButton(text = "Registrarse", onClick = { navController.navigate(Registro) })

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { CustomTitulo("Error", fontSize = 35.sp) },
                    text = { CustomText(dialogMessage, fontSize = 20.sp) },
                    confirmButton = {
                        CustomButton("Cerrar", onClick = { showDialog = false })
                    },
                    containerColor = Color.White
                )
            }
        }
    }
}
