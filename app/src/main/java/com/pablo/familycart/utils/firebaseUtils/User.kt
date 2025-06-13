package com.pablo.familycart.utils.firebaseUtils

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pablo.familycart.models.*
import com.pablo.familycart.utils.apiUtils.MercadonaRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

/**
 * Repositorio principal de usuario. Maneja autenticación, grupos, listas, historial y favoritos
 */
class User(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore
) {

    /**
     * Inicia sesión con email y contraseña en Firebase Auth
     */
    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(traducirErrorFirebase(e.message ?: "")))
        }
    }

    /**
     * Registra un nuevo usuario y lo guarda en Firestore con un avatar aleatorio
     */
    suspend fun register(nombre: String, apellidos: String, email: String, password: String): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: return Result.failure(Exception("Error al registrar el usuario"))

            val randomAvatar = listOf("pan", "zanahoria", "leche", "chocolate").random()

            val userData = UserData(user.uid, email, nombre, apellidos, randomAvatar)

            db.collection("users").document(user.uid).set(userData).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(traducirErrorFirebase(e.message ?: "")))
        }
    }

    /**
     * Traduce errores comunes de Firebase a español
     */
    private fun traducirErrorFirebase(mensaje: String): String {
        return when {
            mensaje.contains("email address is already in use", true) -> "El email ya está registrado"
            mensaje.contains("badly formatted", true) -> "El formato del email no es válido"
            mensaje.contains("password is invalid", true) -> "La contraseña es incorrecta"
            mensaje.contains("no user record", true) -> "No existe ninguna cuenta con ese email"
            mensaje.contains("auth credential is incorrect", true) -> "Las credenciales proporcionadas son incorrectas o han expirado"
            mensaje.contains("network error", true) -> "Error de red. Comprueba tu conexión a internet"
            mensaje.contains("is empty or null", true) -> "Debes completar todos los campos"
            else -> "Ha ocurrido un error: $mensaje"
        }
    }

    /**
     * Obtiene el ID del grupo familiar al que pertenece el usuario
     */
    suspend fun getFamilyId(): String? {
        val currentUser = auth.currentUser ?: return null
        val userDoc = db.collection("users").document(currentUser.uid).get().await()
        return userDoc.getString("familyId")
    }

    /**
     * Crea un nuevo grupo y asocia al usuario como propietario
     */
    suspend fun createGroup(password: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Usuario no autenticado"))

        return try {
            val code = generateUniqueFamilyCode()
            val group = FamilyData(code = code, password = password, ownerId = user.uid)

            val groupRef = db.collection("groups").document()
            groupRef.set(group).await()

            db.collection("users").document(user.uid).update("familyId", groupRef.id).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Se une a una familia usando su código y contraseña
     */
    suspend fun joinGroupByCode(code: String, password: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Usuario no autenticado"))

        return try {
            val querySnapshot = db.collection("groups").whereEqualTo("code", code).get().await()
            if (querySnapshot.isEmpty) return Result.failure(Exception("Código de grupo inválido"))

            val groupDoc = querySnapshot.documents.first()
            val dbPassword = groupDoc.getString("password")

            if (dbPassword != password) return Result.failure(Exception("Contraseña incorrecta"))

            val groupId = groupDoc.id
            db.collection("users").document(user.uid).update("familyId", groupId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sale de la familia actual
     */
    suspend fun leaveGroup(): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Usuario no autenticado"))
        return try {
            db.collection("users").document(user.uid).update("familyId", null).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Genera un código único de 4 caracteres para un grupo
     */
    private suspend fun generateUniqueFamilyCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        var code: String
        var exists: Boolean

        do {
            code = (1..4).map { chars.random() }.joinToString("")
            val snapshot = db.collection("groups").whereEqualTo("code", code).get().await()
            exists = !snapshot.isEmpty
        } while (exists)

        return code
    }

    /**
     * Crea una nueva lista dentro del grupo del usuario actual
     */
    suspend fun createList(listName: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Usuario no autenticado"))

        return try {
            val familyId = getFamilyId() ?: return Result.failure(Exception("Usuario sin grupo"))

            val listData = mapOf(
                "name" to listName,
                "createdBy" to user.uid,
                "timestamp" to Timestamp.now()
            )

            db.collection("groups").document(familyId)
                .collection("lists").add(listData).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene todas las listas del grupo del usuario, ordenadas por fecha
     */
    suspend fun getUserLists(): Result<List<Pair<String, String>>> {
        return try {
            val familyId = getFamilyId() ?: return Result.failure(Exception("Usuario sin grupo"))

            val snapshot = db.collection("groups").document(familyId)
                .collection("lists")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await()

            val listas = snapshot.documents.map {
                it.id to (it.getString("name") ?: "Sin nombre")
            }

            Result.success(listas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene el nombre de una lista específica por ID
     */
    suspend fun getNombreLista(familyId: String, listId: String): String? {
        return try {
            db.collection("groups").document(familyId)
                .collection("lists").document(listId).get().await()
                .getString("name")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Obtiene los productos de una lista
     */
    suspend fun getProductosFromLista(familyId: String, listId: String): List<ProductoLista> {
        return try {
            db.collection("groups").document(familyId)
                .collection("lists").document(listId)
                .collection("items").get().await()
                .documents.mapNotNull {
                    val productId = it.getString("productId") ?: return@mapNotNull null
                    val cantidad = it.getLong("cantidad")?.toInt() ?: 1
                    val nota = it.getString("nota")
                    ProductoLista(productId, cantidad, nota)
                }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Agrega un producto a la lista
     */
    suspend fun addProductToList(listId: String, productId: String, nota: String, cantidad: Int): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Usuario no autenticado"))
        val familyId = getFamilyId() ?: return Result.failure(Exception("Usuario sin grupo"))

        return try {
            val itemsRef = db.collection("groups").document(familyId)
                .collection("lists").document(listId).collection("items")

            val exists = itemsRef.whereEqualTo("productId", productId).limit(1).get().await()
            if (!exists.isEmpty) return Result.failure(Exception("Este producto ya está en la lista"))

            val productData = mapOf(
                "productId" to productId,
                "addedBy" to user.uid,
                "nota" to nota,
                "cantidad" to cantidad,
                "timestamp" to Timestamp.now()
            )

            itemsRef.add(productData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina un producto de una lista por su ID
     */
    suspend fun removeProductFromList(listId: String, productId: String): Result<Unit> {
        val familyId = getFamilyId() ?: return Result.failure(Exception("Usuario sin grupo"))

        return try {
            val itemsRef = db.collection("groups").document(familyId)
                .collection("lists").document(listId).collection("items")

            val snapshot = itemsRef.whereEqualTo("productId", productId).get().await()
            snapshot.documents.forEach { it.reference.delete().await() }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina varios productos de una lista
     */
    suspend fun removeProductsFromList(familyId: String, listId: String, productos: List<ProductoCompra>) {
        productos.forEach {
            val snapshot = db.collection("groups").document(familyId)
                .collection("lists").document(listId)
                .collection("items")
                .whereEqualTo("productId", it.producto.id)
                .get().await()

            snapshot.documents.forEach { doc ->
                try {
                    doc.reference.delete().await()
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * Obtiene los detalles de un producto desde la API de Mercadona por su ID
     */
    suspend fun getProductosDeListaConDetalles(familyId: String, listId: String): List<ProductoConDetalles> =
        coroutineScope {
            getProductosFromLista(familyId, listId).map { pl ->
                async {
                    MercadonaRepository.getProductById(pl.productId)?.let {
                        ProductoConDetalles(it, pl)
                    }
                }
            }.awaitAll().filterNotNull()
        }

    /**
     * Guarda una compra finalizada en el historial
     */
    suspend fun guardarCompra(familyId: String, productos: List<ProductoCompra>, nombreLista: String) {
        val precioTotal = productos.sumOf {
            (it.producto.price_instructions.unit_price.toDoubleOrNull() ?: 0.0) * it.productoLista.cantidad
        }

        val productosMap = productos.map {
            mapOf(
                "productId" to it.producto.id,
                "precio" to (it.producto.price_instructions.unit_price.toDoubleOrNull() ?: 0.0),
                "cantidad" to it.productoLista.cantidad
            )
        }

        val data = mapOf(
            "fecha" to Timestamp.now(),
            "precio_total" to precioTotal,
            "productos" to productosMap,
            "nombre_lista" to nombreLista
        )

        db.collection("groups").document(familyId)
            .collection("historial").add(data).await()
    }

    /**
     * Comprueba si un producto está en la lista de favoritos
     */
    suspend fun esFavorito(familyId: String, productId: String): Boolean {
        return db.collection("groups").document(familyId)
            .collection("favoritos").document(productId)
            .get().await().exists()
    }

    /**
     * Agrega un producto a la lista de favoritos
     */
    suspend fun addToFavoritos(familyId: String, productId: String) {
        db.collection("groups").document(familyId)
            .collection("favoritos").document(productId)
            .set(mapOf("id" to productId)).await()
    }

    /**
     * Elimina un producto de la lista de favoritos
     */
    suspend fun removeFromFavoritos(familyId: String, productId: String) {
        db.collection("groups").document(familyId)
            .collection("favoritos").document(productId)
            .delete().await()
    }
}
