package com.example.instagramclone.ui.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.data.entity.Posts
import com.example.instagramclone.databinding.PostsCardViewBinding
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class PostsAdapters(val mContext: Context, val postsList: List<Posts>) :
    RecyclerView.Adapter<PostsAdapters.CardViewHolder>() {

    val firebaseUser = Firebase.auth.currentUser
    val firestore = Firebase.firestore

    inner class CardViewHolder(val view: PostsCardViewBinding) : RecyclerView.ViewHolder(view.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: PostsCardViewBinding = PostsCardViewBinding.inflate(layoutInflater, parent, false)
        return CardViewHolder(view)

    }


    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {

        val post = postsList[position]
        val b = holder.view

        val imageUrl=post.postImage

        Picasso.get().load(post.postImage).into(b.postImage)

        if (post.description == "") {
            b.description.visibility = GONE

        } else {
            b.description.visibility = VISIBLE
            b.description.text = post.description

        }
        publisherInfo(b.profileImage, b.userName, b.publisher, post.publisher)


    }


    override fun getItemCount(): Int {
        return postsList.size
    }


    private fun publisherInfo(
        profil_image: ImageView,
        username: TextView,
        publisher: TextView,
        userId: String
    ) {
        firestore.collection("user").document(userId).addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(mContext, error.localizedMessage, Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            } else {
                if (value != null && value.exists()) {
                    val imageUrl = value["image_url"] as String
                    val userName = value["username"] as String
                    Picasso.get().load(imageUrl).into(profil_image)
                    username.text = userName
                    publisher.text = userName


                } else {
                    Toast.makeText(mContext, "User not found", Toast.LENGTH_SHORT).show()
                }


            }

        }

    }

    private fun isLike(postId:String,imageView: ImageView){

        firestore.collection("Likes").document(postId).addSnapshotListener { value, error ->

            if (error != null) {
                Toast.makeText(mContext, error.localizedMessage, Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            } else {
                if (value != null ) {
                    val doc=value.data as? HashMap<*, *>
                    if (doc != null) {
                        for (i in doc){

                        }
                    }



                } else {
                    Toast.makeText(mContext, "User not found", Toast.LENGTH_SHORT).show()
                }


            }


        }




    }
}