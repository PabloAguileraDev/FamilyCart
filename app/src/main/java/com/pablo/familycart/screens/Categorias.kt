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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pablo.familycart.R
import com.pablo.familycart.components.CustomText
import com.pablo.familycart.components.Footer
import com.pablo.familycart.components.Header
import com.pablo.familycart.models.Category
import com.pablo.familycart.models.SubCategory
import com.pablo.familycart.navigation.Productos
import com.pablo.familycart.ui.theme.Verde
import com.pablo.familycart.viewModels.CategoriasViewModel

/**
 * Pantalla que muestra las categorías y subcategorías
 */
@Composable
fun CategoriasScreen(
    navController: NavController,
    viewModel: CategoriasViewModel = viewModel()
) {
    // Observo el estado de las categorías y de las categorías expandidas
    val categorias by viewModel.categorias.collectAsState()
    val expandedCategoryIds by viewModel.expandedCategoryIds.collectAsState()

    val isLoading = categorias.isEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Header(navController)

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CustomText(text = "Cargando...", fontSize = 18.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                items(categorias) { categoria ->
                    CategoriaItem(
                        categoria = categoria,
                        isExpanded = expandedCategoryIds.contains(categoria.id),
                        onToggleExpand = { viewModel.switchCategoriaExpandida(categoria.id) },
                        onSubCategoryClick = { sub ->
                            navController.navigate(Productos(subcatId = sub.id))
                        }
                    )
                }
            }
        }
        Footer(navController, home = R.drawable.home_fill)
    }
}

/**
 * Composable que representa una categoría con subcategorías desplegables
 */
@Composable
fun CategoriaItem(
    categoria: Category,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSubCategoryClick: (SubCategory) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(2.dp, Verde, RoundedCornerShape(12.dp))
                .clickable { onToggleExpand() }
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

        if (isExpanded) {
            categoria.categories.forEach { sub ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSubCategoryClick(sub) }
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
