package com.example.instagramclone.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramclone.data.entity.Users
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class EditProfileViewModel:ViewModel() {
    val userInfo=MutableLiveData<Users>()

    init {
        getUserInfo()
    }
    fun getUserInfo(){
        Firebase.firestore.collection("user").document(Firebase.auth.currentUser!!.uid).addSnapshotListener { value, error ->
            if (error!=null){

            }else{
                val bio=value?.get("bio") as String
                val username= value.get("username") as String
                val image_url=value.get("image_url") as String

                userInfo.postValue(Users(username,image_url,bio))
            }
        }
    }
}