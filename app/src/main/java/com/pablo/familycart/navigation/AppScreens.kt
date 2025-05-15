package com.pablo.familycart.navigation

import kotlinx.serialization.Serializable

@Serializable
object Login

@Serializable
object Registro

@Serializable
object Categorias

@Serializable
data class Productos(val subcatId: Int)

@Serializable
data class DetallesProducto(val productoId: Int)

@Serializable
object Familia
