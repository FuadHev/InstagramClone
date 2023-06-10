package com.example.instagramclone.ui.view.edit_profie

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramclone.model.Users
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

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