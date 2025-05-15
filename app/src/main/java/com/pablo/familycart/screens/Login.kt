package com.pablo.familycart.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pablo.familycart.ui.theme.Amarillo
import com.pablo.familycart.ui.theme.Verde
import com.pablo.familycart.R
import com.pablo.familycart.components.CustomButton
import com.pablo.familycart.components.CustomText
import com.pablo.familycart.components.CustomTextField
import com.pablo.familycart.components.CustomTitulo
import com.pablo.familycart.navigation.Categorias
import com.pablo.familycart.navigation.Registro
import com.pablo.familycart.viewModels.LoginViewModel

@Composable
fun LoginScreen (navController: NavController, viewModel: LoginViewModel){
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
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

            CustomTextField(value = email, onValueChange = { email = it }, label = "Email")
            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(value = password, onValueChange = { password = it }, label = "Contraseña", isPassword = true)
            Spacer(modifier = Modifier.height(16.dp))

            CustomButton(text = "Iniciar Sesión", onClick = {
                viewModel.login(
                    email = email,
                    password = password,
                    onSuccess = {
                        println("Inicio de sesión exitoso")
                        navController.navigate(Categorias)
                    },
                    onFailure = { error ->
                        println("Error al iniciar sesión: $error")
                    }
                )
            })

            Spacer(modifier = Modifier.height(16.dp))

            CustomText("¿Has olvidado la contraseña? ", color = Amarillo, fontSize = 18.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ){

                CustomText("¿No tienes cuenta? ", color = Verde)

                CustomButton(text = "Registrarse", onClick = { navController.navigate(Registro) })
            }

        }
    }
}
