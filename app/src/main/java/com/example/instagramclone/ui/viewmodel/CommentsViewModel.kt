package com.example.instagramclone.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramclone.data.entity.Comment
import com.example.instagramclone.ui.adapters.CommentAdapter
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CommentsViewModel:ViewModel() {

    val commentsList=MutableLiveData<ArrayList<Comment>>()
    var postId=""
    var firestore= Firebase.firestore


    fun readComment(){
        firestore.collection("Comments").document(postId).addSnapshotListener { value, error ->
            if (error != null) {
            } else {
                if (value != null && value.exists()) {
                    val doc = value.data as HashMap<*, *>
                    try {

                        val commentList=ArrayList<Comment>()
                        for (i in doc) {
                            val com = i.value as HashMap<*, *>
                            val comm = com.get("comment") as String
                            val publisher = com.get("publisher") as String
                            val time = com.get("time") as Timestamp
                            val comment = Comment(comm, publisher, time)
                            commentList.add(comment)
                        }

                        commentList.sortByDescending {
                            it.time
                        }

                        commentsList.value=commentList

                    } catch (e: java.lang.NullPointerException) {


                    }


                }

            }

        }


    }


}