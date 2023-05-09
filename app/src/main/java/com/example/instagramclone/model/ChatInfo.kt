package com.example.instagramclone.model

import com.google.firebase.Timestamp

data class ChatInfo(
    val time: Timestamp,
    val seen: Boolean,
    val lastMessage: String,
    val senderId: String
)
