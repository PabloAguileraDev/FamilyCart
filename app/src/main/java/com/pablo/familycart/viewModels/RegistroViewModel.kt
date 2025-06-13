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

/**
 * ViewModel para la pantalla de registro de usuario.
 * Maneja los estados de los campos de entrada, validación y la lógica para registrar un usuario.
 *
 * @param auth instancia de FirebaseAuth para autenticación.
 * @param db instancia de Firebase para base de datos.
 */
class RegistroViewModel(auth: FirebaseAuth, db: FirebaseFirestore) : ViewModel() {

    var nombre by mutableStateOf("")
    var apellidos by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    var nombreError by mutableStateOf(false)
    var apellidosError by mutableStateOf(false)
    var emailError by mutableStateOf(false)
    var passwordError by mutableStateOf(false)
    var confirmPasswordError by mutableStateOf(false)

    private val userRepository = User(auth, db)

    /**
     * Registra al usuario en la base de datos.
     *
     * Si la validación de los campos es correcta, intenta registrar al usuario.
     */
    fun registerUser(onSuccess: () -> Unit, onFailure: (String) -> Unit) {

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
                onFailure = { onFailure(it.message ?: "Error en el registro") }
            )
        }
    }

    /**
     * Valida todos los campos del formulario y actualiza los estados de error.
     */
    private fun validarCampos(
        nombre: String,
        apellidos: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Pair<Boolean, String> {

        nombreError = nombre.isBlank()
        apellidosError = apellidos.isBlank()
        emailError = !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        passwordError = password.length < 8 ||
                !password.matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}\$"))
        confirmPasswordError = password != confirmPassword

        return when {
            nombreError -> Pair(false, "El nombre no puede estar vacío")
            apellidosError -> Pair(false, "Los apellidos no pueden estar vacíos")
            emailError -> Pair(false, "El email no es válido")
            passwordError -> Pair(false, "La contraseña debe tener como mínimo 8 caracteres y contener al menos una mayúscula, una minúscula, un número y un carácter especial")
            confirmPasswordError -> Pair(false, "Las contraseñas no coinciden")
            else -> Pair(true, "")
        }
    }

    fun validarNombre() {
        nombreError = nombre.isBlank()
    }

    fun validarApellidos() {
        apellidosError = apellidos.isBlank()
    }

    fun validarEmail() {
        emailError = !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validarPassword() {
        passwordError = !password.matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}\$"))
    }

    fun validarConfirmPassword() {
        confirmPasswordError = confirmPassword != password
    }
}
