package com.pablo.familycart.utils.apiUtils

import com.pablo.familycart.models.*
import retrofit2.http.GET
import retrofit2.http.Path

// Interfaz de la API
interface MercadonaApiService {
    @GET("categories/")
    suspend fun getCategories(): CategoriesResponse

    @GET("categories/{id}/")
    suspend fun getCategoryById(@Path("id") id: Any): ProductResponse

    @GET("products/{id}/")
    suspend fun getProductById(@Path("id") id: Any): Product
}

// Repositorio de consultas a la API
object MercadonaRepository {

    private val api = ApiClient.retrofit.create(MercadonaApiService::class.java)

    val categoryList = mutableListOf<Category>()
    val subCategoryList = mutableListOf<SubCategory>()

    /**
     * Carga todas las categorías y subcategorías desde la API.
     */
    suspend fun loadCategories() {
        try {
            val response = api.getCategories()

            categoryList.clear()
            subCategoryList.clear()

            categoryList.addAll(response.results)
            response.results.forEach { category ->
                subCategoryList.addAll(category.categories)
            }
        } catch (e: Exception) {
            println("Error al cargar categorías: ${e.message}")
        }
    }

    /**
     * Devuelve la respuesta completa de una categoría con subcategorías y productos.
     */
    suspend fun getCategoryWithProducts(id: Any): ProductResponse {
        return api.getCategoryById(id)
    }

    /**
     * Obtiene un producto específico por su ID.
     */
    suspend fun getProductById(id: String): Product? {
        return try {
            api.getProductById(id)
        } catch (e: Exception) {
            println("Error al obtener producto por ID: ${e.message}")
            null
        }
    }
}
