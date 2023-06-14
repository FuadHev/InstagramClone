package com.example.instagramclone.ui.view.comments_view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramclone.R
import com.example.instagramclone.base.BaseFragment
import com.example.instagramclone.databinding.DeleteMessageDialogBinding
import com.example.instagramclone.databinding.FragmentCommentsBinding
import com.example.instagramclone.ui.adapters.CommentAdapter
import com.example.instagramclone.ui.adapters.CommentClickListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
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
import java.util.*
import kotlin.collections.HashMap
import kotlin.random.Random

class CommentsFragment : BaseFragment() {


    private lateinit var binding: FragmentCommentsBinding
    private lateinit var postId: String
    private lateinit var publisherId: String
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firestore: FirebaseFirestore
    private val viewModel by viewModels<CommentsViewModel>()
    private val args by navArgs<CommentsFragmentArgs>()
    private val adapter by lazy {
        CommentAdapter(object : CommentClickListener {
            override fun commentLongClickListener(postId: String, commentId: String) {
                showDeletePostDialog(postId,commentId)
            }


        }, requireActivity(), emptyList(), emptyList())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_comments, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseUser = Firebase.auth.currentUser!!
        binding.commentsFragment = this
        binding.toolbar.title = "Comments"
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        binding.toolbar.setTitleTextColor(Color.BLACK)
        firestore = Firebase.firestore

        val arg = args
        postId = arg.postId
        publisherId = arg.publisherId



        binding.commentsRv.setHasFixedSize(true)
        binding.commentsRv.layoutManager = LinearLayoutManager(requireActivity())
        binding.commentsRv.adapter = adapter

        getImage()
        viewModel.readComment(postId)
    }

    private fun showDeletePostDialog(postId: String,commentId:String){
        val dialogBinding= DeleteMessageDialogBinding.inflate(layoutInflater)
        val mDialog= Dialog(requireContext())
        mDialog.setContentView(dialogBinding.root)
        mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogBinding.dInfo.setText(R.string.delete_post_comment)

        dialogBinding.yes.setOnClickListener {
            firestore.collection("Comments").document(postId).update(commentId,FieldValue.delete())

            mDialog.dismiss()
        }

        dialogBinding.no.setOnClickListener {

            mDialog.dismiss()
        }

        mDialog.create()
        mDialog.show()

    }



    fun sendComment() {
        val comment = binding.addToComment
        if (comment.text.trim().toString() == "") {
            Toast.makeText(requireContext(), "Please add the comment", Toast.LENGTH_SHORT).show()
            comment.text.clear()

        } else {
            addComment()
            comment.text.clear()
        }


    }

    private fun getPlayerIdSendNotification(postPublisher: String, comment: String) {

        var username = ""
        firestore.collection("user").document(firebaseUser.uid).get()

            .addOnSuccessListener { value ->
                if (value != null) {
                    username = value.get("username") as String
                    firestore.collection("user").document(postPublisher).get()
                        .addOnSuccessListener { uservalue ->
                            if (uservalue != null) {
                                val playerId = uservalue.get("playerId") as String?
                                if (playerId != null && postPublisher != firebaseUser.uid) {
                                    sentPushNotification(playerId, username, comment)
                                }
                            }
                        }.addOnFailureListener {
                        it.localizedMessage?.let { message -> Log.e("User_Notification", message) }
                    }
                }
            }


    }

    private fun sentPushNotification(playerId: String, username: String, comment: String) {
        try {

            OneSignal.postNotification(
                JSONObject(
                    """{
          "contents": {"en": "Commented on your post: $comment"},
          "include_player_ids": ["$playerId"],
          "headings": {"en": "$username"}
                 }
        """.trimIndent()
                ), null
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun addNotification() {

        if (publisherId != firebaseUser.uid) {
            val ref = firestore.collection("Notification").document(publisherId)
            val nKey = UUID.randomUUID()
            val notification = hashMapOf<String, Any>()
            val notifi = hashMapOf<String, Any>()
            notifi["userId"] = firebaseUser.uid
            notifi["nText"] = "Commented ${binding.addToComment.text}"
            notifi["postId"] = postId
            notifi["isPost"] = true
            notifi["notificationId"] = nKey.toString()
            notifi["time"] = Timestamp.now()

            notification[nKey.toString()] = notifi

            ref.set(notification, SetOptions.merge())
        }


    }

    private fun addComment() {

        val randomValue = (20..40).random()
        val commentId =
            randomAlphaNumericString(randomValue)//UUID.randomUUID().toString() ile evez ede bilerem baxacam axirda.
        val time = Timestamp.now()
        val reference = firestore.collection("Comments").document(postId)
        val hmapkey = HashMap<String, Any>()
        val hmap = HashMap<String, Any>()
        hmap["comment"] = binding.addToComment.text.toString()
        hmap["publisher"] = firebaseUser.uid
        hmap["comment_id"]=commentId
        hmap["time"] = time
        hmapkey[commentId] = hmap
        reference.set(hmapkey, SetOptions.merge())
        addNotification()
        getPlayerIdSendNotification(publisherId, binding.addToComment.text.toString())


    }

    private fun getImage() {
        val reference = firestore.collection("user").document(firebaseUser.uid)
        reference.addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_SHORT).show()
            } else {
                if (value != null) {
                    val imageUrl = value.get("image_url") as String
                    Picasso.get().load(imageUrl).into(binding.profilImage)
                }
            }
        }
    }


    private fun randomAlphaNumericString(length: Int): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..length)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }

    override fun addObserves() {
        viewModel.commentsList.observe(viewLifecycleOwner) {
            adapter.updateElements(it)
        }

        viewModel.publisherInfoLiveData.observe(viewLifecycleOwner) {
            adapter.updatePublisher(it)
        }
    }

}