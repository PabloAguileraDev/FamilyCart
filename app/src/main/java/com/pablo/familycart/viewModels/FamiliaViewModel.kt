package com.pablo.familycart.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pablo.familycart.models.FamilyData
import com.pablo.familycart.models.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.StateFlow

class FamiliaViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _hasFamily = MutableStateFlow<Boolean?>(null)
    val hasFamily: StateFlow<Boolean?> = _hasFamily

    private val _familyData = MutableStateFlow<FamilyData?>(null)
    val familyData: StateFlow<FamilyData?> = _familyData

    private val _miembros = MutableStateFlow<List<UserData>>(emptyList())
    val miembros: StateFlow<List<UserData>> = _miembros

    private val _ownerNombre = MutableStateFlow<String?>(null)
    val ownerNombre: StateFlow<String?> = _ownerNombre


    init {
        checkFamilyStatus()
    }

    fun checkFamilyStatus() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _hasFamily.value = false
            return
        }

        viewModelScope.launch {
            try {
                // 1. Obtener ID de familia del usuario
                val snapshot = db.collection("users").document(currentUser.uid).get().await()
                val familyId = snapshot.getString("familyId")

                if (familyId != null) {
                    // 2. Solo si tiene familia, actualizar estado y cargar datos
                    _hasFamily.value = true
                    cargarDatosFamilia(familyId)
                } else {
                    _hasFamily.value = false
                }
            } catch (e: Exception) {
                Log.e("FamiliaViewModel", "Error al verificar familia", e)
                _hasFamily.value = false
            }
        }
    }


    private suspend fun cargarDatosFamilia(familyId: String) {
        try {
            val groupDoc = db.collection("groups").document(familyId).get().await()
            val group = groupDoc.toObject(FamilyData::class.java) ?: return
            _familyData.value = group

            val miembrosSnapshot = db.collection("users")
                .whereEqualTo("familyId", familyId)
                .get()
                .await()
            _miembros.value = miembrosSnapshot.toObjects(UserData::class.java)

            val ownerDoc = db.collection("users").document(group.ownerId).get().await()
            _ownerNombre.value = ownerDoc.getString("nombre")
        } catch (e: Exception) {
            Log.e("FamiliaViewModel", "Error cargando datos de la familia", e)
        }
    }


}
