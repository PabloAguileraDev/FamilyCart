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
import com.google.firebase.auth.FirebaseAuth
import com.pablo.familycart.navigation.Login

@Composable
fun Header(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_mercadona_completo),
            contentDescription = "logo",
            modifier = Modifier
                .height(40.dp),
            contentScale = ContentScale.FillHeight
        )

        Image(
            painter = painterResource(id = R.drawable.account_amarillo),
            contentDescription = "cuenta",
            modifier = Modifier
                .size(40.dp)
                .clickable {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Login)
                           },
            contentScale = ContentScale.Crop
        )
    }
}

