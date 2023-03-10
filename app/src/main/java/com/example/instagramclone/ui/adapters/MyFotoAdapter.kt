package com.example.instagramclone.ui.adapters

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.R
import com.example.instagramclone.data.entity.Posts
import com.example.instagramclone.databinding.FotosItemBinding
import com.example.instagramclone.ui.fragments.ProfileFragmentDirections
import com.squareup.picasso.Picasso

class MyFotoAdapter(private var postList:List<Posts>):RecyclerView.Adapter<MyFotoAdapter.CardViewHolder>() {


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

        Picasso.get().load(post.postImage).resize(1980,1720).centerCrop().into(b.postImage)


        b.postImage.setOnClickListener {

            val bundle=Bundle()
            bundle.putParcelableArrayList("posts",postList as java.util.ArrayList<out Parcelable>)
            bundle.putInt("position",position)



            Navigation.findNavController(it).navigate(R.id.action_profilfragment_to_profileDetailFragment,bundle)


        }
    }

    fun updateMyPosts(newPostList:ArrayList<Posts>){
        this.postList=newPostList
        notifyDataSetChanged()

    }



}
