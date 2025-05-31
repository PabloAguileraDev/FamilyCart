package com.pablo.familycart.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pablo.familycart.models.FamilyData
import kotlinx.coroutines.tasks.await
import com.pablo.familycart.models.UserData


class User(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    /**
     * Método para iniciar sesión con el usuario en Firebase Auth
     */
    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
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
            Result.failure(e)
        }
    }

    /**
     * Método para guardar los datos del usuario en Firebase
     */
    fun saveUserData(
        userData: UserData,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ): Result<Unit> {
        val user = auth.currentUser
        return if (user != null) {
            db.collection("users").document(user.uid)
                .update(
                    "nombre", userData.nombre,
                    "apellidos", userData.apellidos
                ).addOnSuccessListener {
                    onSuccess()
                }.addOnFailureListener { e ->
                    onFailure(e.message ?: "Error desconocido")
                }

            Result.success(Unit)
        } else {
            // Usuario no registrado
            return Result.failure(Exception("Usuario no autenticado"))
        }
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

    /**
     * Agrega un producto a una lista específica del grupo
     */
    suspend fun addProductToList(listId: String, productId: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Usuario no autenticado"))

        return try {
            val userDoc = db.collection("users").document(user.uid).get().await()
            val familyId = userDoc.getString("familyId") ?: return Result.failure(Exception("Usuario sin grupo"))

            val productData = mapOf(
                "productId" to productId,
                "addedBy" to user.uid,
                "timestamp" to com.google.firebase.Timestamp.now()
            )

            db.collection("groups").document(familyId)
                .collection("lists").document(listId)
                .collection("items").add(productData).await()

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


}