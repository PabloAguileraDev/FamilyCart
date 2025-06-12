package com.pablo.familycart.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
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
data class DetallesProducto(val productoId: String)

@Serializable
object Familia

@Serializable
object Perfil

@Serializable
object Lista

@Serializable
data class ProductosLista(val familyId: String, val listId: String) {
    override fun toString(): String = "productos_lista/$familyId/$listId"
}

@Parcelize
data class DetallesProductoLista(val productoId: String, val listId: String): Parcelable

@Serializable
object Compra

@Serializable
object Favoritos

@Serializable
object DetallesCompra