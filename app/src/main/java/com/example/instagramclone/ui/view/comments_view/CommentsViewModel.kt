package com.example.instagramclone.ui.view.comments_view

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramclone.model.Comment
import com.example.instagramclone.model.Users
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CommentsViewModel:ViewModel() {

    val commentsList=MutableLiveData<ArrayList<Comment>>()
    val publisherInfoLiveData= MutableLiveData<List<Users>>()
    var firestore= Firebase.firestore

    fun readComment(postId:String){
        firestore.collection("Comments").document(postId).addSnapshotListener { value, error ->
            if (error != null) {

                Log.e("comment", "readComment_error")
                return@addSnapshotListener
            }
                if (value != null && value.exists()) {
                    val doc = value.data as? HashMap<*,*>
                    try {
                        val publisherIdList=ArrayList<String>()
                        val commentList=ArrayList<Comment>()
                        if (doc != null) {
                            for (i in doc) {
                                val com = i.value as HashMap<*, *>
                                val comm = com["comment"] as String
                                val publisher = com["publisher"] as String
                                val commentId = com["comment_id"] as String
                                val time = com["time"] as Timestamp

                                val comment = Comment(comm,publisher,postId,commentId,time)
                                publisherIdList.add(publisher)
                                commentList.add(comment)

                            }
                        }
                        allUsers(publisherIdList)
                        commentList.sortByDescending {
                            it.time
                        }
                        commentsList.postValue(commentList)

                    } catch (e: java.lang.NullPointerException) {
                        Log.e("comment", "readComment_error")


                    }


                }



        }


    }
    private fun allUsers(publishersIds:List<String>) {
        firestore.collection("user").get().addOnSuccessListener { value ->

            if (value != null) {
                val allPublisherList=ArrayList<Users>()
                for (users in value.documents) {
                    if (publishersIds.contains(users.id)){
                        val userid = users.get("user_id") as String
                        val username = users.get("username") as String
                        val imageurl = users.get("image_url") as String
                        val user = Users(userid, "", username, "",imageurl, "")
                        allPublisherList.add(user)
                    }
                }
                publisherInfoLiveData.postValue(allPublisherList)
            }

        }

    }



}