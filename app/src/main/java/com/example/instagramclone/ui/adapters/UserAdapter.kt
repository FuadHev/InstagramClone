package com.example.instagramclone.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
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
import com.example.instagramclone.R
import com.example.instagramclone.data.entity.Users
import com.example.instagramclone.databinding.UsersItemBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore

import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class UserAdapter(val clickListener: ClickListener,var usersList: ArrayList<Users>) :
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
        isFollowing(user.user_id,v.follow)
        v.username.text=user.username
        val url=user.imageurl
        Picasso.get().load(url).into(v.profileImage)

        v.cardView.setOnClickListener {

//            val editor=mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
//            editor.putString("profileid",user.user_id)
//            editor.apply()

            val bundle= Bundle()

            bundle.putString("profileid",user.user_id)

            clickListener.userClickListener(bundle)
//            Navigation.findNavController(it).navigate(R.id.action_searctoFragment_to_search_nav,bundle)

        }
        v.follow.setOnClickListener {


            val following = hashMapOf<String, HashMap<String, Boolean>>()
            val id = hashMapOf<String, Boolean>()
            id[user.user_id]=true
            following["following"] = id

            val follower = hashMapOf<String, HashMap<String, Boolean>>()
            val id2 = hashMapOf<String, Boolean>()
            id2[auth.currentUser!!.uid]=true
            follower["followers"] = id2
            if (v.follow.text.toString().toLowerCase(Locale.ROOT).trim() == "follow") {
                firestore.collection("Follow").document(auth.currentUser!!.uid).set(following,SetOptions.merge())
                firestore.collection("Follow").document(user.user_id).set(follower, SetOptions.merge())
                v.follow.text="following"


            } else {

                firestore.collection("Follow").document(auth.currentUser!!.uid).update("following.${user.user_id}",
                    FieldValue.delete())
                firestore.collection("Follow").document(user.user_id).update("followers.${auth.currentUser!!.uid}",
                    FieldValue.delete())

                v.follow.text="follow"

            }


        }


    }

    @SuppressLint("SetTextI18n")
    private fun isFollowing(userId: String, button: Button) {

        val firebaseUser = Firebase.auth.currentUser

        val firestore = Firebase.firestore
        firestore.collection("Follow").document(firebaseUser!!.uid)
            .addSnapshotListener { documentSnapshot, error ->
                if(error!=null){
                    error.localizedMessage?.let { Log.e("", it)
                    return@addSnapshotListener}
                }else{
                    if (documentSnapshot!=null&&documentSnapshot.exists()){
                        val follow=documentSnapshot.data

                        if (follow != null) {

                            try {
                                val following= follow["following"] as HashMap<*,*>
                                for (i in following){
                                    if (i.key==userId){
                                        button.text="following"
                                    }
                                }
                            }catch (_:java.lang.NullPointerException){

                            }

                        }

                    }else{
                        Log.e("","")
                    }
                }
            }
    }


    override fun getItemCount(): Int {
        return usersList.size

    }



}

interface ClickListener{
    fun userClickListener(bundle:Bundle)
}
