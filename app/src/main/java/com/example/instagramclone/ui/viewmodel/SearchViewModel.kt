package com.example.instagramclone.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramclone.model.Posts
import com.example.instagramclone.model.Users
import com.example.instagramclone.utils.Resource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SearchViewModel : ViewModel() {

    val userLiveData = MutableLiveData<List<Users>>()

    val postMutableLiveData = MutableLiveData<Resource<List<Posts>>>()

    val firestore = Firebase.firestore


    fun readPost() {
        firestore.collection("Posts").get()
            .addOnSuccessListener { value ->
                if (value != null) {
                    val postList = ArrayList<Posts>()
                    val hashSet= hashSetOf<Posts>()
                    postMutableLiveData.postValue(Resource.Loading())
                    try {
                        for (document in value.documents) {
                            val publisher = document.get("publisher") as String
                            val post_id = document.get("postId") as String
                            val postImage = document.get("postImage") as String
                            val description = document.get("description") as String
                            val time = document.get("time") as Timestamp
                            val post = Posts(post_id, postImage, description, publisher, time)
                            hashSet.add(post)
                        }
                        postList.addAll(hashSet)
                        postMutableLiveData.postValue(Resource.Success(postList))

                    } catch (exception: java.lang.NullPointerException) {
                        postMutableLiveData.postValue(exception.localizedMessage?.let {
                            Resource.Error(it)
                        })
                    }
                }

            }.addOnFailureListener { exception ->
                postMutableLiveData.postValue(exception.localizedMessage?.let { Resource.Error(it) })
            }
    }

    fun searchUsers(s: String) {

        val ref = firestore.collection("user").orderBy("username").startAt(s).endAt(s + "\uf8ff")

        ref.get().addOnSuccessListener { value ->
            if (value != null) {
                val usersList = ArrayList<Users>()
                for (users in value.documents) {

                    val user_id = users.get("user_id") as String
                    val username = users.get("username") as String
                    val imageurl = users.get("image_url") as String

                    val user = Users(user_id, "", username, "", imageurl, "")
                    usersList.add(user)

                }
                userLiveData.postValue(usersList)
            }

        }.addOnFailureListener {

        }
    }
}