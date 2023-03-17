package com.example.instagramclone.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramclone.data.entity.Users
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FollowingViewModel: ViewModel() {
    val userList= MutableLiveData<ArrayList<Users>>()
    val firestore = Firebase.firestore

    private fun getUsers(idList:ArrayList<String>) {
        firestore.collection("user").addSnapshotListener { value, error ->
            if (error != null) {

            } else {
                if (value != null) {
                    if (!value.isEmpty) {
                        val usersList=ArrayList<Users>()
                        for (user in value.documents) {
                            val user_id = user.get("user_id") as String
                            val email = user.get("email") as String
                            val username = user.get("username") as String
                            val password = user.get("password") as String
                            val imageurl = user.get("image_url") as String
                            val bio = user.get("bio") as String
                            val user = Users(user_id, email, username, password, imageurl, bio)
                            for (id in idList) {
                                if (user_id == id) {
                                    usersList.add(user)
                                }
                            }
                        }
                        userList.value=usersList


                    }

                }
            }
        }


    }

   fun getFollowings(id:String){

        firestore.collection("Follow").document(id).addSnapshotListener { value, error ->
            if (error != null) {

            } else {
                try {
                    if (value != null) {
                        val idList=ArrayList<String>()
                        val data = value.get("following") as HashMap<*, *>

                        for (following in data) {
                            idList.add(following.key.toString())
                        }
                        getUsers(idList)
                    }
                } catch (e: java.lang.NullPointerException) {

                }

            }


        }




    }


}