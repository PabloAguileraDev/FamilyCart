package com.pablo.familycart.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FamiliaViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _hasFamily = MutableStateFlow<Boolean?>(null)
    val hasFamily: StateFlow<Boolean?> = _hasFamily

    fun checkFamilyStatus() {
        val currentUser = auth.currentUser ?: run {
            _hasFamily.value = false
            return
        }

        viewModelScope.launch {
            try {
                val familyId = withContext(Dispatchers.IO) {
                    val snapshot = db.collection("users").document(currentUser.uid).get().await()
                    snapshot.getString("familyId")
                }
                _hasFamily.value = familyId != null
            } catch (e: Exception) {
                // En caso de error, asumimos que no tiene familia (o mostrar error si prefieres)
                _hasFamily.value = false
            }
        }
    }
}
