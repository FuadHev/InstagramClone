package com.example.instagramclone.ui.view.activity

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramclone.R
import com.example.instagramclone.databinding.ActivityCommentsBinding
import com.example.instagramclone.ui.adapters.CommentAdapter
import com.example.instagramclone.ui.viewmodel.CommentsViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
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

class CommentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommentsBinding
    private lateinit var postId: String
    private lateinit var publisherId: String
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firestore: FirebaseFirestore
    private val viewModel by viewModels<CommentsViewModel>()
    private val adapter by lazy {
        CommentAdapter(this, emptyList(), emptyList())
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_comments)
        setContentView(binding.root)

        firebaseUser = Firebase.auth.currentUser!!
        binding.commentActivity=this
        binding.toolbar.title = "Comments"

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setTitleTextColor(Color.BLACK)
        firestore = Firebase.firestore


        postId = intent.getStringExtra("postId") as String
        publisherId = intent.getStringExtra("publisherId") as String

        binding.commentsRv.setHasFixedSize(true)
        binding.commentsRv.layoutManager = LinearLayoutManager(this)
        binding.commentsRv.adapter = adapter

        viewModel.commentsList.observe(this) {
            adapter.updateElements(it)
        }

        viewModel.publisherInfoLiveData.observe(this){
            adapter.updatePublisher(it)
        }
//        binding.post.setOnClickListener {
//            sendComment()
//        }

        getImage()
        viewModel.readComment(postId)


    }

     fun sendComment(){
        val comment = binding.addToComment
        if (comment.text.trim().toString() == "") {
            Toast.makeText(this, "Please add the comment", Toast.LENGTH_SHORT).show()
            comment.text.clear()

        } else {
            addComment()
        }
        comment.text.clear()

    }

    private fun getPlayerIdSendNotification(postPublisher: String, comment: String) {

        var username = ""
        firestore.collection("user").document(firebaseUser.uid)
            .addSnapshotListener { value, error ->
                if (error != null) {

                } else {
                    if (value != null) {
                        username = value.get("username") as String
                    }
                }
            }
        firestore.collection("user").document(postPublisher).get().addOnSuccessListener { value ->

            if (value != null) {
                val playerId = value.get("playerId") as String?
                if (playerId != null && postPublisher != firebaseUser.uid) {
                    sentPushNotification(playerId, username, comment)
                }

            }

        }.addOnFailureListener {
            it.localizedMessage?.let { it1 -> Log.e("User_Notification", it1) }
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

        val randomValue = (20..28).random()
        val commentId =
            randomAlphaNumericString(randomValue)//UUID.randomUUID().toString() ile evez ede bilerem baxacam axirda.
        val time = Timestamp.now()
        val reference = firestore.collection("Comments").document(postId)
        val hmapkey = HashMap<String, Any>()
        val hmap = HashMap<String, Any>()
        hmap["comment"] = binding.addToComment.text.toString()
        hmap["publisher"] = firebaseUser.uid
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
                Toast.makeText(this, error.localizedMessage, Toast.LENGTH_SHORT).show()
            } else {
                if (value != null) {
                    val imageUrl = value.get("image_url") as String
                    Picasso.get().load(imageUrl).into(binding.profilImage)
                }
            }
        }
    }


    fun randomAlphaNumericString(length: Int): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..length)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }




}