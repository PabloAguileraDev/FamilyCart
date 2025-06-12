package com.pablo.familycart.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.pablo.familycart.R
import com.pablo.familycart.components.CustomText
import com.pablo.familycart.components.Footer
import com.pablo.familycart.components.Header
import com.pablo.familycart.ui.theme.Gris
import com.pablo.familycart.ui.theme.Verde
import com.pablo.familycart.viewModels.DetallesProductoViewModel

@Composable
fun DetallesProductoScreen(
    navController: NavController,
    viewModel: DetallesProductoViewModel,
) {
    val producto by viewModel.producto.collectAsState()
    val esFavorito by viewModel.esFavorito.collectAsState()
    val showNoFamilyDialog = remember { mutableStateOf(false) }

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
                .padding(16.dp)
        ) {
            producto?.let {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "volver",
                        modifier = Modifier
                            .size(55.dp)
                            .clickable {
                                navController.popBackStack()
                            },
                        contentScale = ContentScale.Crop
                    )
                    val starIcon = if (esFavorito) R.drawable.star_fill else R.drawable.star
                    Image(
                        painter = painterResource(id = starIcon),
                        contentDescription = "favorito",
                        modifier = Modifier
                            .size(55.dp)
                            .clickable {
                                viewModel.alternarFavorito(
                                    onNoFamily = { showNoFamilyDialog.value = true }
                                )
                            },
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = rememberImagePainter(it.thumbnail),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(16.dp))
                CustomText(text = it.display_name, fontSize = 34.sp)
                Spacer(modifier = Modifier.height(8.dp))
                CustomText(text = it.packaging, color = Gris, fontSize = 28.sp)
                Spacer(modifier = Modifier.height(8.dp))
                CustomText(
                    text = "${it.price_instructions.unit_price} €",
                    color = Verde,
                    fontSize = 28.sp
                )
            } ?: CustomText("Cargando...")
        }

        if (showNoFamilyDialog.value) {
            AlertDialog(
                onDismissRequest = { showNoFamilyDialog.value = false },
                confirmButton = {
                    com.pablo.familycart.components.CustomButton(
                        text = "Entendido",
                        onClick = { showNoFamilyDialog.value = false }
                    )
                },
                title = {
                    CustomText(text = "No perteneces a una familia", color = Verde)
                },
                text = {
                    CustomText(
                        text = "No puedes añadir productos a favoritos sin estar en una familia.",
                        fontSize = 20.sp
                    )
                },
                containerColor = Color.White
            )
        }


        Footer(navController)
    }
}

