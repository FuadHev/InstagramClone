package com.example.instagramclone.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagramclone.model.Posts
import com.example.instagramclone.model.Story
import com.example.instagramclone.utils.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {


    val postMutableLiveData = MutableLiveData<Resource<List<Posts>>>()
    val storyMutableLiveData = MutableLiveData<Resource<List<Story>>>()
    val followList =
        ArrayList<String>()// belke funksiyanin icine yazib burdan silesi oldum.(checkFollow funksiyasi)

    val firestore = Firebase.firestore
    val checkMessageLiveData = MutableLiveData(true)


    init {
        checkFollowing()
        checkMessage()
    }

    private fun checkMessage() {
        firestore.collection("Chats").addSnapshotListener { value, error ->

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
                        return@addSnapshotListener
                    }
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val follow = documentSnapshot.data

                    if (follow != null) {
                        try {

                            val following = follow["following"] as HashMap<*, *>
                            followList.clear()
                            for (i in following) {
                                followList.add(i.key as String)
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
                }
            }


    }


    private fun readPost() {

        firestore.collection("Posts").get()
            .addOnSuccessListener { value ->
                if (value != null) {
                    val postList = ArrayList<Posts>()
                    postMutableLiveData.postValue(Resource.Loading())
                    for (document in value.documents) {
                        try {
                            val post_id = document.get("postId") as String
                            val postImage = document.get("postImage") as String
                            val description = document.get("description") as String
                            val publisher = document.get("publisher") as String
                            val time = document.get("time") as Timestamp
                            val post = Posts(post_id, postImage, description, publisher, time)

                            if (followList.contains(publisher)) {
                                postList.add(post)
                            }
                            postList.sortByDescending {
                                it.time
                            }

                            postMutableLiveData.postValue(Resource.Success(postList))

                        } catch (exception: java.lang.NullPointerException) {
                            postMutableLiveData.postValue(exception.localizedMessage?.let { Resource.Error(it)
                            })

                        }
                    }
                }

            }.addOnFailureListener { exception ->
                postMutableLiveData.postValue(exception.localizedMessage?.let { Resource.Error(it) })
            }
    }

    private fun readStory() {
        firestore.collection("Story").get().addOnSuccessListener { value ->

            if (value != null) {
                storyMutableLiveData.postValue(Resource.Loading())
                val storyList = ArrayList<Story>()
                var ustory: Story? = null
                val timecurrent = System.currentTimeMillis()
                for (document in value.documents) {
                    var countStory = 0
                    if (followList.contains(document.id)) {
                        val stories = document.data as HashMap<*, *>

                        for (storyIds in stories) {
                            val story = storyIds.value as? HashMap<*, *>
                            if (story != null) {
                                val storyId = story["storyId"] as String
                                val timestart = story["timeStart"] as Long
                                val timeend = story["timeEnd"] as Long
                                val imageurl = story["imageurl"] as String
                                val userId = story["userId"] as String
                                if (timecurrent in (timestart + 1) until timeend) {
                                    ++countStory
                                }
                                ustory = Story(imageurl, timestart, timeend, storyId, userId)
                            }
                        }

                        if (countStory > 0) {
                            if (ustory != null) {
                                storyList.add(ustory)
                            }
                        }

                    }
                }
                storyList.sortedByDescending {
                    it.timestart
                }
                storyList.add(0, Story("", 0, 0, "", Firebase.auth.currentUser!!.uid))

                storyMutableLiveData.postValue(Resource.Success(storyList))

            }

        }.addOnFailureListener {
            storyMutableLiveData.postValue(it.localizedMessage?.let { it1 -> Resource.Error(it1) })
        }

    }


}