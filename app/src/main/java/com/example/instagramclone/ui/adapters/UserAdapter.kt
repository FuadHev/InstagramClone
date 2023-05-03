package com.example.instagramclone.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.data.entity.Users
import com.example.instagramclone.databinding.UsersItemBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore

import com.google.firebase.ktx.Firebase
import com.onesignal.OSNotification
import com.onesignal.OneSignal
import com.squareup.picasso.Picasso
import kotlinx.coroutines.NonDisposableHandle.parent
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import android.util.Base64


class UserAdapter(
    var mContext: Context,
    private val clickListener: ClickListener,
    var usersList: ArrayList<Users>
) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    inner class ViewHolder(val view: UsersItemBinding) : RecyclerView.ViewHolder(view.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: UsersItemBinding = UsersItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = usersList[position]
        val v = holder.view
        val auth = Firebase.auth
        val firestore = Firebase.firestore
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

            val following = hashMapOf<String, HashMap<String, Boolean>>()
            val id = hashMapOf<String, Boolean>()
            id[user.user_id] = true
            following["following"] = id

            val follower = hashMapOf<String, HashMap<String, Boolean>>()
            val id2 = hashMapOf<String, Boolean>()
            id2[auth.currentUser!!.uid] = true
            follower["followers"] = id2
            if (v.follow.text.toString().lowercase().trim() == "follow") {
                firestore.collection("Follow").document(auth.currentUser!!.uid)
                    .set(following, SetOptions.merge())
                firestore.collection("Follow").document(user.user_id)
                    .set(follower, SetOptions.merge())
                if(user.user_id!=Firebase.auth.currentUser!!.uid){
                    addNotification(user.user_id)
                }

                v.follow.text = "following"

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
                v.follow.text = "follow"
            }


        }


    }

    private fun getPlayerIdSendNotification(userId: String) {


        var username = ""
        Firebase.firestore.collection("user").document(Firebase.auth.currentUser!!.uid)
            .addSnapshotListener { value, error ->
                if (error != null) {

                } else {
                    if (value != null) {
                        username = value.get("username") as String
                    }
                }
            }
        Firebase.firestore.collection("user").document(userId).addSnapshotListener { value, error ->
            if (error != null) {
                error.localizedMessage?.let { Log.e("userError", it) }

            } else {
                if (value != null) {

                    val playerId = value.get("playerId") as String?
                    if (playerId != null) {
                        sentPushNotification(playerId, username)
                    }

                }
            }
        }

    }

    private fun sentPushNotification(playerId: String, username: String) {
        try {

// foto gondermeye calisirdim olmadi helelik
// OneSignal API ile push bildirimi oluşturun.
//            val notification = JSONObject()
//            notification.put("contents", JSONObject()
//                .put("en", "Bildirim İçeriği")) // Bildirim içeriği
//            notification.put("headings", JSONObject()
//                .put("en", "Bildirim Başlığı")) // Bildirim başlığı
//            notification.put("include_player_ids", JSONArray()
//                .put(playerId)) // Hedef kullanıcının OneSignal ID'si
//            notification.put("big_picture", photoUrl)
//
//            OneSignal.postNotification(notification,null)

            if(username!=Firebase.auth.currentUser!!.uid){
                OneSignal.postNotification(
                    JSONObject(
                        """{
          "contents": {"en": "started following you"},
          "include_player_ids": ["$playerId"],
          "headings": {"en": "$username"}
                  }
        """.trimIndent()
                    ),
                    null
                )
//            OneSignal.postNotification(
//                JSONObject("{'contents': {'en':'$username : started following you'},{'headings': {'en': Notification Title'}, 'include_player_ids': ['$playerId']}"),
//                null
//            )
            }


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
                } else {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val follow = documentSnapshot.data
                        if (follow != null) {
                            try {
                                val following = follow["following"] as HashMap<*, *>

                                if (following.containsKey(userId)) {
                                    button.text = "following"
                                } else {
                                    button.text = "follow"
                                }


                            } catch (_: java.lang.NullPointerException) {

                            }
                        }
                    } else {
                        Log.e("", "")
                    }
                }
            }
    }


    override fun getItemCount(): Int {
        return usersList.size

    }

    fun updateUsers(newList: ArrayList<Users>) {
        this.usersList = newList
        notifyDataSetChanged()

    }


}

interface ClickListener {
    fun userClickListener(bundle: Bundle)
}