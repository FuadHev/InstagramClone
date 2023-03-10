package com.example.instagramclone.data.entity

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Posts(
    val post_id:String,
    val postImage: String,
    val description: String,
    val publisher: String,
    val time:Timestamp
):Parcelable