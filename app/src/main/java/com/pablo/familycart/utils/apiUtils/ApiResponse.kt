package com.pablo.familycart.utils.apiUtils

import com.pablo.familycart.models.*
import retrofit2.http.GET
import retrofit2.http.Path

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

    suspend fun getProductById(id: String): Product? {
        return try {
            api.getProductById(id)
        } catch (e: Exception) {
            println("Error al obtener producto por ID: ${e.message}")
            null
        }
    }


}