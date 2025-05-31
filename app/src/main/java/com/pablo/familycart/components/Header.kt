package com.pablo.familycart.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pablo.familycart.R
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.pablo.familycart.models.UserData
import com.pablo.familycart.navigation.Login
import com.pablo.familycart.navigation.Perfil
import com.pablo.familycart.viewModels.UserViewModel

@Composable
fun Header(navController: NavController, userViewModel: UserViewModel = viewModel()) {
    val userData by userViewModel.userData.collectAsState()

    val context = LocalContext.current
    val imageResId = remember(userData?.foto) {
        userData?.foto?.let { fotoName ->
            context.resources.getIdentifier(fotoName, "drawable", context.packageName)
        } ?: R.drawable.account_amarillo
    }

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

