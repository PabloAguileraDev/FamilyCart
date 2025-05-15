package com.pablo.familycart.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pablo.familycart.components.CustomButton
import com.pablo.familycart.components.CustomText
import com.pablo.familycart.components.CustomTextField
import com.pablo.familycart.components.CustomTitulo
import com.pablo.familycart.navigation.Login
import com.pablo.familycart.ui.theme.Amarillo
import com.pablo.familycart.viewModels.RegistroViewModel

@Composable
fun RegistroScreen(navController: NavController, viewModel: RegistroViewModel){

    Box(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            CustomTitulo("Registro")

            Spacer(modifier = Modifier.height(20.dp))

            CustomTextField(value = viewModel.nombre, onValueChange = { viewModel.nombre = it }, label = "Nombre")
            Spacer(modifier = Modifier.height(8.dp))

            CustomTextField(value = viewModel.apellidos, onValueChange = { viewModel.apellidos = it }, label = "Apellidos")
            Spacer(modifier = Modifier.height(8.dp))

            CustomTextField(value = viewModel.email, onValueChange = { viewModel.email = it }, label = "Email")
            Spacer(modifier = Modifier.height(8.dp))

            CustomTextField(value = viewModel.password, onValueChange = { viewModel.password = it }, label = "Contraseña", isPassword = true)
            Spacer(modifier = Modifier.height(8.dp))

            CustomTextField(value = viewModel.confirmPassword, onValueChange = { viewModel.confirmPassword = it }, label = "Confirmar contraseña", isPassword = true)
            Spacer(modifier = Modifier.height(20.dp))


//            Row(
//                verticalAlignment = Alignment.CenterVertically
//            ){
//
//                CustomText("He leído y acepto los ", fontSize = 18.sp)
//
//                CustomText("términos y condiciones", color = Amarillo, fontSize = 18.sp)
//            }
//            Spacer(modifier = Modifier.height(20.dp))

            CustomButton(text = "Registrarse", onClick = {
                viewModel.registerUser(
                    onSuccess = {
                        println("Registro exitoso")
                        // Navegación home
                    },
                    onFailure = { error ->
                        println("Error de registro: $error")
                    }
                )
            })

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ){

                CustomText("¿Ya tienes cuenta? ", color = Amarillo)

                CustomButton(text = "Iniciar sesión", onClick = {
                    navController.navigate(Login)
                })
            }

        }
    }
}
