package com.example.instagramclone.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.data.entity.Comment
import com.example.instagramclone.databinding.CommentItemBinding
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class CommentAdapter(private val mContext:Context ,private val commentlist: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    private lateinit var firbaseUser: FirebaseUser
    private var firestore=Firebase.firestore

    inner class ViewHolder(val view: CommentItemBinding) : RecyclerView.ViewHolder(view.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: CommentItemBinding = CommentItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return commentlist.size
    }

    @SuppressLint("CommitTransaction")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = commentlist[position]
        val b = holder.view

        b.comment.text=comment.comment
        getUserInfo(b.imageProfile,b.username,comment.publiser)


        b.imageProfile.setOnClickListener {

//            val intent=Intent(mContext,ProfilActivity::class.java)
//            intent.putExtra("publisherId",comment.publiser)
//            mContext.startActivity(intent)


        }

    }

    private fun getUserInfo(imageView:ImageView,username:TextView,publisherId:String){
        firestore.collection("user").document(publisherId).addSnapshotListener { value, error ->
            if (error!=null){
                error.localizedMessage?.let { Log.e("error", it) }
            }else{
                if (value!=null&&value.exists()){
                    val imageUrl = value["image_url"] as String
                    val userName = value["username"] as String
                    Picasso.get().load(imageUrl).into(imageView)
                    username.text=userName

                }
            }


        }


    }


}