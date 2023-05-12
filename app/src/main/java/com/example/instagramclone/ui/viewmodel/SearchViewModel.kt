package com.example.instagramclone.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramclone.model.Users
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SearchViewModel:ViewModel() {

    val userLiveData= MutableLiveData<List<Users>>()


    fun searchUsers(s: String) {
        val ref = Firebase.firestore.collection("user").orderBy("username").startAt(s).endAt(s + "\uf8ff")

        ref.addSnapshotListener { value, error ->
            if (error!=null){
                error.localizedMessage?.let { Log.e("user_Search_error", it) }
                return@addSnapshotListener
            }

            if (value!=null){
                val usersList=ArrayList<Users>()
                for (users in value!!.documents) {

                    val user_id = users.get("user_id") as String
                    val username = users.get("username") as String
                    val imageurl = users.get("image_url") as String

                    val user = Users(user_id, "", username, "", imageurl, "")
                    usersList.add(user)

                }
                userLiveData.postValue(usersList)
            }

        }


    }
}