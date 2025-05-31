package com.pablo.familycart.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pablo.familycart.models.FamilyData
import com.pablo.familycart.models.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.StateFlow

class FamiliaViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    // Estado para saber si el usuario tiene familia
    private val _hasFamily = MutableStateFlow<Boolean?>(null)
    val hasFamily: StateFlow<Boolean?> = _hasFamily

    // Datos de la familia
    private val _familyData = MutableStateFlow<FamilyData?>(null)
    val familyData: StateFlow<FamilyData?> = _familyData

    // Lista de miembros de la familia
    private val _members = MutableStateFlow<List<UserData>>(emptyList())
    val members: StateFlow<List<UserData>> = _members

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
                val familyId = withContext(Dispatchers.IO) {
                    val snapshot = db.collection("users").document(currentUser.uid).get().await()
                    snapshot.getString("familyId")
                }
                _hasFamily.value = familyId != null
            } catch (e: Exception) {
                _hasFamily.value = false
            }
        }
    }

//    fun loadFamilyDetails() {
//        val currentUser = auth.currentUser ?: return
//
//        viewModelScope.launch {
//            try {
//                // Obtener el ID de la familia del usuario actual
//                val userSnapshot = db.collection("users").document(currentUser.uid).get().await()
//                val familyId = userSnapshot.getString("familyId") ?: return@launch
//
//                // Obtener los datos de la familia
//                val familySnapshot = db.collection("groups").document(familyId).get().await()
//                val family = familySnapshot.toObject(familyData::class.java)
//                _familyData.value = family
//
//                // Obtener todos los miembros de la familia
//                val usersSnapshot = db.collection("users")
//                    .whereEqualTo("familyId", familyId)
//                    .get()
//                    .await()
//
//                val membersList = usersSnapshot.documents.mapNotNull { it.toObject(UserData::class.java) }
//                _members.value = membersList
//
//            } catch (e: Exception) {
//                // Manejo de errores si es necesario
//                _familyData.value = null
//                _members.value = emptyList()
//            }
//        }
//    }

}
