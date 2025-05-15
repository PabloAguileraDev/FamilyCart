package com.pablo.familycart.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pablo.familycart.data.User
import kotlinx.coroutines.launch


class RegistroViewModel(auth: FirebaseAuth, db: FirebaseFirestore): ViewModel() {

    var nombre by mutableStateOf("")
    var apellidos by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    private val userRepository = User(auth, db)

    /**
     * Método que registra al usuario en base de datos
     */
    fun registerUser(onSuccess: () -> Unit, onFailure: (String) -> Unit) {

        // Valida los datos
        val (isValid, errorMessage) = validarCampos(
            nombre = nombre,
            apellidos = apellidos,
            email = email,
            password = password,
            confirmPassword = confirmPassword
        )

        if (!isValid) {
            onFailure(errorMessage)
            return
        }

        viewModelScope.launch {
            val result = userRepository.register(
                email = email,
                nombre = nombre,
                apellidos = apellidos,
                password = password
            )

            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { onFailure(it.message ?: "Error en el registro")}
            )
        }
    }

    /**
     * Método que comprueba que los datos sean correctos
     */
    private fun validarCampos(
        nombre: String,
        apellidos: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Pair<Boolean, String>  {
        return when {
            nombre.isEmpty() -> Pair(false, "El nombre no puede estar vacío")
            apellidos.isEmpty() -> Pair(false, "Los apellidos no pueden estar vacíos")
            email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                .matches() -> Pair(false,"Ingresa un email válido.")
            password.length < 8 -> Pair(false, "La contraseña debe tener al menos 8 caracteres.")
            password != confirmPassword -> Pair(false, "Las contraseñas no coinciden.")
            else -> Pair(true, "")
        }
    }
}