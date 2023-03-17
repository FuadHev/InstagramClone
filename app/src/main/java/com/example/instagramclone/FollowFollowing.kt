package com.example.instagramclone

import android.util.Log
import android.widget.Button
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object FollowFollowing {

    var arg:String?=null
//    val multiplaFollowList=MutableLiveData<ArrayList<String>>()
//
//
//
//
//
//    fun checkFollowing() {
//       val firestore=Firebase.firestore
//
//
//        firestore.collection("Follow").document(Firebase.auth.currentUser!!.uid)
//            .addSnapshotListener { documentSnapshot, error ->
//                if (error != null) {
//                    error.localizedMessage?.let {
//                        Log.e("", it)
//                        return@addSnapshotListener
//                    }
//                } else {
//
//
//                    if (documentSnapshot != null && documentSnapshot.exists()) {
//                        val follow = documentSnapshot.data
//                        val followList=ArrayList<String>()
//
//                        if (follow != null) {
//                            try {
//
//                                val following = follow["following"] as HashMap<*, *>
//                                followList.clear()
//                                for (i in following) {
//                                    followList.add(i.key as String)
//                                }
//                                multiplaFollowList.value=followList
//
//                            } catch (_: java.lang.NullPointerException) {
//
//                            }
//                        }
//
//                    } else {
//                        Log.e("", "")
//                    }
//                }
//            }
//
//
//    }



}