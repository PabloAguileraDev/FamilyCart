package com.pablo.familycart.data

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pablo.familycart.models.FamilyData
import com.pablo.familycart.models.Product
import com.pablo.familycart.models.ProductoConDetalles
import com.pablo.familycart.models.ProductoLista
import kotlinx.coroutines.tasks.await
import com.pablo.familycart.models.UserData
import com.pablo.familycart.utils.apiUtils.MercadonaRepository
import com.pablo.familycart.viewModels.ProductoCompra
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope


class User(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore
) {
    /**
     * Método para iniciar sesión con el usuario en Firebase Auth
     */
    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            val mensajeTraducido = traducirErrorFirebase(e.message ?: "")
            Result.failure(Exception(mensajeTraducido))
        }
    }

    suspend fun register(
        nombre: String,
        apellidos: String,
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            // Crea el usuario en firebase
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: return Result.failure(Exception("Error al registrar el usuario"))
            val avatarList = listOf(
                "pan",
                "zanahoria",
                "leche",
                "chocolate"
            )
            val randomAvatar = avatarList.random()

            // Usuario a guardar en firebase
            val userData = UserData(
                uid = user.uid,
                nombre = nombre,
                apellidos = apellidos,
                email = email,
                foto = randomAvatar
            )

            // Registra al usuario en firebase
            db.collection("users").document(user.uid).set(userData).await()

            Result.success(Unit)
        } catch (e: Exception) {
            val mensajeTraducido = traducirErrorFirebase(e.message ?: "")
            Result.failure(Exception(mensajeTraducido))
        }
    }

    private fun traducirErrorFirebase(mensaje: String): String {
        return when {
            mensaje.contains("email address is already in use", ignoreCase = true) -> "El email ya está registrado"
            mensaje.contains("badly formatted", ignoreCase = true) -> "El formato del email no es válido"
            mensaje.contains("password is invalid", ignoreCase = true) -> "La contraseña es incorrecta"
            mensaje.contains("no user record", ignoreCase = true) -> "No existe ninguna cuenta con ese email"
            mensaje.contains("auth credential is incorrect", ignoreCase = true) -> "Las credenciales proporcionadas son incorrectas o han expirado"
            mensaje.contains("network error", ignoreCase = true) -> "Error de red. Comprueba tu conexión a internetl"
            mensaje.contains("is empty or null", ignoreCase = true) -> "Debes completar todos los campos"
            else -> "Ha ocurrido un error: $mensaje"
        }
    }


    suspend fun getFamilyId(): String? {
        val currentUser = auth.currentUser ?: return null

        val userDoc = db.collection("users").document(currentUser.uid).get().await()
        return userDoc.getString("familyId")
    }

    /**
     * Crea un nuevo grupo y asigna al usuario actual como dueño y miembro
     */
    suspend fun createGroup(password: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Usuario no autenticado"))

        return try {
            val code = generateUniqueFamilyCode(db)

            val group = FamilyData(
                code = code,
                password = password,
                ownerId = user.uid
            )

            val groupRef = db.collection("groups").document()
            groupRef.set(group).await()

            db.collection("users").document(user.uid).update("familyId", groupRef.id).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    /**
     * Unirse a un grupo con un código
     */
    suspend fun joinGroupByCode(code: String, password: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Usuario no autenticado"))

        return try {
            val querySnapshot = db.collection("groups")
                .whereEqualTo("code", code)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                return Result.failure(Exception("Código de grupo inválido"))
            }

            val groupDoc = querySnapshot.documents.first()
            val groupId = groupDoc.id

            // Asignar grupo al usuario
            db.collection("users").document(user.uid).update("familyId", groupId).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun leaveGroup(): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Usuario no autenticado"))
        return try {
            FirebaseFirestore.getInstance().collection("users").document(user.uid)
                .update("familyId", null).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateUniqueFamilyCode(db: FirebaseFirestore): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        var code: String
        var exists: Boolean

        do {
            code = (1..4)
                .map { chars.random() }
                .joinToString("")

            val snapshot = db.collection("groups")
                .whereEqualTo("code", code)
                .get()
                .await()

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
            val userDoc = db.collection("users").document(user.uid).get().await()
            val familyId = userDoc.getString("familyId") ?: return Result.failure(Exception("Usuario sin grupo"))

            val listData = mapOf(
                "name" to listName,
                "createdBy" to user.uid,
                "timestamp" to com.google.firebase.Timestamp.now()
            )

            db.collection("groups").document(familyId)
                .collection("lists")
                .add(listData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserLists(): Result<List<Pair<String, String>>> {
        val user = auth.currentUser ?: return Result.failure(Exception("Usuario no autenticado"))

        return try {
            val userDoc = db.collection("users").document(user.uid).get().await()
            val familyId = userDoc.getString("familyId") ?: return Result.failure(Exception("Usuario sin grupo"))

            val snapshot = db.collection("groups")
                .document(familyId)
                .collection("lists")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await()

            val listas = snapshot.documents.map { doc ->
                val name = doc.getString("name") ?: "Sin nombre"
                val id = doc.id
                id to name
            }

            Result.success(listas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductosDeLista(familyId: String, listId: String): List<ProductoLista> {
        return try {
            val db = FirebaseFirestore.getInstance()

            val snapshot = db
                .collection("groups")
                .document(familyId)
                .collection("lists")
                .document(listId)
                .collection("items")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val productId = doc.getString("productId") ?: return@mapNotNull null
                val cantidad = doc.getLong("cantidad")?.toInt() ?: 1
                val nota = doc.getString("nota")
                ProductoLista(productId, cantidad, nota)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }


    /**
     * Agrega un producto a una lista específica del grupo
     */
    suspend fun addProductToList(
        listId: String,
        productId: String,
        nota: String,
        cantidad: Int
    ): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Usuario no autenticado"))

        return try {
            val userDoc = db.collection("users").document(user.uid).get().await()
            val familyId = userDoc.getString("familyId") ?: return Result.failure(Exception("Usuario sin grupo"))

            val itemsRef = db.collection("groups").document(familyId)
                .collection("lists").document(listId)
                .collection("items")

            // Verifica si ya existe el producto en la lista
            val existingItemSnapshot = itemsRef
                .whereEqualTo("productId", productId)
                .limit(1)
                .get()
                .await()

            if (!existingItemSnapshot.isEmpty) {
                // Ya existe, no se permite añadirlo de nuevo
                return Result.failure(Exception("Este producto ya está en la lista"))
            }

            // Si no existe, se añade
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
     * Elimina un producto por su ID de una lista específica
     */
    suspend fun removeProductFromList(listId: String, productId: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Usuario no autenticado"))

        return try {
            val userDoc = db.collection("users").document(user.uid).get().await()
            val familyId = userDoc.getString("familyId") ?: return Result.failure(Exception("Usuario sin grupo"))

            val itemsRef = db.collection("groups").document(familyId)
                .collection("lists").document(listId)
                .collection("items")

            val snapshot = itemsRef.whereEqualTo("productId", productId).get().await()
            snapshot.documents.forEach { it.reference.delete().await() }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductosDeListaConDetalles(
        familyId: String,
        listId: String
    ): List<ProductoConDetalles> = coroutineScope {
        val productosLista = getProductosDeLista(familyId, listId)

        productosLista.map { pl ->
            async {
                val producto = MercadonaRepository.getProductById(pl.productId)
                if (producto != null) {
                    ProductoConDetalles(producto = producto, productoLista = pl)
                } else {
                    null // Ignora los que no se encuentren
                }
            }
        }.awaitAll().filterNotNull()
    }

    suspend fun guardarCompra(familyId: String, productos: List<ProductoCompra>, nombreLista: String) {
        val db = FirebaseFirestore.getInstance()

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

        db.collection("groups")
            .document(familyId)
            .collection("historial")
            .add(data)
    }



    suspend fun eliminarProductosDeLista(familyId: String, listId: String, productos: List<ProductoCompra>) {
        val db = FirebaseFirestore.getInstance()

        productos.forEach { productoCompra ->
            val productId = productoCompra.producto.id

            val querySnapshot = db.collection("groups")
                .document(familyId)
                .collection("lists")
                .document(listId)
                .collection("items")
                .whereEqualTo("productId", productId)
                .get()
                .await()

            for (doc in querySnapshot.documents) {
                try {
                    db.collection("groups")
                        .document(familyId)
                        .collection("lists")
                        .document(listId)
                        .collection("items")
                        .document(doc.id)
                        .delete()
                        .await()
                } catch (e: Exception) {
                }
            }
        }
    }

    suspend fun esFavorito(familyId: String, productId: String): Boolean {
        val db = FirebaseFirestore.getInstance()

        val ref = db.collection("groups")
            .document(familyId)
            .collection("favoritos")
            .document(productId)

        return ref.get().await().exists()
    }

    suspend fun agregarAFavoritos(familyId: String, productId: String) {
        val db = FirebaseFirestore.getInstance()

        val ref = db.collection("groups")
            .document(familyId)
            .collection("favoritos")
            .document(productId)

        ref.set(mapOf("id" to productId)).await()
    }

    suspend fun eliminarDeFavoritos(familyId: String, productId: String) {
        val db = FirebaseFirestore.getInstance()

        val ref = db.collection("groups")
            .document(familyId)
            .collection("favoritos")
            .document(productId)

        ref.delete().await()
    }

    suspend fun obtenerNombreLista(familyId: String, listId: String): String? {
        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("groups")
                .document(familyId)
                .collection("lists")
                .document(listId)
                .get()
                .await()

            snapshot.getString("name")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}