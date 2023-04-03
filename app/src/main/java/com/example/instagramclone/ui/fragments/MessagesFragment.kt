package com.example.instagramclone.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramclone.R
import com.example.instagramclone.data.entity.Comment
import com.example.instagramclone.data.entity.Message
import com.example.instagramclone.databinding.FragmentMessagesBinding
import com.example.instagramclone.ui.adapters.MessaggeAdapter
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.UUID


class MessagesFragment : Fragment() {

    private lateinit var binding: FragmentMessagesBinding
    private val args by navArgs<MessagesFragmentArgs>()
    private lateinit var messagesList: ArrayList<Message>
    private lateinit var messageAdapter: MessaggeAdapter
    var senderRoom: String? = null
    var receiverRoom: String? = null
    private lateinit var firestore: FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentUser = args.userId
        firestore = Firebase.firestore

        val receiverUid = currentUser

        val senderUid = Firebase.auth.currentUser!!.uid

        userInfo(currentUser,binding.mUsername,binding.chatImage)


//        Picasso.get().load(currentUser.imageUrl).into(binding.chatImage)
//        binding.mUsername.text = currentUser.username

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        messagesList = ArrayList()

        binding.messageRv.layoutManager = LinearLayoutManager(requireActivity())
        messageAdapter = MessaggeAdapter(messagesList)
        binding.messageRv.adapter = messageAdapter

        readMessages()
        binding.send.setOnClickListener {

            val randomkey = UUID.randomUUID().toString()

            val hkey = hashMapOf<String, Any>()
            val hmessage = hashMapOf<Any, Any>()
            hmessage["messageId"] = randomkey
            hmessage["messagetxt"] = binding.editMessage.text.toString()
            hmessage["seen"] = false
            hmessage["senderId"] = senderUid
            hmessage["time"] = Timestamp.now()

            hkey[randomkey] = hmessage

            firestore.collection("Messages").document(senderRoom!!).set(hkey, SetOptions.merge())
                .addOnSuccessListener {
                    firestore.collection("Messages").document(receiverRoom!!)
                        .set(hkey, SetOptions.merge())
                    firestore.collection("Chats").document(receiverRoom!!)
                        .set(hashMapOf("time" to Timestamp.now(),"seen" to true))
                    firestore.collection("Chats").document(senderRoom!!)
                        .set(hashMapOf("time" to Timestamp.now(),"seen" to false))
                }

            binding.editMessage.setText("")

        }




    }


    fun userInfo(profileId:String,userName: TextView, profilImage: CircleImageView) {

        Firebase.firestore.collection("user").document(profileId).addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_SHORT).show()
            } else {
                if (value != null && value.exists()) {

                    val username = value.get("username") as String
                    val imageurl = value.get("image_url") as String

                    Picasso.get().load(imageurl).into(profilImage)
                    userName.text=username


                }


            }

        }


    }

    fun readMessages(){
        firestore.collection("Messages").document(senderRoom!!).addSnapshotListener { value, error ->
            if (error != null) {
            } else {
                if (value != null && value.exists()) {
                    val doc = value.data as HashMap<*,*>
                    try {


                        messagesList.clear()
                        for (i in doc) {
                            val message = i.value as HashMap<*,*>
                            val messageId=message["messageId"] as String
                            val messageTxt=message["messagetxt"] as String
                            val senderId=message["senderId"] as String
                            val time=message["time"] as Timestamp
                            val seen=message["seen"] as Boolean

                            val messages=Message(messageId,messageTxt,senderId,time,seen)
                            messagesList.add(messages)

                        }
                        messagesList.sortBy {
                            it.time
                        }
                        messageAdapter.notifyDataSetChanged()



                    } catch (e: java.lang.NullPointerException) {


                    }


                }

            }
        }
    }

}