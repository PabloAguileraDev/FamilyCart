package com.pablo.familycart.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pablo.familycart.data.User
import kotlinx.coroutines.launch

class LoginViewModel(auth: FirebaseAuth, db: FirebaseFirestore): ViewModel() {
    private val userRepository = User(auth, db)

    fun login(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            val result = userRepository.login(email, password)
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { onFailure(it.message ?: "Error al iniciar sesión") }
            )
        }
    }
}