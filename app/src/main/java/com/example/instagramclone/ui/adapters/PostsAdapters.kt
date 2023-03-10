package com.example.instagramclone.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.CommentsActivity
import com.example.instagramclone.R
import com.example.instagramclone.data.entity.Posts
import com.example.instagramclone.databinding.PostsCardViewBinding

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class PostsAdapters(val mContext: Context, var postsList: List<Posts>) :
    RecyclerView.Adapter<PostsAdapters.CardViewHolder>() {

    val firebaseUser = Firebase.auth.currentUser
    val firestore = Firebase.firestore


    inner class CardViewHolder(val view: PostsCardViewBinding) :
        RecyclerView.ViewHolder(view.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: PostsCardViewBinding = PostsCardViewBinding.inflate(layoutInflater, parent, false)
        return CardViewHolder(view)

    }


    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {

        val post = postsList[position]
        val b = holder.view


        b.like.tag = "like"
        Picasso.get().load(post.postImage).into(b.postImage)

        if (post.description == "") {
            b.description.visibility = GONE

        } else {
            b.description.visibility = VISIBLE
            b.description.text = post.description

        }
        publisherInfo(b.profileImage, b.userName, b.publisher, post.publisher)
        isLiked(post.post_id, b.like)
        nrLike(b.likeCount, post.post_id)
        getComments(post.post_id, b.comments)
        isSaved(post.post_id,b.save)


        b.like.setOnClickListener {


            if (b.like.tag == "like") {

                val map = hashMapOf<String, Boolean>()

                map[firebaseUser!!.uid] = true
                firestore.collection("Likes").document(post.post_id).set(map, SetOptions.merge())
                    .addOnSuccessListener {

                        nrLike(b.likeCount, post.post_id)
                        b.like.setImageResource(R.drawable.like)
                        b.like.tag = "liked"
                    }


            } else {
                val docRef = firestore.collection("Likes").document(post.post_id)


                val updates = hashMapOf<String, Any>(
                    firebaseUser!!.uid to FieldValue.delete()
                )

                docRef.update(updates).addOnSuccessListener {
                    nrLike(b.likeCount, post.post_id)
                    b.like.setImageResource(R.drawable.heart_noselected)
                    b.like.tag = "like"
                }


            }


        }

        b.comments.setOnClickListener {


            val intent = Intent(mContext, CommentsActivity::class.java)
            intent.putExtra("postId", post.post_id)
            intent.putExtra("publisherId", post.publisher)
            mContext.startActivity(intent)

        }
        b.comment.setOnClickListener {


            val intent = Intent(mContext, CommentsActivity::class.java)
            intent.putExtra("postId", post.post_id)
            intent.putExtra("publisherId", post.publisher)
            mContext.startActivity(intent)

        }

        b.save.setOnClickListener {
            if (b.save.tag == "save") {
                val hmap = hashMapOf(
                    post.post_id to true
                )
                firestore.collection("Saves").document(firebaseUser!!.uid)
                    .set(hmap, SetOptions.merge())

            } else {
                firestore.collection("Saves").document(firebaseUser!!.uid)
                    .update("${post.post_id}", FieldValue.delete())


            }

        }


    }

    fun updatePosts(newPostsList: ArrayList<Posts>) {
        this.postsList = newPostsList
        notifyDataSetChanged()

    }

    @SuppressLint("SetTextI18n")
    private fun getComments(postId: String, comments: TextView) {


        firestore.collection("Comments").document(postId).addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(mContext, error.localizedMessage, Toast.LENGTH_SHORT).show()
            } else {
                try {
                    if (value != null) {


                        val allcomments = value.data as HashMap<*, *>
                        val count = allcomments.count()
                        comments.text = "View all $count comments"

                    }
                } catch (_: NullPointerException) {

                }


            }


        }


    }


    override fun getItemCount(): Int {
        return postsList.size
    }


    @SuppressLint("SetTextI18n")
    private fun nrLike(likes: TextView, postId: String) {


        firestore.collection("Likes").document(postId).addSnapshotListener { value, error ->

            if (error != null) {
                Toast.makeText(mContext, error.localizedMessage, Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            } else {
                if (value != null) {
                    val doc = value.data as? HashMap<*, *>
                    val count = doc?.count()
                    if (count == null) {
                        likes.text = "0 likes"
                    } else {
                        likes.text = "$count likes"
                    }


                } else {
                    likes.text = "0 likes"
                }


            }

        }


    }

    private fun isLiked(postId: String, imageView: ImageView) {

        firestore.collection("Likes").document(postId).addSnapshotListener { value, error ->

            if (error != null) {
                Toast.makeText(mContext, error.localizedMessage, Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            } else {
                if (value != null) {
                    val doc = value.data as? HashMap<*, *>
                    if (doc != null) {

                        if (doc.containsKey(firebaseUser!!.uid)) {
                            imageView.setImageResource(R.drawable.like)
                            imageView.tag = "liked"
                        } else {
                            imageView.setImageResource(R.drawable.heart_noselected)
                            imageView.tag = "like"
                        }


                    }


                } else {
                    Toast.makeText(mContext, "", Toast.LENGTH_SHORT).show()
                }


            }


        }


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

    private fun isSaved(postId: String, imageView: ImageView) {
        val ref = firestore.collection("Saves").document(firebaseUser!!.uid)

        ref.addSnapshotListener { value, error ->
            if (error != null) {
                error.localizedMessage?.let { Log.e("error", it) }
            } else {
                if (value != null) {
                    if (value.contains(postId)) {
                        imageView.setImageResource(R.drawable.is_saved)
                        imageView.tag = "saved"
                    } else {
                        imageView.setImageResource(R.drawable.bookmark)
                        imageView.tag="save"
                    }
                }

            }
        }


    }

    override fun onViewAttachedToWindow(holder: CardViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.adapterPosition

    }

}