package com.example.instagramclone.data.entity

import com.google.firebase.Timestamp

data class ChatInfo(
    val time: Timestamp,
    val seen: Boolean,
    val lastMessage: String,
    val senderId: String
)
