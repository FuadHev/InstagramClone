package com.example.instagramclone.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramclone.model.Users
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FollowerViewModel : ViewModel() {
    val userList = MutableLiveData<ArrayList<Users>>()
    val firestore = Firebase.firestore

    fun getUsers(idList: ArrayList<String>) {
        firestore.collection("user").get().addOnSuccessListener { value ->
                if (value != null) {
                    if (!value.isEmpty) {
                        val usersList = ArrayList<Users>()
                        for (user in value.documents) {
                            val user_id = user.get("user_id") as String
                            for (id in idList) {
                                if (user_id == id) {
                                    val username = user.get("username") as String
                                    val imageurl = user.get("image_url") as String
                                    val follower =
                                        Users(user_id, "", username, "", imageurl, "")
                                    usersList.add(follower)
                                }
                            }


                        }
                        userList.postValue(usersList)

                    }

                }

        }.addOnFailureListener {
            it.localizedMessage?.let { it1 -> Log.e("UserError", it1) }
        }


    }


    fun getFollower(id: String) {

        firestore.collection("Follow").document(id).get().addOnSuccessListener { value ->


            val idList = ArrayList<String>()
            try {
                if (value != null) {
                    val data = value.get("followers") as HashMap<*, *>
                    for (follower in data) {
                        idList.add(follower.key.toString())
                    }
                    getUsers(idList)
                }
            } catch (e: java.lang.NullPointerException) {

                e.localizedMessage?.let { Log.e("follow_Collection_Error", it) }
            }


        }
    }


}