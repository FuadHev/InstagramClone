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



    fun getUsersId(){
        Firebase.firestore.collection("Chats").addSnapshotListener { value, error ->

            if (value!=null){
                val idList=ArrayList<String>()
                for (doc in value.documents){

                    val senderId=doc.get("senderId") as String
                    if (senderId!=Firebase.auth.currentUser!!.uid){
                        idList.add(senderId)
                    }

                }

                allUser(idList)
            }
        }
    }
    fun allUser(idList:ArrayList<String>){

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

                        if (idList.contains(user_id)){
                            val user = Users(user_id, email, username, "", imageurl, "")
                            alluser.add(user)
                        }


                    }
                    getChatUser(alluser)

                }
            }
        }


    }

    private fun getChatUser(alluser:ArrayList<Users>){

        val ref = Firebase.firestore.collection("Chats")


        ref.addSnapshotListener { value, error ->

            if (error!=null){

            }else{
                if (value!=null){
                    val chatlist = ArrayList<ChatUser>()
                    for ( doc in value.documents){

                        val time = doc.get("time") as Timestamp
                        val seen = doc.get("seen") as Boolean
                        val senderId=doc.get("senderId") as String
                        val lastMessage=doc.get("lastmessage") as String
                        for (user in alluser){
                            if (user.user_id==senderId){
                                val chatUser = ChatUser(user.user_id, user.username, user.imageurl,lastMessage ,time,seen)
                                chatlist.add(chatUser)

                            }

                        }
                        chatlist.sortByDescending { chatItem->
                            chatItem.time
                        }


                    }
                    Log.e("allchatuser",chatlist.toString())
                    chatList.value=chatlist
                }
            }
        }


    }
}