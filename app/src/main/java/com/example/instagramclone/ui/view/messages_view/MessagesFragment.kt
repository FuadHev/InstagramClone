package com.example.instagramclone.ui.view.messages_view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramclone.R
import com.example.instagramclone.base.BaseFragment
import com.example.instagramclone.databinding.DeleteMessageDialogBinding
import com.example.instagramclone.databinding.FragmentMessagesBinding
import com.example.instagramclone.ui.adapters.MessageClickListener
import com.example.instagramclone.ui.adapters.MessaggeAdapter
import com.example.instagramclone.ui.view.messages_view.MessagesFragmentArgs
import com.example.instagramclone.utils.Constant
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.onesignal.OneSignal
import com.squareup.picasso.Picasso
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
        MessaggeAdapter(object : MessageClickListener {
            override fun messageClickListener(senderId: String, messageId: String) {
                showDeleteMessageDialog(senderId, messageId)
            }
        }, emptyList())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_messages, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSenderRecieverRoom()
        setRecyclerView()

    }

    private fun setSenderRecieverRoom() {
        firestore = Firebase.firestore
        val receiverUid = args.userId
        val senderUid = Firebase.auth.currentUser!!.uid

        binding.messagesFragment = this
        binding.senderUid = senderUid
        binding.receiverUid = receiverUid

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid
        viewModel.checkSession(receiverUid)
        viewModel.readMessages(senderRoom!!)
    }

    private fun setRecyclerView() {
        val layoutManager = LinearLayoutManager(requireActivity())
        binding.messageRv.layoutManager = layoutManager
        binding.messageRv.adapter = messageAdapter

        binding.nestedScroll.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            // RecyclerView'ın son elemanı ekranda görünürse
            if (scrollY == binding.nestedScroll.getChildAt(0).measuredHeight - binding.nestedScroll.measuredHeight) {
                // RecyclerView'ın son elemanına odaklan
                layoutManager.scrollToPositionWithOffset(messageAdapter.itemCount - 1, 0)
            }
        })
    }

    fun sendMessage(senderUid: String, receiverUid: String) {
        val randomkey = UUID.randomUUID().toString()
        val message = binding.editMessage.text.toString()

        if (message.trim() != "") {
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

    private fun getPlayerIdSendNotification(userId: String, message: String) {

        firestore.collection("user").document(Firebase.auth.currentUser!!.uid)
            .get().addOnSuccessListener { value->
                if (value != null) {
                   val username = value.get("username") as String
                    val profileImage = value.get("image_url") as String

                    firestore.collection("user").document(userId).get().addOnSuccessListener { userValue->

                        if (userValue != null) {

                            val playerId = userValue.get("playerId") as String?
                            if (playerId != null && playerId != "") {
                                sentPushNotification(playerId, username, message, profileImage)
                            }

                        }

                    }.addOnFailureListener {
                        it.localizedMessage?.let { Log.e("user_error", it) }
                    }

                }
            }.addOnFailureListener {
                it.localizedMessage?.let { Log.e("user_error", it) }
            }


    }

    private fun showDeleteMessageDialog(senderId: String, messageId: String) {
        val dialogBinding = DeleteMessageDialogBinding.inflate(layoutInflater)
        val mDialog = Dialog(requireContext())
        mDialog.setContentView(dialogBinding.root)
        mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogBinding.dInfo.text = "Delete this message?"

        dialogBinding.yes.setOnClickListener {
            if (senderId == Firebase.auth.currentUser!!.uid) {
                firestore.collection("Messages").document(senderRoom!!)
                    .update(messageId, FieldValue.delete())
                firestore.collection("Messages").document(receiverRoom!!)
                    .update(messageId, FieldValue.delete())
            } else {
                firestore.collection("Messages").document(senderRoom!!)
                    .update(messageId, FieldValue.delete())
            }
            mDialog.dismiss()
        }

        dialogBinding.no.setOnClickListener {

            mDialog.dismiss()
        }

        mDialog.create()
        mDialog.show()

    }

    private fun sentPushNotification(
        playerId: String,
        username: String,
        message: String,
        profileImage: String
    ) {
        try {

            val notificationContent = JSONObject(
                """{
        "app_id": "${Constant.APP_ID}", 
        "include_player_ids": ["$playerId"],
        "headings": {"en": "$username"},
        "contents": {"en": "$message"},
        "large_icon": "$profileImage"
    }"""
            )
            OneSignal.postNotification(notificationContent, null)


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
        viewModel.userInfo.observe(viewLifecycleOwner) {
            binding.mUsername.text = it.username
            binding.toolbarUsername.text = it.username
            Picasso.get().load(it.imageurl).into(binding.chatImage)
            Picasso.get().load(it.imageurl).into(binding.tlbPImage)
        }
        viewModel.checkSession.observe(viewLifecycleOwner) {
            binding.session.text = it
        }
    }


}