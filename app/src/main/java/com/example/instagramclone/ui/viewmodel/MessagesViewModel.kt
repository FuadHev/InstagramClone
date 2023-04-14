package com.example.instagramclone.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramclone.data.entity.Message
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MessagesViewModel:ViewModel(){
    val messageList=MutableLiveData<List<Message>>()


     fun readMessages(senderRoom:String) {
        Firebase.firestore.collection("Messages").document(senderRoom)
            .addSnapshotListener { value, error ->
                if (error != null) {
                } else {
                    if (value != null && value.exists()) {
                        try {
                            val doc = value.data as HashMap<*,*>
                            val messagesList=ArrayList<Message>()
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


                        }


                    }

                }
            }
    }
}