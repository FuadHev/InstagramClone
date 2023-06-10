package com.example.instagramclone.ui.view.discover_posts

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramclone.model.Posts
import com.example.instagramclone.utils.Resource
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DiscoverPostsViewModel : ViewModel() {

    val postMutableLiveData =MutableLiveData<Resource<ArrayList<Posts>>>()

    fun readPost(currentPosts: Posts) {
        Firebase.firestore.collection("Posts").get()
            .addOnSuccessListener { value ->
                if (value != null) {
                    val postList = ArrayList<Posts>()
                    val hashSet= hashSetOf<Posts>()
                    postMutableLiveData.postValue(Resource.Loading())
                    try {
                        for (document in value.documents) {

                            val postid = document.get("postId") as String
                            if (postid!=currentPosts.post_id){
                                val publisher = document.get("publisher") as String
                                val postImage = document.get("postImage") as String
                                val description = document.get("description") as String
                                val time = document.get("time") as Timestamp
                                val post = Posts(postid, postImage, description, publisher, time)
                                hashSet.add(post)
                            }

                        }
                        postList.addAll(hashSet)
                        postList.add(0,currentPosts)
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
}