package com.example.instagramclone.ui.viewmodel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.instagramclone.model.Posts
import com.example.instagramclone.model.Users
import com.example.instagramclone.utils.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    val postsList = MutableLiveData<Resource<List<Posts>>>()
    val savesList = MutableLiveData<ArrayList<Posts>>()
    val userInfo = MutableLiveData<Users>()
    val followCount = MutableLiveData(0)
    val followerCount = MutableLiveData(0)
    val postCount = MutableLiveData(0)
    val checkFollowLiveData = MutableLiveData<String>()

    fun getFollower(profileid: String) {

        Firebase.firestore.collection("Follow").document(profileid)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    error.localizedMessage?.let {
                        Log.e("get_Follow_error", it)
                        return@addSnapshotListener
                    }
                } else {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val follow = documentSnapshot.data

                        if (follow != null) {

                            try {
                                val followers = follow["followers"] as? HashMap<*, *>
                                if (followers != null) {
                                    followerCount.postValue(followers.keys.count())
                                }
                                val following = follow["following"] as? HashMap<*, *>
                                if (following != null) {
                                    followCount.postValue(following.keys.count())
                                }
                            } catch (e: java.lang.NullPointerException) {

                                e.localizedMessage?.let { Log.e("checkFollow_error", it) }
                            }
                        }
                    }
                }
            }
    }

    fun getNrPost(profileid: String) {
        Firebase.firestore.collection("Posts").addSnapshotListener { value, error ->
            if (error != null) {
                error.localizedMessage?.let {
                    Log.e("", it)
                    return@addSnapshotListener
                }
            } else {
                if (value != null) {
                    var i = 0
                    for (doc in value.documents) {
                        if (profileid == doc.get("publisher").toString()) {
                            i++
                        }
                    }

                    postCount.postValue(i)
                }
            }

        }


    }


    fun mySaves() {

        val mySaves = ArrayList<String>()
        Firebase.firestore.collection("Saves").document(Firebase.auth.currentUser!!.uid)
            .addSnapshotListener { value, error ->
                if (error != null) {

                    error.localizedMessage?.let { Log.e("Saves_Error", it) }
                } else {
                    try {
                        if (value != null) {
                            val data = value.data as HashMap<*, *>
                            for (savekeys in data) {
                                mySaves.add(savekeys.key as String)
                            }
                            readSaves(mySaves)

                        }
                    } catch (e: Exception) {
                        e.localizedMessage?.let { Log.e("Saves_Error", it) }
                    }

                }


            }


    }

    private fun readSaves(mySaves: ArrayList<String>) {

        Firebase.firestore.collection("Posts").addSnapshotListener { value, error ->
            if (error != null) {
                error.localizedMessage?.let { Log.e("posts", it) }
            } else {
                if (value != null) {
                    val savepostList = ArrayList<Posts>()
                    for (document in value.documents) {
                        try {

                            val postid = document.get("postId") as String
                            if (mySaves.contains(postid)) {
                                val postImage = document.get("postImage") as String
                                val description = document.get("description") as String
                                val publisher = document.get("publisher") as String
                                val time = document.get("time") as Timestamp
                                val post = Posts(postid, postImage, description, publisher, time)
                                savepostList.add(post)
                            }

                        } catch (e: Exception) {

                            Log.e("saves_error", e.localizedMessage!!)
                        }
                    }
                    savesList.postValue(savepostList)
                }
            }
        }


    }


    fun userInfo(profileId: String) {
        Firebase.firestore.collection("user").document(profileId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(
                        (getApplication() as Application).applicationContext,
                        error.localizedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    if (value != null && value.exists()) {

                        val username = value.get("username") as String
                        val imageurl = value.get("image_url") as String
                        val bio = value.get("bio") as String

                        val user = Users(profileId, "", username, "", imageurl, bio)
                        userInfo.postValue(user)

                    }


                }

            }


    }


    fun myFotos(profileid: String) {

        Firebase.firestore.collection("Posts").addSnapshotListener { value, error ->
            if (error != null) {
                error.localizedMessage?.let { postsList.postValue(Resource.Error(it)) }
                return@addSnapshotListener
            }
            if (value != null) {
                postsList.postValue(Resource.Loading())
                val postList = ArrayList<Posts>()
                for (document in value.documents) {
                    try {
                        val publisher = document.get("publisher") as String
                        if (publisher == profileid) {
                            val postid = document.get("postId") as String
                            val postImage = document.get("postImage") as String
                            val description = document.get("description") as String
                            val time = document.get("time") as Timestamp
                            val post = Posts(postid, postImage, description, publisher, time)
                            postList.add(post)
                        }
                    } catch (e: java.lang.NullPointerException) {
                        e.localizedMessage?.let { postsList.postValue(Resource.Error(it)) }
                    }

                }
                postList.sortByDescending {
                    it.time
                }
                postsList.postValue(Resource.Success(postList))

            }


        }

    }


    fun checkFollow(profileid: String) {

        Firebase.firestore.collection("Follow").document(Firebase.auth.currentUser!!.uid)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    error.localizedMessage?.let {
                        Log.e("CheckFollow_Error", it)
                        return@addSnapshotListener
                    }
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val follow = documentSnapshot.data

                    if (follow != null) {

                        try {
                            val following = follow["following"] as HashMap<*, *>
                            if (following.containsKey(profileid)) {
                                checkFollowLiveData.postValue("following")
                            } else {
                                checkFollowLiveData.postValue("follow")
                            }
                        } catch (_: java.lang.NullPointerException) {

                        }

                    }
                }

            }


    }

}