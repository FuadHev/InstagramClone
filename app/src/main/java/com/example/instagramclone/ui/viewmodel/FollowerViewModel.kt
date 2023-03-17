package com.example.instagramclone.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramclone.data.entity.Users
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FollowerViewModel : ViewModel() {
    val userList = MutableLiveData<ArrayList<Users>>()
    val firestore = Firebase.firestore


    fun getUsers(idList: ArrayList<String>) {
        firestore.collection("user").addSnapshotListener { value, error ->
            if (error != null) {

            } else {
                if (value != null) {
                    if (!value.isEmpty) {
                        val usersList = ArrayList<Users>()
                        for (user in value.documents) {
                            val user_id = user.get("user_id") as String
                            for (id in idList) {
                                if (user_id == id) {
                                    val email = user.get("email") as String
                                    val username = user.get("username") as String
                                    val password = user.get("password") as String
                                    val imageurl = user.get("image_url") as String
                                    val bio = user.get("bio") as String
                                    val follower = Users(user_id, email, username, password, imageurl, bio)
                                    usersList.add(follower)
                                }
                            }


                        }
                        userList.value = usersList

                    }

                }
            }
        }


    }


    fun getFollower(id: String) {


        firestore.collection("Follow").document(id).addSnapshotListener { value, error ->

            val idList = ArrayList<String>()
            if (error != null) {

            } else {
                try {
                    if (value != null) {
                        val data = value.get("followers") as HashMap<*, *>
                        for (follower in data) {
                            idList.add(follower.key.toString())
                        }
                        getUsers(idList)
                    }
                } catch (e: java.lang.NullPointerException) {

                }

            }


        }
    }


}