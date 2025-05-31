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

class UserViewModel : ViewModel() {
    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData

    init {
        viewModelScope.launch {
            val auth = FirebaseAuth.getInstance()
            val db = FirebaseFirestore.getInstance()
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val snapshot = db.collection("users").document(currentUser.uid).get().await()
                val data = snapshot.toObject(UserData::class.java)
                _userData.value = data
            }
        }
    }
}
