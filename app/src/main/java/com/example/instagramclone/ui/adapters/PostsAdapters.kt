package com.example.instagramclone.ui.adapters

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
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
import com.example.instagramclone.R
import com.example.instagramclone.model.Posts
import com.example.instagramclone.databinding.PostsCardViewBinding
import com.example.instagramclone.utils.Constant
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.onesignal.OneSignal
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class PostsAdapters(
    private val postclickListener: PostClickListener,
    private val mContext: Context,
    private var postsList: List<Posts>
) :
    RecyclerView.Adapter<PostsAdapters.CardViewHolder>() {

    private val firebaseUser = Firebase.auth.currentUser
    private val firestore = Firebase.firestore


    inner class CardViewHolder(val view: PostsCardViewBinding) :
        RecyclerView.ViewHolder(view.root)

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
//        b.comments.text = "View all 0 comments"
        b.like.tag = "like"
        Picasso.get().load(post.postImage).into(b.postImage)

        if (post.publisher == Firebase.auth.currentUser!!.uid) {
            b.postOption.visibility = VISIBLE
        }

        if (post.description.trim() == "") {
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

        val timestamp = post.time // Firestore'dan aldığınız timestamp
        val date = timestamp.toDate()
        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.US)
        val formattedDate: String = dateFormat.format(date)
        b.time.text=formattedDate

        b.postOption.setOnClickListener {
            postclickListener.postOptionCLickListener(post.post_id, it)
        }

        b.userName.setOnClickListener {

            goToProfile(post.publisher)
        }
        b.profileImage.setOnClickListener {

            goToProfile(post.publisher)
        }

        var i = 0
        b.postImage.setOnClickListener {
            ++i
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                if (i == 2) {
                    likePost(
                        b.like,
                        post.post_id,
                        post.publisher,
                        b.likeCount,
                        b.likeAnim,
                        post.postImage
                    )
                }
                i = 0
            }, 500)


        }
        b.like.setOnClickListener {
            likePost(b.like, post.post_id, post.publisher, b.likeCount, b.likeAnim, post.postImage)
        }

        b.comments.setOnClickListener {
            postclickListener.commentsClickListener(post.post_id, post.publisher)

        }
        b.comment.setOnClickListener {
            postclickListener.commentsClickListener(post.post_id, post.publisher)
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

    private fun goToProfile(publisher: String) {
        val bundle = Bundle()

        bundle.putString("profileid", publisher)

        postclickListener.pImageuNameClickListener(bundle)
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
                    //artiq ehtiyac yoxdur
//                    nrLike(likeCount, postId)
//                    likeBtn.setImageResource(R.drawable.like)
//                    likeBtn.tag = "liked"
                    if (postPublisher != firebaseUser.uid) {
                        addNotification(postPublisher, postId)
                        getPlayerIdSendNotification(postPublisher, imageUrl)
                    }
                }

        } else {
            val ref = firestore.collection("Likes").document(postId)
            val updates = hashMapOf<String, Any>(
                firebaseUser!!.uid to FieldValue.delete()
            )
            ref.update(updates).addOnSuccessListener {
//                nrLike(likeCount, postId)
//                likeBtn.setImageResource(R.drawable.heart_noselected)
//                likeBtn.tag = "like"
            }


        }


    }

    private fun likeAnimation(likeImage: ImageView) {
        likeImage.visibility = VISIBLE
        val scaleanimX = ObjectAnimator.ofFloat(likeImage, "scaleX", 1.0f, 1.5f)
        val scaleanimY = ObjectAnimator.ofFloat(likeImage, "scaleY", 1.0f, 1.5f)
        val scaleanimAlpha = ObjectAnimator.ofFloat(likeImage, "alpha", 0.0f, 1.0f)
        val animation = AnimatorSet().apply {
            duration = 1200
            playTogether(scaleanimX, scaleanimY, scaleanimAlpha)
        }
        animation.start()
        android.os.Handler(Looper.getMainLooper()).postDelayed({
            likeImage.visibility = View.INVISIBLE
        }, 1800)
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updatePosts(newPostsList: List<Posts>) {
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
        firestore.collection("user").document(firebaseUser!!.uid).get()
            .addOnSuccessListener { value ->
                if (value != null) {
                    username = value.get("username") as String
                }

            }
        firestore.collection("user").document(postPublisher).get().addOnSuccessListener { value ->
            if (value != null) {
                val playerId = value.get("playerId") as String?

                if (playerId != null) {
                    sentPushNotification(playerId, username, imageUrl)
                }
            }

        }

    }

    private fun sentPushNotification(playerId: String, username: String, imageUrl: String) {
        try {
            /*paylasmadan  qabaq appId gizli saxla saxlanilmalidi*/
            OneSignal.postNotification(
                JSONObject(
                    """{
        "app_id": "${Constant.APP_ID}", 
        "include_player_ids": ["$playerId"],
        "headings": {"en": "$username"},
        "contents": {"en": "Liked Your Post"},
        "small_icon": "mipmap/ic_launcher_instalife",
        "big_picture": "$imageUrl"
    }"""
                ),
                null
            )

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
                return@addSnapshotListener
            }
            try {
                if (value != null) {
                    val allcomments = value.data as? HashMap<*, *>

                    val count = allcomments?.count()

                    if (count == null) {
                        val viewAllComments =
                            mContext.getString(R.string.view_all_comments, 0.toString())
                        comments.text = viewAllComments
                    } else {

                        val viewAllComments =
                            mContext.getString(R.string.view_all_comments, count.toString())
                        comments.text = viewAllComments

//                            comments.text = "View all $count comments"
                    }
//                        Log.e("countcomment",count.toString())
//                        comments.text = "View all $count comments"

                }
            } catch (e: NullPointerException) {

                Log.e("Comments_Error", e.localizedMessage!! )
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
            }
            if (value != null) {
                val doc = value.data as? HashMap<*, *>
                val count = doc?.count()
                if (count == null) {
                    val viewLikesCount = mContext.getString(R.string.likes_count, 0.toString())
                    likes.text = viewLikesCount
                } else {
                    val viewLikesCount = mContext.getString(R.string.likes_count, count.toString())
                    likes.text = viewLikesCount
                }

            } else {
                val viewLikesCount = mContext.getString(R.string.likes_count, 0.toString())
                likes.text = viewLikesCount
            }

        }


    }

    private fun isLiked(postId: String, imageView: ImageView) {

        firestore.collection("Likes").document(postId).addSnapshotListener { value, error ->

            if (error != null) {
                Toast.makeText(mContext, error.localizedMessage, Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
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
                Log.e("isLike_error", "")
            }


        }


    }

    private fun publisherInfo(
        profil_image: ImageView,
        username: TextView,
        publisher: TextView,
        userId: String
    ) {
        firestore.collection("user").document(userId).get().addOnSuccessListener { value ->
            if (value != null && value.exists()) {
                val imageUrl = value["image_url"] as String
                val userName = value["username"] as String
                Picasso.get().load(imageUrl).into(profil_image)
                username.text = userName
                publisher.text = userName
            } else {
                Toast.makeText(mContext, "User not found", Toast.LENGTH_SHORT).show()
            }

        }.addOnFailureListener {
            Log.e("publisher_Info", it.localizedMessage!!)
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

interface PostClickListener {
    fun pImageuNameClickListener(bundle: Bundle)

    fun postOptionCLickListener(postId: String, view: View)

    fun commentsClickListener(postId: String, publisherId: String)
}