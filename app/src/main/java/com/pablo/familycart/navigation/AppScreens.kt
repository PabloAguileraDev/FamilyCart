package com.pablo.familycart.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Destinos de navegación de la app
 * Cada objeto/clase representa una pantalla o ruta dentro de la aplicación.
 */

@Serializable
object Login

@Serializable
object Registro

@Serializable
object Categorias

@Serializable
data class Productos(val subcatId: Int)

@Serializable
data class DetallesProducto(val productoId: String)

@Serializable
object Familia

@Serializable
object Perfil

@Serializable
object Lista

@Parcelize
data class DetallesProductoLista(val productoId: String, val listId: String) : Parcelable

@Serializable
object Favoritos