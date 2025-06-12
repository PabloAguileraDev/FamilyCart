package com.pablo.familycart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.pablo.familycart.navigation.AppNavigation
import com.pablo.familycart.ui.theme.FamilyCartTheme

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        setContent {
            FamilyCartTheme (darkTheme = false) {
                AppNavigation(auth, db)
            }
        }
    }
}
