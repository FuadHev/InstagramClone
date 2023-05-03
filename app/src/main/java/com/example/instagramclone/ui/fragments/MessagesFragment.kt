package com.example.instagramclone.ui.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramclone.R
import com.example.instagramclone.base.BaseFragment
import com.example.instagramclone.data.entity.Comment
import com.example.instagramclone.data.entity.Message
import com.example.instagramclone.databinding.FragmentMessagesBinding
import com.example.instagramclone.ui.adapters.MessaggeAdapter
import com.example.instagramclone.ui.viewmodel.ChatsViewModel
import com.example.instagramclone.ui.viewmodel.MessagesViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.onesignal.OneSignal
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONException
import org.json.JSONObject
import java.util.UUID


class MessagesFragment : BaseFragment() {

    private lateinit var binding: FragmentMessagesBinding
    private val args by navArgs<MessagesFragmentArgs>()
    var senderRoom: String? = null
    var receiverRoom: String? = null
    private lateinit var firestore: FirebaseFirestore
    private val viewModel by activityViewModels<MessagesViewModel>()
    private val messageAdapter by lazy {
        MessaggeAdapter(emptyList())
    }

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

        userInfo(currentUser, binding.mUsername, binding.chatImage)


        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        viewModel.readMessages(senderRoom!!)
        val layoutManager=LinearLayoutManager(requireActivity())
        binding.messageRv.layoutManager = layoutManager
        binding.messageRv.adapter = messageAdapter

        binding.nestedScroll.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            // RecyclerView'ın son elemanı ekranda görünürse
            if (scrollY == binding.nestedScroll.getChildAt(0).measuredHeight - binding.nestedScroll.measuredHeight) {
                // RecyclerView'ın son elemanına odaklan
                layoutManager.scrollToPositionWithOffset(messageAdapter.itemCount - 1, 0)
            }
        })

        binding.send.setOnClickListener {
            sendMessage(senderUid, receiverUid)
        }


    }


    fun sendMessage(senderUid: String, receiverUid: String) {
        val randomkey = UUID.randomUUID().toString()
        val message = binding.editMessage.text.toString()

        if (message.trim()!=""){
            val hkey = hashMapOf<String, Any>()
            val hmessage = hashMapOf<Any, Any>()
            hmessage["messageId"] = randomkey
            hmessage["messagetxt"] = message
            hmessage["seen"] = false
            hmessage["senderId"] = senderUid
            hmessage["time"] = Timestamp.now()

            hkey[randomkey] = hmessage

            firestore.collection("Messages").document(senderRoom!!).set(hkey, SetOptions.merge())
                .addOnSuccessListener {

                    getPlayerIdSendNotification(receiverUid, message)
                }
            firestore.collection("Messages").document(receiverRoom!!)
                .set(hkey, SetOptions.merge())
            firestore.collection("Chats").document(receiverRoom!!)
                .set(
                    hashMapOf(
                        "time" to Timestamp.now(),
                        "seen" to true,
                        "lastmessage" to "",
                        "senderId" to receiverUid
                    )
                )
            firestore.collection("Chats").document(senderRoom!!)
                .set(
                    hashMapOf(
                        "time" to Timestamp.now(),
                        "seen" to false,
                        "lastmessage" to message,
                        "senderId" to senderUid
                    )
                )

            binding.editMessage.setText("")
        }

    }

    override fun onResume() {
        super.onResume()
        firestore.collection("Chats").document(Firebase.auth.currentUser!!.uid + args.userId)
            .update("seen", true)

    }


    private fun userInfo(profileId: String, userName: TextView, profilImage: CircleImageView) {

        Firebase.firestore.collection("user").document(profileId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_SHORT)
                        .show()
                } else {
                    if (value != null && value.exists()) {

                        val username = value.get("username") as String
                        val imageurl = value.get("image_url") as String

                        Picasso.get().load(imageurl).into(profilImage)
                        userName.text = username


                    }


                }

            }


    }


    private fun getPlayerIdSendNotification(userId: String, message: String) {

        var username = ""
        var profilImage = ""
        Firebase.firestore.collection("user").document(Firebase.auth.currentUser!!.uid)
            .addSnapshotListener { value, error ->
                if (error != null) {

                } else {
                    if (value != null) {
                        username = value.get("username") as String
                        profilImage = value.get("image_url") as String
                    }
                }
            }
        Firebase.firestore.collection("user").document(userId).addSnapshotListener { value, error ->
            if (error != null) {

            } else {
                if (value != null) {

                    val playerId = value.get("playerId") as String?
                    if (playerId != null) {
                        sentPushNotification(playerId, username, message, profilImage)
                    }

                }
            }
        }

    }

    private fun sentPushNotification(
        playerId: String,
        username: String,
        message: String,
        profileImage: String
    ) {
        try {

            // profil fotosu elave etmeliyem
//
//            "large_icon": "$profileImage",
//            "large_icon_width": 64,
//            "large_icon_height": 64
            OneSignal.postNotification(
                JSONObject(
                    """{
        "app_id": "9b3b9701-9264-41ef-b08c-1c69f1fabfef", 
        "include_player_ids": ["$playerId"],
        "headings": {"en": "$username"},
        "contents": {"en": "$message"}
    }"""
                ),
                null
            )


        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }
    override fun addObserves() {
        viewModel.messageList.observe(viewLifecycleOwner) {
            messageAdapter.updateMessages(it)
            binding.nestedScroll.post {
                binding.nestedScroll.fullScroll(View.FOCUS_DOWN)
                binding.editMessage.requestFocus()
            }

        }
    }


}