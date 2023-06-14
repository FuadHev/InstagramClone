package com.example.instagramclone.ui.view.messages_view

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramclone.model.Message
import com.example.instagramclone.model.Users
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MessagesViewModel : ViewModel() {
    val messageList = MutableLiveData<List<Message>>()
    val userInfo = MutableLiveData<Users>()
    val checkSession = MutableLiveData<String>()


    fun checkSession(profileId: String) {
        Firebase.firestore.collection("user").document(profileId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    error.localizedMessage?.let { Log.e("user_Error", it) }
                    return@addSnapshotListener
                }
                if (value != null && value.exists()) {

                    val username = value.get("username") as String
                    val imageurl = value.get("image_url") as String
                    val online = value.get("online") as? Boolean?

                    val user = Users(username, imageurl)

                    Log.e("checksession", online.toString())
                    if (online != null && online == true) {
                        checkSession.postValue("online")
                    } else {
                        checkSession.postValue("offline")
                    }
                    userInfo.postValue(user)
                }

            }
    }

    fun readMessages(senderRoom: String) {
        Firebase.firestore.collection("Messages").document(senderRoom)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    error.localizedMessage?.let { Log.e("Messages_error", it) }
                    return@addSnapshotListener
                }
                if (value != null && value.exists()) {
                    try {
                        val doc = value.data as HashMap<*, *>
                        val messagesList = ArrayList<Message>()
                        for (i in doc) {
                            val message = i.value as HashMap<*, *>
                            val messageId = message["messageId"] as String
                            val messageTxt = message["messagetxt"] as String
                            val senderId = message["senderId"] as String
                            val time = message["time"] as Timestamp
                            val seen = message["seen"] as Boolean

                            val messages = Message(messageId, messageTxt, senderId, time, seen)
                            messagesList.add(messages)
                        }
                        messagesList.sortBy {
                            it.time
                        }
                        messageList.postValue(messagesList)


                    } catch (e: java.lang.NullPointerException) {
                        e.localizedMessage?.let { Log.e("user_Error", it) }


                    }


                }


            }
    }
}