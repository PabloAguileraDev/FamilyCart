package com.pablo.familycart.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pablo.familycart.models.familyData
import kotlinx.coroutines.tasks.await
import com.pablo.familycart.models.userData


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

            // Usuario a guardar en firebase
            val userData = userData(
                uid = user.uid,
                nombre = nombre,
                apellidos = apellidos,
                email = email
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
        userData: userData,
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
    suspend fun createGroup(familyCode: String, password: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Usuario no autenticado"))

        return try {
            val group = familyData(
                code = familyCode,
                ownerId = user.uid
            )

            // Crear grupo
            val groupRef = db.collection("groups").document()
            groupRef.set(group).await()

            // Asignar grupo al usuario
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

    /**
     * Agrega un número entero a la lista compartida del grupo
     */
    suspend fun addNumberToGroupList(productId: Int, quantity: Int, note: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Usuario no autenticado"))

        return try {
            // Obtener groupId del usuario
            val userDoc = db.collection("users").document(user.uid).get().await()
            val groupId = userDoc.getString("familyId") ?: return Result.failure(Exception("El usuario no pertenece a un grupo"))

            val numberData = mapOf(
                "productId" to productId,
                "quantity" to quantity,
                "note" to note,
                "addedBy" to user.uid,
                "timestamp" to com.google.firebase.Timestamp.now()
            )

            // Agregar número a la subcolección del grupo
            db.collection("groups").document(groupId)
                .collection("list").add(numberData).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}