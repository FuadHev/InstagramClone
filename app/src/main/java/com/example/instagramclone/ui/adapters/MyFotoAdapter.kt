package com.example.instagramclone.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.data.entity.Posts
import com.example.instagramclone.databinding.FotosItemBinding
import com.example.instagramclone.databinding.PostsCardViewBinding
import com.squareup.picasso.Picasso

class MyFotoAdapter(private val mCOntext:Context,private val postList:ArrayList<Posts>):RecyclerView.Adapter<MyFotoAdapter.CardViewHolder>() {


    inner class CardViewHolder(val view: FotosItemBinding) : RecyclerView.ViewHolder(view.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: FotosItemBinding = FotosItemBinding.inflate(layoutInflater, parent, false)
        return CardViewHolder(view)

    }

    override fun getItemCount(): Int {

         return postList.size
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val post=postList[position]
        val b=holder.view

        Picasso.get().load(post.postImage).into(b.postImage)



    }
}