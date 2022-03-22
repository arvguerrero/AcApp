package com.example.acapp_v_2.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User (
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val businessName: String = "",
    val email: String = "",

    val mobile: Long = 0,
    val image: String = ""): Parcelable