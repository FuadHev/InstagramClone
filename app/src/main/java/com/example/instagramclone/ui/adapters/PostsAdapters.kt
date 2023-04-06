package com.example.instagramclone.ui.adapters

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
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
import com.onesignal.OneSignal
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject
import java.util.UUID
import java.util.logging.Handler

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


    @SuppressLint("Recycle")
    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {

        val post = postsList[position]
        val b = holder.view
        b.like.setImageResource(R.drawable.heart_noselected)
        b.comments.text = "View all 0 comments"

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
        isSaved(post.post_id, b.save)

        var i = 0
        b.postImage.setOnClickListener {
            ++i

            android.os.Handler(Looper.getMainLooper()).postDelayed({
                if (i == 2) {
                    likePost(b.like, post.post_id, post.publisher, b.likeCount, b.likeAnim,post.postImage)
                }

                i = 0
            }, 500)


        }




        b.like.setOnClickListener {

            likePost(b.like, post.post_id, post.publisher, b.likeCount, b.likeAnim,post.postImage)

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
                    .update(post.post_id, FieldValue.delete())


            }

        }


    }

    private fun likePost(
        likeBtn: ImageView,
        postId: String,
        postPublisher: String,
        likeCount: TextView,
        likeImage: ImageView,
        imageUrl: String
    ) {
        if (likeBtn.tag == "like") {

            val map = hashMapOf<String, Boolean>()

            map[firebaseUser!!.uid] = true
            firestore.collection("Likes").document(postId).set(map, SetOptions.merge())
                .addOnSuccessListener {


                    likeAnimation(likeImage)
                    nrLike(likeCount, postId)
                    likeBtn.setImageResource(R.drawable.like)
                    likeBtn.tag = "liked"
                    if (postPublisher != firebaseUser.uid) {
                        addNotification(postPublisher, postId)
                        getPlayerIdSendNotification(postPublisher,imageUrl)
                    }

                }

        } else {
            val ref = firestore.collection("Likes").document(postId)


            val updates = hashMapOf<String, Any>(
                firebaseUser!!.uid to FieldValue.delete()
            )

           ref.update(updates).addOnSuccessListener {
                nrLike(likeCount, postId)
                likeBtn.setImageResource(R.drawable.heart_noselected)
                likeBtn.tag = "like"
            }


        }


    }

    fun likeAnimation(likeImage: ImageView) {
        likeImage.visibility = VISIBLE
        val scaleanimX = ObjectAnimator.ofFloat(likeImage, "scaleX", 1.0f, 1.5f)
        val scaleanimY = ObjectAnimator.ofFloat(likeImage, "scaleY", 1.0f, 1.5f)
        val scaleanimAlpha = ObjectAnimator.ofFloat(likeImage, "alpha", 0.0f, 1.0f)


        val animation = AnimatorSet().apply {
            duration = 1200
            playTogether(scaleanimX, scaleanimY, scaleanimAlpha)
        }
        animation.start()
        android.os.Handler().postDelayed({
            likeImage.visibility = View.INVISIBLE
        }, 2000)



    }


    fun updatePosts(newPostsList: ArrayList<Posts>) {
        this.postsList = newPostsList
        notifyDataSetChanged()
    }

    private fun addNotification(userId: String, postId: String) {
        val ref = firestore.collection("Notification").document(userId)
        val nKey = UUID.randomUUID()
        val notification = hashMapOf<String, Any>()
        val notifi = hashMapOf<String, Any>()
        notifi["userId"] = firebaseUser!!.uid
        notifi["nText"] = "Liked your Post"
        notifi["postId"] = postId
        notifi["isPost"] = true
        notifi["notificationId"] = nKey.toString()
        notifi["time"] = com.google.firebase.Timestamp.now()

        notification[nKey.toString()] = notifi

        ref.set(notification, SetOptions.merge())

    }

    private fun getPlayerIdSendNotification(postPublisher: String, imageUrl: String) {


        var username = ""
        firestore.collection("user").document(firebaseUser!!.uid)
            .addSnapshotListener { value, error ->
                if (error != null) {

                } else {
                    if (value != null) {
                        username = value.get("username") as String
                    }
                }
            }
        firestore.collection("user").document(postPublisher).addSnapshotListener { value, error ->
            if (error != null) {

            } else {
                if (value != null) {
                    val playerId = value.get("playerId") as String?

                    if (playerId != null) {
                        sentPushNotification(playerId, username, imageUrl)
                    }

                }
            }
        }

    }

    private fun sentPushNotification(playerId: String, username: String, imageUrl: String) {
        try {
//            OneSignal.postNotification(
//                JSONObject(
//                    """{
//          "contents": {"en": "Liked Your Post"},
//          "include_player_ids": ["$playerId"],
//          "headings": {"en": "$username"},
//        "image_url": "https://firebasestorage.googleapis.com/v0/b/instagramclone-9f5ee.appspot.com/o/images%2F0756c3f1-5545-4716-b4e4-174e564031c7.jpg?alt=media&token=9674c6a4-76b2-41a2-ac18-8dcec6e5a3ac"
//                  }
//        """.trimIndent()
//                ),
//                null
//            )

            /*paylasmadan  qabaq appId deyismelidi*/
            OneSignal.postNotification(
                JSONObject(
                    """{
        "app_id": "9b3b9701-9264-41ef-b08c-1c69f1fabfef", 
        "include_player_ids": ["$playerId"],
        "headings": {"en": "$username"},
        "contents": {"en": "Liked Your Post"},
        "big_picture": "$imageUrl"
    }"""
                ),
                null
            )


//            OneSignal.postNotification(
//                JSONObject("{'contents': {'en':'$username : Liked Your Post'}, 'include_player_ids': ['$playerId']}"),
//                null
//            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    @SuppressLint("SetTextI18n")
    private fun getComments(postId: String, comments: TextView) {


        firestore.collection("Comments").document(postId).addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(mContext, error.localizedMessage, Toast.LENGTH_SHORT).show()
                comments.text = "View all 0 comments"
            } else {
                try {
                    if (value != null) {
                        val allcomments = value.data as? HashMap<*,*>

                        val count = allcomments?.count()

                        if (count == null) {
                            comments.text = "View all 0 comments"
                        } else {
                            comments.text = "View all $count comments"
                        }
//                        Log.e("countcomment",count.toString())
//                        comments.text = "View all $count comments"

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
                        imageView.tag = "save"
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