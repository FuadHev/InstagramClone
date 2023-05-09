package com.example.instagramclone.model

data class Story(
    val imageurl: String,
    val timestart: Long,
    val timeend: Long,
    val storyId: String,
    val userId: String
){

    constructor(imageurl:String,timestart:Long,storyId: String):this(imageurl,timestart,0,storyId,"")
}
