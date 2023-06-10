package com.example.instagramclone.ui.view.followers_view

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramclone.model.Users
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.math.log

class FollowingViewModel : ViewModel() {
    val userList = MutableLiveData<List<Users>>(emptyList())
    val firestore = Firebase.firestore

    private fun getUsers(idList: ArrayList<String>) {
        firestore.collection("user").get().addOnSuccessListener { value ->
            if (value != null) {
                if (!value.isEmpty) {
                    val usersList = ArrayList<Users>()
                    for (user in value.documents) {
                        val userid = user.get("user_id") as String
                        if (idList.contains(userid)) {
                            val email = user.get("email") as String
                            val username = user.get("username") as String
                            val password = user.get("password") as String
                            val imageurl = user.get("image_url") as String
                            val bio = user.get("bio") as String
                            val userinfo = Users(userid, email, username, password, imageurl, bio)
                            usersList.add(userinfo)
                        }

                    }
                    userList.postValue(usersList)


                }

            }

        }.addOnFailureListener {
            Log.e("user_error", it.localizedMessage!!)
        }


    }

    fun getFollowings(id: String) {

        firestore.collection("Follow").document(id).get().addOnSuccessListener { value ->
            try {
                if (value != null) {
                    val idList = ArrayList<String>()
                    val data = value.get("following") as? HashMap<*, *>

                    if (data != null) {
                        for (following in data) {
                            idList.add(following.key.toString())
                        }
                        getUsers(idList)
                    }


                }
            } catch (e: java.lang.NullPointerException) {

                Log.e("following_error", e.localizedMessage!!)
            }


        }


    }


}