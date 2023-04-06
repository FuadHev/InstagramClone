package com.example.instagramclone.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.example.instagramclone.data.entity.ChatUser
import com.example.instagramclone.data.entity.Users
import com.example.instagramclone.ui.adapters.ChatAdapter
import com.example.instagramclone.ui.adapters.UserClickListener
import com.example.instagramclone.ui.fragments.ChatsFragmentDirections
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatsViewModel:ViewModel() {

    val chatList=MutableLiveData<List<ChatUser>>()



    fun allUser(){

        Firebase.firestore.collection("user").addSnapshotListener { value, error ->
            if (error != null) {
                error.localizedMessage?.let { Log.e("error", it) }
            } else {
                if (value != null) {
                    val alluser=ArrayList<Users>()
                    for (users in value.documents) {
                        val user_id = users.get("user_id") as String
                        val email = users.get("email") as String
                        val username = users.get("username") as String
                        val imageurl = users.get("image_url") as String

                        val user = Users(user_id, email, username, "", imageurl, "")
                        alluser.add(user)

                    }
                    getChatUser(alluser)

                }
            }
        }


    }

    private fun getChatUser(alluser:ArrayList<Users>){

        val ref = Firebase.firestore.collection("Chats")
        val chatlist = ArrayList<ChatUser>()
        alluser.forEach {
            val senderRoom = Firebase.auth.currentUser!!.uid + it.user_id


            ref.document(senderRoom).addSnapshotListener { value, error ->

                if (error != null) {

                } else {

                    if (value != null && value.exists()) {
                        val time = value.get("time") as Timestamp
                        val seen = value.get("seen") as Boolean
                        val lastMessage=value.get("lastmessage") as String
                        val chatUser = ChatUser(it.user_id, it.username, it.imageurl,lastMessage ,time,seen)
                        chatlist.add(chatUser)

                        chatList.value=chatlist

                    }
                }

            }


        }

    }
}