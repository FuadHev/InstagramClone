package com.example.instagramclone.model


data class Users(
    val user_id: String,
    val email: String,
    val username: String,
    val password: String,
    val imageurl:String,
    val bio:String
):java.io.Serializable{

    constructor(username: String, imageurl: String) : this(user_id="",email="",username=username,password="",imageurl=imageurl,bio="")
    constructor(username: String, imageurl: String,bio: String) : this(user_id="",email="",username=username,password="",imageurl=imageurl,bio=bio)
}
