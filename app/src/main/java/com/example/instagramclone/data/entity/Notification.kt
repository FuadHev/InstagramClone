package com.example.instagramclone.data.entity

import com.google.firebase.Timestamp


class Notification(val userId:String,
                   val ntext:String,
                   val postId:String,
                   val isPost: Boolean,
                   val time:Timestamp)