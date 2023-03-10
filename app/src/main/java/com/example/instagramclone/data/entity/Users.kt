package com.example.instagramclone.data.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Users(
    val user_id: String,
    val email: String,
    val username: String,
    val password: String,
    val imageurl:String,
    val bio:String
):Parcelable