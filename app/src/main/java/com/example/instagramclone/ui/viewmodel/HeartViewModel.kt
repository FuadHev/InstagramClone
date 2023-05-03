package com.example.instagramclone.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramclone.data.entity.Notification
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HeartViewModel:ViewModel() {

    val notificationLiveData=MutableLiveData<List<Notification>>()



    init {
        readNotification()
    }

    fun readNotification() {
        Firebase.firestore.collection("Notification").document(Firebase.auth.currentUser!!.uid)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    error.localizedMessage?.let { Log.e("error", it) }
                } else {
                    if (value != null) {
                        val notificationsList=ArrayList<Notification>()
                        try {
                            val datakeys = value.data as HashMap<*,*>
                            for (data in datakeys){
                                val valuedata=data.value as HashMap<*,*>
                                val userId=valuedata["userId"] as String
                                val ntext=valuedata["nText"] as String
                                val postId=valuedata["postId"] as String
                                val isPost=valuedata["isPost"] as Boolean
                                val time=valuedata["time"] as Timestamp
                                val notification= Notification(userId,ntext,postId,isPost,time)
                                notificationsList.add(notification)
                            }
                            notificationsList.sortByDescending {
                                it.time
                            }
                            notificationLiveData.postValue(notificationsList)


                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        }

                    }

                }

            }
    }

}