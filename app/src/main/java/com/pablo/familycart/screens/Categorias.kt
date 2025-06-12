package com.pablo.familycart.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pablo.familycart.R
import com.pablo.familycart.components.CustomText
import com.pablo.familycart.components.Header
import com.pablo.familycart.components.Footer
import com.pablo.familycart.navigation.Productos
import com.pablo.familycart.ui.theme.Verde
import com.pablo.familycart.viewModels.CategoriasViewModel

@Composable
fun CategoriasScreen(navController: NavController, viewModel: CategoriasViewModel) {
    val categorias by viewModel.categorias.collectAsState()
    val expandedCategoryIds by viewModel.expandedCategoryIds.collectAsState()
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(categorias) {
        if (categorias.isNotEmpty()) {
            showContent = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Header(navController)

        if (categorias.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CustomText(text = "Cargando...", fontSize = 18.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                items(categorias) { categoria ->
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, shape = RoundedCornerShape(12.dp))
                                .border(2.dp, Verde, shape = RoundedCornerShape(12.dp))
                                .clickable { viewModel.toggleCategoriaExpandida(categoria.id) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomText(text = categoria.name, fontSize = 20.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            Image(
                                painter = painterResource(id = R.drawable.arrow_right),
                                contentDescription = "Desplegar",
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        if (expandedCategoryIds.contains(categoria.id)) {
                            categoria.categories.forEach { sub ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            navController.navigate(Productos(subcatId = sub.id))
                                        }
                                        .padding(start = 32.dp, top = 6.dp, bottom = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CustomText(text = sub.name, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Image(
                                        painter = painterResource(id = R.drawable.arrow_right),
                                        contentDescription = "Ir a productos",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        Footer(navController, home = R.drawable.home_fill)
    }
}


