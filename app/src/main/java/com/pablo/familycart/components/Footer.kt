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
import com.pablo.familycart.navigation.Categorias
import com.pablo.familycart.navigation.Familia

@Composable
fun Footer(
    navController: NavController,
    home: Int = R.drawable.home,
    cart: Int = R.drawable.cart,
    heart: Int = R.drawable.heart,
    family: Int = R.drawable.family
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Image(
            painter = painterResource(id = home),
            contentDescription = "categorias",
            modifier = Modifier
                .size(35.dp)
                .clickable {
                    navController.navigate(Categorias)
                },
            contentScale = ContentScale.Crop
        )

        Image(
            painter = painterResource(id = cart),
            contentDescription = "lista",
            modifier = Modifier
                .size(35.dp)
                .clickable {
                    //navController.navigate(Login)
                },
            contentScale = ContentScale.Crop
        )

        Image(
            painter = painterResource(id = heart),
            contentDescription = "favoritos",
            modifier = Modifier
                .size(35.dp)
                .clickable {
                    //navController.navigate(Login)
                },
            contentScale = ContentScale.Crop
        )

        Image(
            painter = painterResource(id = family),
            contentDescription = "familia",
            modifier = Modifier
                .size(35.dp)
                .clickable {
                    navController.navigate(Familia)
                },
            contentScale = ContentScale.Crop
        )
    }
}