package com.pablo.familycart.utils.apiUtils

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// Una subcategoría como "Aceite, vinagre y sal"
data class SubCategory(
    val id: Int,
    val name: String,
    val order: Int,
    val layout: Int,
    val published: Boolean,
    val is_extended: Boolean
)

// Una categoría como "Aceite, especias y salsas"
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

data class ProductResponse(
    val id: Int,
    val name: String,
    val categories: List<SubCategoryWithProducts>
)

data class SubCategoryWithProducts(
    val id: Int,
    val name: String,
    val products: List<Product>
)

data class Product(
    val id: Int,
    val slug: String,
    val display_name: String,
    val thumbnail: String,
    val packaging: String,
    val price_instructions: PriceInstructions
)

data class PriceInstructions(
    val unit_price: String,
    val size_format: String,
    val unit_size: String,
    val reference_format: String,
    val previous_unit_price: String?
)


// Petición a la API
interface MercadonaApiService {
    @GET("categories/")
    suspend fun getCategories(): CategoriesResponse

    @GET("categories/{id}/")
    suspend fun getCategoryById(@Path("id") id: Any): ProductResponse

    @GET("products/{id}/")
    suspend fun getProductById(@Path("id") id: Any): Product
}

object MercadonaRepository {
    private val api = ApiClient.retrofit.create(MercadonaApiService::class.java)

    // Aquí guardaremos las categorías y subcategorías
    val categoryList = mutableListOf<Category>()
    val subCategoryList = mutableListOf<SubCategory>()

    // Función para cargar las categorías de forma asíncrona
    suspend fun loadCategories() {
        try {
            // Realizamos la llamada HTTP de forma suspendida (espera hasta obtener respuesta)
            val categoriesResponse = api.getCategories()

            // Guardamos las categorías principales
            categoryList.clear()
            categoryList.addAll(categoriesResponse.results)

            // Extraemos las subcategorías y las agregamos
            subCategoryList.clear()
            categoriesResponse.results.forEach { category ->
                subCategoryList.addAll(category.categories)
            }
        } catch (e: Exception) {
            println("Error al cargar categorías: ${e.message}")
        }
    }

    suspend fun getProductsBySubcategoryId(id: Any): List<Product> {
        return try {
            val response = api.getCategoryById(id)
            // Asumimos que los productos están en la primera subcategoría
            response.categories.firstOrNull()?.products ?: emptyList()
        } catch (e: Exception) {
            println("Error cargando productos: ${e.message}")
            emptyList()
        }
    }

    suspend fun getCategoryWithProducts(id: Any): ProductResponse {
        return api.getCategoryById(id)
    }

    suspend fun getProductById(id: Int): Product? {
        return try {
            api.getProductById(id)
        } catch (e: Exception) {
            println("Error al obtener producto por ID: ${e.message}")
            null
        }
    }


}