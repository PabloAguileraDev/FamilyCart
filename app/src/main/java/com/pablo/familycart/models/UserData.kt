package com.pablo.familycart.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
// Data class con los datos del usuario
data class userData(
    val uid: String = "",
    val email: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val familyId: String? = null
) : Parcelable

@Parcelize
// Data class con los datos de la familia
data class familyData(
    val code: String = "",
    val password: String = "",
    val ownerId: String = ""
) : Parcelable
