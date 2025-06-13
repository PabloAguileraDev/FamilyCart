package com.pablo.familycart.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pablo.familycart.R
import com.pablo.familycart.navigation.Categorias
import com.pablo.familycart.navigation.Familia
import com.pablo.familycart.navigation.Favoritos
import com.pablo.familycart.navigation.Lista

/**
 * Pie de página con navegación entre las pantallas principales de la app.
 *
 * Muestra 4 íconos: Categorías, Lista, Favoritos y Familia, cada uno redirige a una pantalla distinta.
 *
 * @param navController Controlador de navegación de Jetpack Navigation.
 * @param modifier Permite aplicar estilos externos.
 * @param home Icono para la navegación a Categorías.
 * @param cart Icono para la navegación a Lista.
 * @param heart Icono para la navegación a Favoritos.
 * @param family Icono para la navegación a Familia.
 */
@Composable
fun Footer(
    navController: NavController,
    modifier: Modifier = Modifier,
    home: Int = R.drawable.home,
    cart: Int = R.drawable.cart,
    heart: Int = R.drawable.heart,
    family: Int = R.drawable.family
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        FooterIcon(resId = home, description = "categorias") {
            navController.navigate(Categorias)
        }

        FooterIcon(resId = cart, description = "lista") {
            navController.navigate(Lista)
        }

        FooterIcon(resId = heart, description = "favoritos") {
            navController.navigate(Favoritos)
        }

        FooterIcon(resId = family, description = "familia") {
            navController.navigate(Familia)
        }
    }
}

/**
 * Icono individual del footer que navega al destino indicado cuando se hace clic.
 *
 * @param resId Ruta de la imagen.
 * @param description Descripción de la imagen.
 * @param onClick Acción de navegación.
 */
@Composable
private fun FooterIcon(
    resId: Int,
    description: String,
    onClick: () -> Unit
) {
    Image(
        painter = painterResource(id = resId),
        contentDescription = description,
        modifier = Modifier
            .size(35.dp)
            .clickable(onClick = onClick),
        contentScale = ContentScale.Crop
    )
}
