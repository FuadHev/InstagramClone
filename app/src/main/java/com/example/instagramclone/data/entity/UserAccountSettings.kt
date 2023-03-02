package com.example.instagramclone.data.entity

data class UserAccountSettings(
    val description: String,
    var display_name: String,
    val followers: Int,
    val following: Int,
    val posts: Int,
    val profil_photo: String,
    val user_name: String
) {
}