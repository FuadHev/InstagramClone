package com.example.instagramclone.model

import com.google.firebase.Timestamp

data class Comment(val comment:String,val publiser:String,val postId:String,val commentId:String,val time:Timestamp)