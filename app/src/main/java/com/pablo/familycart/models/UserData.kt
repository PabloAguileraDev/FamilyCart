package com.pablo.familycart.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


/**
 * Datos de un usuario.
 */
@Parcelize
data class UserData(
    val uid: String = "",
    val email: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val foto: String = "",
    val familyId: String? = null
) : Parcelable

/**
 * Información de una familia
 */
data class FamilyData(
    val code: String = "",
    val password: String = "",
    val ownerId: String = ""
)

/**
 * Subcategoría de productos
 */
data class SubCategory(
    val id: Int,
    val name: String,
    val order: Int,
    val layout: Int,
    val published: Boolean,
    val is_extended: Boolean
)

/**
 * Categoría de productos
 */
data class Category(
    val id: Int,
    val name: String,
    val order: Int,
    val is_extended: Boolean,
    val categories: List<SubCategory>
)

/**
 * Respuesta de la API al consultar las categorías
 */
data class CategoriesResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<Category>
)

/**
 * Cliente Retrofit para consumir la API de Mercadona
 */
object ApiClient {
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://tienda.mercadona.es/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

/**
 * Producto que devuelve el endpoint /products
 */
data class ProductResponse(
    val id: Int,
    val name: String,
    val categories: List<SubCategoryWithProducts>
)

/**
 * Producto almacenado en una lista de la compra
 */
data class ProductoLista(
    val productId: String,
    val cantidad: Int = 1,
    val nota: String? = null
)

/**
 * Combina un producto con sus datos específicos de la lista a la que pertenece
 */
data class ProductoCompleto(
    val producto: Product,
    val productoLista: ProductoLista
)

/**
 * Subcategoría con sus productos
 */
data class SubCategoryWithProducts(
    val id: Int,
    val name: String,
    val products: List<Product>
)

/**
 * Información principal del producto
 */
data class Product(
    val id: String,
    val slug: String,
    val display_name: String,
    val thumbnail: String,
    val packaging: String,
    val price_instructions: PriceInstructions
)

/**
 * Instrucciones de precio de un producto
 */
data class PriceInstructions(
    val unit_price: String,
    val size_format: String,
    val unit_size: String,
    val reference_format: String,
    val previous_unit_price: String?,
    val unit_name: String,
    val pack_size: String,
    val total_units: String,
    val is_pack: Boolean = false
)

/**
 * Representa un producto con todos los detalles y cómo está en la lista
 */
data class ProductoConDetalles(
    val producto: Product,
    val productoLista: ProductoLista
)

/**
 * Representa un producto junto a su información de lista y si ha sido añadido
 */
data class ProductoCompra(
    val producto: Product,
    val productoLista: ProductoLista,
    val anadido: Boolean = false
)

/**
 * Producto dentro de un historial de compra
 */
data class ProductoHistorial(
    val productId: String = "",
    val precio: Double = 0.0,
    val cantidad: Int = 0,
    val nombreProducto: String? = null
)

/**
 * Representa una compra almacenada en el historial
 */
data class HistorialCompra(
    val id: String = "",
    val nombre_lista: String = "",
    val fecha: Timestamp = Timestamp.now(),
    val precio_total: Double = 0.0,
    val productos: List<ProductoHistorial> = emptyList()
)
