package com.pablo.familycart.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pablo.familycart.models.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PerfilViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                val snapshot = db.collection("users").document(currentUser.uid).get().await()
                _userData.value = snapshot.toObject(UserData::class.java)
            } catch (e: Exception) {
                // Manejar error si quieres
            }
        }
    }

    fun updateUserProfile(nombre: String, apellidos: String, foto: String) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                db.collection("users").document(uid).update(
                    mapOf(
                        "nombre" to nombre,
                        "apellidos" to apellidos,
                        "foto" to foto
                    )
                ).await()
                loadUserData() // Recarga los datos
            } catch (e: Exception) {
                // Manejo de errores si lo deseas
            }
        }
    }


}
