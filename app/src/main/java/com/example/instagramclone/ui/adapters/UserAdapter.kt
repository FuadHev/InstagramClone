package com.example.instagramclone.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.R
import com.example.instagramclone.model.Users
import com.example.instagramclone.databinding.UsersItemBinding
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
import java.util.*
import kotlin.collections.HashMap


class UserAdapter(
    var mContext: Context,
    private val clickListener: ClickListener,
    private var usersList:List<Users>
) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    inner class ViewHolder(val view: UsersItemBinding) : RecyclerView.ViewHolder(view.root)

    val auth = Firebase.auth
    val firestore = Firebase.firestore
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: UsersItemBinding = UsersItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = usersList[position]
        val v = holder.view
        v.follow.visibility = VISIBLE
        if (user.user_id == auth.currentUser!!.uid) {
            v.follow.visibility = GONE
        }


        isFollowing(user.user_id, v.follow)

        v.username.text = user.username
        val url = user.imageurl
        Picasso.get().load(url).into(v.profileImage)

        v.cardView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("profileid", user.user_id)
            clickListener.userClickListener(bundle)

        }
        v.follow.setOnClickListener {
            followClickListener(user,v.follow)
        }


    }
    private fun followClickListener(user:Users,follow:Button){
        val following = hashMapOf<String, HashMap<String, Boolean>>()
        val id = hashMapOf<String, Boolean>()
        id[user.user_id] = true
        following["following"] = id

        val follower = hashMapOf<String, HashMap<String, Boolean>>()
        val id2 = hashMapOf<String, Boolean>()
        id2[auth.currentUser!!.uid] = true
        follower["followers"] = id2
        if (follow.text.toString().lowercase().trim() == "follow") {
            firestore.collection("Follow").document(auth.currentUser!!.uid)
                .set(following, SetOptions.merge())
            firestore.collection("Follow").document(user.user_id)
                .set(follower, SetOptions.merge())
            if(user.user_id!=Firebase.auth.currentUser!!.uid){
                addNotification(user.user_id)
            }
            follow.setText(R.string.following)

            getPlayerIdSendNotification(user.user_id)
        } else {

            firestore.collection("Follow").document(auth.currentUser!!.uid).update(
                "following.${user.user_id}",
                FieldValue.delete()
            )
            firestore.collection("Follow").document(user.user_id).update(
                "followers.${auth.currentUser!!.uid}",
                FieldValue.delete()
            )
            follow.setText(R.string.follow)
        }
    }

    private fun getPlayerIdSendNotification(userId: String) {

        firestore.collection("user").document(Firebase.auth.currentUser!!.uid).get()

            .addOnSuccessListener { value ->
                    if (value != null) {
                        val username = value.get("username") as String
                        val profileImage = value.get("image_url") as String

                        firestore.collection("user").document(userId)
                            .get().addOnSuccessListener { userValue->
                                if (userValue != null) {
                                    val playerId = userValue.get("playerId") as String?
                                    if (playerId != null) {
                                        sentPushNotification(playerId, username,profileImage)
                                    }

                                }

                            }


                    }


            }


    }

    private fun sentPushNotification(playerId: String, username: String,profileImage:String) {
        try {
//                OneSignal.postNotification(
//                    JSONObject(
//                        """{
//          "contents": {"en": "started following you"},
//          "include_player_ids": ["$playerId"],
//          "headings": {"en": "$username"}
//                  }
//        """.trimIndent()
//                    ),
//                    null
//                )

            val notificationContent = JSONObject(
                """{
        "app_id": "${Constant.APP_ID}", 
        "include_player_ids": ["$playerId"],
        "headings": {"en": "$username"},
        "contents": {"en": "started following you"},
        "small_icon": "mipmap/ic_launcher_instalife",
        "large_icon": "$profileImage"
    }"""
            )
            OneSignal.postNotification(notificationContent, null)

        } catch (e: JSONException) {
            e.printStackTrace()
        }


    }


    private fun addNotification(userId: String) {

        if(userId!=Firebase.auth.currentUser!!.uid){
            val ref = Firebase.firestore.collection("Notification").document(userId)
            val nKey = UUID.randomUUID()
            val notification = hashMapOf<String, Any>()
            val notifi = hashMapOf<String, Any>()
            notifi["userId"] = Firebase.auth.currentUser!!.uid
            notifi["nText"] = "started following you "
            notifi["postId"] = ""
            notifi["isPost"] = false
            notifi["notificationId"]=nKey.toString()
            notifi["time"] = com.google.firebase.Timestamp.now()

            notification[nKey.toString()] = notifi

            ref.set(notification, SetOptions.merge())

        }


    }

    @SuppressLint("SetTextI18n")
    private fun isFollowing(userId: String, button: Button) {

        val firebaseUser = Firebase.auth.currentUser
        val firestore = Firebase.firestore
        firestore.collection("Follow").document(firebaseUser!!.uid)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    error.localizedMessage?.let {
                        Log.e("", it)
                        return@addSnapshotListener
                    }
                }
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val follow = documentSnapshot.data
                        if (follow != null) {
                            try {
                                val following = follow["following"] as HashMap<*, *>

                                if (following.containsKey(userId)) {
                                    button.setText(R.string.following)
                                } else {
                                    button.setText(R.string.follow)
                                }


                            } catch (_: java.lang.NullPointerException) {

                            }
                        }
                    } else {
                        Log.e("", "")
                    }

            }
    }


    override fun getItemCount(): Int {
        return usersList.size

    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateUsers(newList: List<Users>) {
        this.usersList = newList
        notifyDataSetChanged()

    }


}

interface ClickListener {
    fun userClickListener(bundle: Bundle)
}