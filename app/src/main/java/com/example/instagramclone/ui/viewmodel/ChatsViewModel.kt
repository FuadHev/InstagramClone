package com.example.instagramclone.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramclone.model.ChatUser
import com.example.instagramclone.model.Users
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.instagramclone.utils.Resource

class ChatsViewModel : ViewModel() {

    val chatLiveData = MutableLiveData<Resource<List<ChatUser>>>()


    init {
        getUsersId()
    }
    fun getUsersId() {
        Firebase.firestore.collection("Chats").addSnapshotListener { value, error ->

            if (value != null && !value.isEmpty) {
                val chatUserIdList = ArrayList<String>()
                for (doc in value.documents) {
                    val senderId = doc.get("senderId") as? String
                    if (senderId != Firebase.auth.currentUser!!.uid && Firebase.auth.currentUser!!.uid + senderId == doc.id) {
                        if (senderId != null) {
                            chatUserIdList.add(senderId)
                        }
                    }
                }


                    chatLiveData.postValue(Resource.Loading())
                    allUser(chatUserIdList)


            }
        }


    }

    private fun allUser(chatUserIdList: ArrayList<String>) {

        Firebase.firestore.collection("user").addSnapshotListener { value, error ->
            if (error != null) {
                chatLiveData.postValue(error.localizedMessage?.let { Resource.Error(it) }
                    ?: Resource.Error("Chat_Error"))
            } else {
                if (value != null) {

                    val alluser = ArrayList<Users>()
                    for (users in value.documents) {
                        val user_id = users.get("user_id") as String
                        val username = users.get("username") as String
                        val imageurl = users.get("image_url") as String

                        if (chatUserIdList.contains(user_id)) {
                            val user = Users(user_id, "", username, "", imageurl, "")
                            alluser.add(user)
                        }
                    }
                    getChatUser(alluser)

                }
            }
        }


    }

    private fun getChatUser(alluser: ArrayList<Users>) {

        val ref = Firebase.firestore.collection("Chats")
        ref.addSnapshotListener { value, error ->

            if (error != null) {
                chatLiveData.postValue(error.localizedMessage?.let { Resource.Error(it) }
                    ?: Resource.Error("Chat_Error"))
            } else {
                if (value != null) {
                    val chatlist = ArrayList<ChatUser>()
                    for (doc in value.documents) {
                        val time = doc.get("time") as Timestamp
                        val seen = doc.get("seen") as Boolean
                      //  val senderId = doc.get("senderId") as String
                        val lastMessage = doc.get("lastmessage") as String
                        for (user in alluser) {
                            if (Firebase.auth.currentUser!!.uid + user.user_id == doc.id) {
                                val chatUser = ChatUser(
                                    user.user_id,
                                    user.username,
                                    user.imageurl,
                                    lastMessage,
                                    time,
                                    seen
                                )
                                chatlist.add(chatUser)
                            }
                        }
                        chatlist.sortByDescending { chatItem ->
                            chatItem.time
                        }
                    }
                    chatLiveData.postValue(Resource.Success(chatlist))
                }
            }
        }


    }
}