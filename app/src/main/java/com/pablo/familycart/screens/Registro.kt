package com.pablo.familycart.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pablo.familycart.components.*
import com.pablo.familycart.navigation.Login
import com.pablo.familycart.ui.theme.Amarillo
import com.pablo.familycart.viewModels.RegistroViewModel

/**
 * Pantalla de registro de usuario donde se recopilan los datos necesarios para crear una cuenta.
 *
 * @param navController Controlador de navegación para cambiar entre pantallas.
 * @param viewModel ViewModel que contiene la lógica de negocio para registro.
 */
@Composable
fun RegistroScreen(navController: NavController, viewModel: RegistroViewModel) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CustomTitulo("Registro")
            Spacer(modifier = Modifier.height(20.dp))

            CustomTextField(
                value = viewModel.nombre,
                onValueChange = { viewModel.nombre = it },
                label = "Nombre",
                isError = viewModel.nombreError,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }),
                modifier = Modifier.onFocusChanged {
                    if (!it.isFocused) viewModel.validarNombre()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            CustomTextField(
                value = viewModel.apellidos,
                onValueChange = { viewModel.apellidos = it },
                label = "Apellidos",
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }),
                modifier = Modifier.onFocusChanged {
                    if (!it.isFocused) viewModel.validarApellidos()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            CustomTextField(
                value = viewModel.email,
                onValueChange = { viewModel.email = it },
                label = "Email",
                isError = viewModel.emailError,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }),
                modifier = Modifier.onFocusChanged {
                    if (!it.isFocused) viewModel.validarEmail()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            CustomTextField(
                value = viewModel.password,
                onValueChange = { viewModel.password = it },
                label = "Contraseña",
                isPassword = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }),
                modifier = Modifier.onFocusChanged {
                    if (!it.isFocused) viewModel.validarPassword()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            CustomTextField(
                value = viewModel.confirmPassword,
                onValueChange = { viewModel.confirmPassword = it },
                label = "Confirmar contraseña",
                isPassword = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                }),
                modifier = Modifier.onFocusChanged {
                    if (!it.isFocused) viewModel.validarConfirmPassword()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            CustomButton(text = "Registrarse", onClick = {
                viewModel.registerUser(
                    onSuccess = {
                        Toast.makeText(context, "Registro exitoso", Toast.LENGTH_LONG).show()
                        navController.navigate(Login)
                    },
                    onFailure = { error ->
                        dialogMessage = error
                        showDialog = true
                    }
                )
            })

            Spacer(modifier = Modifier.height(20.dp))

            CustomText("¿Ya tienes cuenta? ", color = Amarillo)
            Spacer(modifier = Modifier.height(8.dp))

            CustomButton(text = "Iniciar sesión", onClick = {
                navController.navigate(Login)
            })

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
