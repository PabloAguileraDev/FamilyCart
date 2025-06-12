package com.pablo.familycart.models

import android.os.Parcelable
import com.google.errorprone.annotations.Keep
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Parcelize
// Data class con los datos del usuario
data class UserData(
    val uid: String = "",
    val email: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val foto: String = "",
    val familyId: String? = null
) : Parcelable

// Data class con los datos de la familia
data class FamilyData(
    val code: String = "",
    val password: String = "",
    val ownerId: String = ""
)

// Una subcategoría
data class SubCategory(
    val id: Int,
    val name: String,
    val order: Int,
    val layout: Int,
    val published: Boolean,
    val is_extended: Boolean
)

// Una categoría
data class Category(
    val id: Int,
    val name: String,
    val order: Int,
    val is_extended: Boolean,
    val categories: List<SubCategory>
)

// La respuesta completa del endpoint /categories
data class CategoriesResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<Category>
)

// Conexión a la API
object ApiClient {
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://tienda.mercadona.es/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

// Un producto
data class ProductResponse(
    val id: Int,
    val name: String,
    val categories: List<SubCategoryWithProducts>
)

// Producto de la lista en Firebase
data class ProductoLista(
    val productId: String,
    val cantidad: Int = 1,
    val nota: String? = null
)

//Producto Completo
data class ProductoCompleto(
    val producto: Product,
    val productoLista: ProductoLista
)

// Los productos de una subcategoría
data class SubCategoryWithProducts(
    val id: Int,
    val name: String,
    val products: List<Product>
)

// Los detalles necesarios de un producto
data class Product(
    val id: String,
    val slug: String,
    val display_name: String,
    val thumbnail: String,
    val packaging: String,
    val price_instructions: PriceInstructions
)

// Detalles extra del producto
data class PriceInstructions(
    val unit_price: String,
    val size_format: String,
    val unit_size: String,
    val reference_format: String,
    val previous_unit_price: String?,
    val unit_name: String,
    val pack_size: String,
    val total_units: String,
    val is_pack: Boolean = false,
)

data class ProductoConDetalles(
    val producto: Product,
    val productoLista: ProductoLista
)

data class ProductoHistorial(
    val productId: String = "",
    val precio: Double = 0.0,
    val cantidad: Int = 0,
    val nombreProducto: String? = null,
)

data class HistorialCompra(
    val id: String = "",
    val nombre_lista: String = "",
    val fecha: Timestamp = Timestamp.now(),
    val precio_total: Double = 0.0,
    val productos: List<ProductoHistorial> = emptyList()
)
