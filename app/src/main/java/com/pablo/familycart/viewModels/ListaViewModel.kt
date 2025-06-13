package com.pablo.familycart.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pablo.familycart.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel para manejar la lógica de listas compartidas por familias.
 */
class ListaViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _listas = MutableStateFlow<List<Pair<String, Map<String, Any>>>>(emptyList())
    val listas: StateFlow<List<Pair<String, Map<String, Any>>>> = _listas

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _familyId = MutableStateFlow<String?>(null)
    val familyId: StateFlow<String?> = _familyId

    init {
        loadFamilyLists()
    }

    /**
     * Carga las listas de la familia.
     */
    fun loadFamilyLists() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val user = auth.currentUser ?: return@launch
                val userSnapshot = db.collection("users").document(user.uid).get().await()
                val familyId = userSnapshot.getString("familyId") ?: return@launch

                _familyId.value = familyId

                val listsSnapshot = db.collection("groups").document(familyId)
                    .collection("lists").get().await()

                val lists = listsSnapshot.documents.map {
                    it.id to it.data.orEmpty()
                }

                _listas.value = lists
            } catch (e: Exception) {
                _listas.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Crea una nueva lista.
     */
    fun createList(listName: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val result = User(auth, db).createList(listName)
            if (result.isSuccess) {
                loadFamilyLists()
                onComplete()
            }
        }
    }

    /**
     * Elimina una lista específica
     */
    fun deleteList(listId: String, onComplete: () -> Unit) {
        familyId.value?.let { family ->
            db.collection("groups")
                .document(family)
                .collection("lists")
                .document(listId)
                .delete()
                .addOnSuccessListener {
                    loadFamilyLists()
                    onComplete()
                }
                .addOnFailureListener { e ->
                    Log.e("ListaViewModel", "Error al eliminar la lista: ${e.message}")
                }
        }
    }
}
