package com.example.instagramclone.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagramclone.data.entity.Posts
import com.example.instagramclone.data.entity.Story
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {


    val checkFollowLiveData = MutableLiveData<Boolean>(false)
    val followList = ArrayList<String>()
    val postList = ArrayList<Posts>()
    val storyList = ArrayList<Story>()
    val firestore = Firebase.firestore
    val checkMessageLiveData = MutableLiveData(false)
    val isLoading = MutableLiveData(false)


    init {
        checkFollowing()
        checkMessage()
    }


    private fun checkMessage() {
        Firebase.firestore.collection("Chats").addSnapshotListener { value, error ->

            if (error != null) {
                error.localizedMessage?.let { Log.e("Chat_Error", it) }
                return@addSnapshotListener
            }
            if (value != null && !value.isEmpty) {

                for (doc in value.documents) {

                    val senderId = doc.get("senderId") as? String
                    if (senderId != Firebase.auth.currentUser!!.uid && Firebase.auth.currentUser!!.uid + senderId == doc.id) {
                        val seen = doc.get("seen") as Boolean
                        if (senderId != null && seen) {
                            checkMessageLiveData.postValue(seen)
                        } else {
                            checkMessageLiveData.postValue(seen)
                        }
                    }

                }
            }
        }


    }


    fun checkFollowing() {

        firestore.collection("Follow").document(Firebase.auth.currentUser!!.uid)
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


                                if (followList.isNotEmpty()){
                                    checkFollowLiveData.postValue(true)

                                }else{
                                    checkFollowLiveData.postValue(false)
                                }
                                viewModelScope.launch {
                                    readStory()
                                }
                                viewModelScope.launch {
                                    readPost()
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
                    postList.clear()
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
                            if (postList.isNotEmpty()) {
                                isLoading.postValue(true)
                            } else {
                                isLoading.postValue(false)
                            }


                        } catch (_: java.lang.NullPointerException) {


                        }


                    }

                }


            }

        }

    }

    fun readStory() {

        storyList.clear()
        storyList.add(Story("", 0, 0, "", Firebase.auth.currentUser!!.uid))
        for (id in followList) {
            var countStory = 0
            firestore.collection("Story").document(id).addSnapshotListener { value, error ->
                if (error != null) {
                } else {
                    if (value != null && value.exists()) {
                        val doc = value.data as HashMap<*, *>
                        try {

                            var ustory: Story? = null
                            val timecurrent = System.currentTimeMillis()
                            for (i in doc) {
                                val story = i.value as HashMap<*, *>
                                val timestart = story["timeStart"] as Long
                                val timeend = story["timeEnd"] as Long
                                val imageurl = story["imageurl"] as String
                                val storyId = story["storyId"] as String
                                val userId = story["userId"] as String
                                if (timecurrent in (timestart + 1) until timeend) {
                                    ++countStory
                                    break
                                }
                                ustory = Story(imageurl, timestart, timeend, storyId, userId)

                            }
                            if (countStory > 0) {
                                if (ustory != null) {
                                    storyList.add(ustory)
                                }
                            }


                        } catch (e: java.lang.NullPointerException) {


                        }


                    }

                }

            }
        }


//        storyList.add(Story("", 0, 0, "", Firebase.auth.currentUser!!.uid))
//
//        val ref = firestore.collection("Story")
//        storyList.clear()
//
//        for (id in followList) {
//            var countStory = 0
//            ref.document(id).addSnapshotListener { value, error ->
//                if (error != null) {
//
//
//                } else {
//                    if (value != null && value.exists()) {
//                        val doc = value.data as HashMap<*, *>
//                        try {
//
//                            var ustory: Story? = null
//                            val timecurrent = System.currentTimeMillis()
//                            for (i in doc) {
//                                val story = i.value as HashMap<*, *>
//                                val timestart = story["timeStart"] as Long
//                                val timeend = story["timeEnd"] as Long
//                                val imageurl = story["imageurl"] as String
//                                val storyId = story["storyId"] as String
//                                val userId = story["userId"] as String
//                                if (timecurrent > timestart && timecurrent < timeend) {
//                                    ++countStory
//                                    break
//                                }
//                                ustory = Story(imageurl, timestart, timeend, storyId, userId)
//
//                            }
//                            if (countStory > 0) {
//                                if (ustory != null) {
//                                    storyList.add(ustory)
//                                }
//                            }
//
//
//                            storiesList.value = storyList
//
//
//                        } catch (e: java.lang.NullPointerException) {
//
//
//                        }
//
//
//                    }
//
//                }
//
//            }
//        }
//
    }

}