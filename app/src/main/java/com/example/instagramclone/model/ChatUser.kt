package com.example.instagramclone.model

import com.google.firebase.Timestamp

data class ChatUser(
    val receiverId: String,
    val username: String,
    val imageUrl: String,
    val lastMessage:String,
    val time: Timestamp,
    val seen: Boolean
) : java.io.Serializable {
}