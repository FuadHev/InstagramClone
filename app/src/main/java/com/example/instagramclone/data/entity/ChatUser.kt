package com.example.instagramclone.data.entity

import com.google.firebase.Timestamp

data class ChatUser(val receiverId:String,val username:String,val imageUrl:String,val time:Timestamp,val seen:Boolean):java.io.Serializable {
}