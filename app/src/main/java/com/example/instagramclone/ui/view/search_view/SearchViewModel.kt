package com.example.instagramclone.ui.view.search_view

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramclone.model.Posts
import com.example.instagramclone.model.Users
import com.example.instagramclone.utils.Resource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SearchViewModel : ViewModel() {

    val userLiveData = MutableLiveData<List<Users>>()

    val postMutableLiveData = MutableLiveData<Resource<List<Posts>>>()

    val firestore = Firebase.firestore

    init {
        readPost()
    }

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
                            val postid = document.get("postId") as String
                            val postImage = document.get("postImage") as String
                            val description = document.get("description") as String
                            val time = document.get("time") as Timestamp
                            val post = Posts(postid, postImage, description, publisher, time)
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

                    val userid = users.get("user_id") as String
                    val username = users.get("username") as String
                    val imageurl = users.get("image_url") as String
                    val user = Users(userid, "", username, "", imageurl, "")
                    usersList.add(user)

                }
                userLiveData.postValue(usersList)
            }

        }.addOnFailureListener {
            Log.e("Search_User_Error",it.localizedMessage!!)
        }
    }
}