package com.example.instagramclone

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Adapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramclone.data.entity.Comment
import com.example.instagramclone.databinding.ActivityCommentsBinding
import com.example.instagramclone.ui.adapters.CommentAdapter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlin.random.Random

class CommentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommentsBinding
    private lateinit var postId: String
    private lateinit var publisherId: String
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: CommentAdapter
    private lateinit var commentList:ArrayList<Comment>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseUser = Firebase.auth.currentUser!!
        commentList= ArrayList()
        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = "Comments"
        firestore = Firebase.firestore

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        postId = intent.getStringExtra("postId") as String
        publisherId = intent.getStringExtra("publisherId") as String


        binding.commentsRv.setHasFixedSize(true)
        binding.commentsRv.layoutManager=LinearLayoutManager(this)
        adapter= CommentAdapter(this, emptyList())




        binding.post.setOnClickListener {

            if (binding.addToComment.equals(" ")) {
                Toast.makeText(this, "Please add the comment", Toast.LENGTH_SHORT).show()
            } else {

                addComment()

            }
            binding.addToComment.text.clear()


        }


        getImage()
        readComment()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addComment() {

        commentList.clear()
        val randomValue = (20..28).random()
        val commentId = randomAlphaNumericString(randomValue)
        val time=Timestamp.now()


        val reference = firestore.collection("Comments").document(postId)

        val hmapkey = HashMap<String, Any>()
        val hmap = HashMap<String, Any>()


        hmap["comment"] = binding.addToComment.text.toString()
        hmap["publisher"] = firebaseUser.uid
        hmap["time"]=time

        hmapkey[commentId] = hmap
        reference.set(hmapkey, SetOptions.merge())
        adapter.notifyDataSetChanged()

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


    private fun readComment(){
        firestore.collection("Comments").document(postId).addSnapshotListener { value, error ->

            if (error!=null){

            }else{
                if (value!=null&&value.exists()){
                    val doc=value.data as HashMap<*,*>
                    try {
                        for (i in doc){
                            val com=i.value as HashMap<*,*>
                            val comm=com.get("comment") as String
                            val publisher=com.get("publisher") as String
                            val time=com.get("time") as Timestamp
                            val comment =Comment(comm,publisher,time)
                            commentList.add(comment)
                        }

                        commentList.sortByDescending {
                            it.time
                        }
                        adapter= CommentAdapter(this,commentList)
                        binding.commentsRv.adapter=adapter

                    }catch (e:java.lang.NullPointerException){


                    }


                }

            }



        }

    }


}