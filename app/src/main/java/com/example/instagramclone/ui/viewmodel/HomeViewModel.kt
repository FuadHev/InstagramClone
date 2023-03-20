package com.example.instagramclone.ui.viewmodel

import android.app.ProgressDialog
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramclone.data.entity.Posts
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeViewModel:ViewModel() {

    val postsList=MutableLiveData<ArrayList<Posts>>()

    val followList=ArrayList<String>()

    val firestore=Firebase.firestore
    lateinit var auth:FirebaseUser




    fun checkFollowing() {

        firestore.collection("Follow").document(auth.uid)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    error.localizedMessage?.let {
                        Log.e("", it)
                        return@addSnapshotListener
                    }
                } else {


                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val follow = documentSnapshot.data

                        if (follow != null) {
                            try {

                                val following = follow["following"] as HashMap<*, *>
                                followList.clear()
                                for (i in following) {
                                    followList.add(i.key as String)
                                }

                            } catch (e: java.lang.NullPointerException) {

                                e.printStackTrace()

                            }
                        }

                    } else {
                        Log.e("", "")
                    }
                }
            }


    }




    fun readPost() {


        firestore.collection("Posts").addSnapshotListener { value, error ->
            if (error != null) {
                error.localizedMessage?.let { Log.e("", it) }
            } else {

                if (value != null) {
                    val postList=ArrayList<Posts>()
                    for (document in value.documents) {
                        try {
                            val post_id = document.get("postId") as String
                            val postImage = document.get("postImage") as String
                            val description = document.get("description") as String
                            val publisher = document.get("publisher") as String
                            val time = document.get("time") as Timestamp
                            val post = Posts(post_id, postImage, description, publisher, time)


                            for (id in followList) {
                                if (publisher == id) {
                                    postList.add(post)
                                }
                            }

                            postList.sortByDescending {
                                it.time
                            }

                            postsList.value=postList

                        } catch (_: java.lang.NullPointerException) {


                        }


                    }

                }


            }

        }

    }

}