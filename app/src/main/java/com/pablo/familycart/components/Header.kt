package com.pablo.familycart.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pablo.familycart.R
import com.pablo.familycart.navigation.Perfil
import com.pablo.familycart.viewModels.UserViewModel

/**
 * Cabecera de la app que muestra el logo y el avatar del usuario.
 *
 * @param navController Controlador de navegación para redirigir a la pantalla de perfil.
 * @param userViewModel ViewModel que proporciona los datos del usuario logueado.
 * @param modifier Permite modificar el contenedor externo (por defecto vacío).
 */
@Composable
fun Header(
    navController: NavController,
    modifier: Modifier = Modifier,
    userViewModel: UserViewModel = viewModel()
) {
    // Obtengo el usuario desde el ViewModel
    val userData by userViewModel.userData.collectAsState()

    // Contexto necesario para acceder a los recursos
    val context = LocalContext.current

    // Determina la ruta de la imagen según el nombre proporcionado o usa una por defecto
    val imageResId = remember(userData?.foto) {
        userData?.foto?.let { fotoName ->
            context.resources.getIdentifier(fotoName, "drawable", context.packageName)
        } ?: R.drawable.account_amarillo
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Logo de Mercadona
        Image(
            painter = painterResource(id = R.drawable.logo_mercadona_completo),
            contentDescription = "logo",
            modifier = Modifier.height(40.dp),
            contentScale = ContentScale.FillHeight
        )

        // Imagen de perfil del usuario, navega a la pantalla de perfil al pulsar
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = "cuenta",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable {
                    navController.navigate(Perfil)
                },
            contentScale = ContentScale.Crop
        )
    }
}
