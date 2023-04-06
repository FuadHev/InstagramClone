package com.example.instagramclone.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramclone.data.entity.ChatUser
import com.example.instagramclone.data.entity.Users
import com.example.instagramclone.databinding.FragmentChatsBinding
import com.example.instagramclone.ui.adapters.ChatAdapter
import com.example.instagramclone.ui.adapters.UserClickListener
import com.example.instagramclone.ui.viewmodel.ChatsViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.net.UnknownServiceException


class ChatsFragment : Fragment() {

    private lateinit var binding: FragmentChatsBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ChatAdapter
    private lateinit var alluser: ArrayList<Users>
    private val viewModel by activityViewModels<ChatsViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = Firebase.firestore
        auth = Firebase.auth

        alluser=ArrayList()

        binding.chatRv.layoutManager = LinearLayoutManager(requireActivity())

//        adapter = ChatAdapter(object : UserClickListener {
//            override fun chatUserCLickListener(currentUser: String) {
//
//                findNavController().navigate(ChatsFragmentDirections.actionChatsFragmentToMessagesFragment3(currentUser))
//
//                firestore.collection("Chats").document(auth.currentUser!!.uid + currentUser).update("seen",true)
//
//            }
//
//        }, emptyList())
//        binding.chatRv.adapter = adapter

        allUsers()
//
//        viewModel.allUser()
//        viewModel.chatList.observe(viewLifecycleOwner){
//            viewModel.allUser()
//            adapter.updateChatList(it)
//        }


    }

    fun allUsers() {
        firestore.collection("user").addSnapshotListener { value, error ->
            if (error != null) {
                error.localizedMessage?.let { Log.e("error", it) }
            } else {
                if (value != null) {

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


    fun getChatUser(alluser: ArrayList<Users>) {
        val ref = firestore.collection("Chats")
        val list = ArrayList<ChatUser>()
        alluser.forEach {
            val senderRoom = auth.currentUser!!.uid + it.user_id
//            val senderRoom =  it.user_id+ auth.currentUser!!.uid

            ref.document(senderRoom).addSnapshotListener { value, error ->

                if (error != null) {

                } else {
                    if (value != null && value.exists()) {
                        val time = value.get("time") as Timestamp
                        val seen = value.get("seen") as Boolean
                        val lastMessage=value.get("lastmessage") as String
                        val chatUser = ChatUser(it.user_id, it.username, it.imageurl,lastMessage ,time,seen)
                        list.add(chatUser)
                        adapter = ChatAdapter(object : UserClickListener {
                            override fun chatUserCLickListener(currentUser: String) {



                                findNavController().navigate(ChatsFragmentDirections.actionChatsFragmentToMessagesFragment3(currentUser))
                                firestore.collection("Chats").document(auth.currentUser!!.uid + currentUser).update("seen",true)

                            }

                        }, list)
                        binding.chatRv.adapter = adapter
                    }
                }

            }


        }

    }


}