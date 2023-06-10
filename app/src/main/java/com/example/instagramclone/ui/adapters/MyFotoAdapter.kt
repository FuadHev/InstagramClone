package com.example.instagramclone.ui.adapters

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.R
import com.example.instagramclone.model.Posts
import com.example.instagramclone.databinding.FotosItemBinding
import com.squareup.picasso.Picasso

class MyFotoAdapter(private val postClickListener: MyPostCLickListener,private var postList:List<Posts>):RecyclerView.Adapter<MyFotoAdapter.CardViewHolder>() {


    inner class CardViewHolder(val view: FotosItemBinding) : RecyclerView.ViewHolder(view.root)

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

        Picasso.get().load(post.postImage).resize(1280,1070).centerCrop().into(b.postImage)


        b.postImage.setOnClickListener {

            // deyise bilerem eslinde butun listi oturmek istemirem ama lazim ola biler
            val bundle=Bundle()
            bundle.putParcelableArrayList("posts",postList as java.util.ArrayList<out Parcelable>)
            bundle.putInt("position",position)
            postClickListener.postClickListener(post,bundle)
//            Navigation.findNavController(it).navigate(R.id.action_profilfragment_to_profileDetailFragment,bundle)


        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateMyPosts(newPostList:List<Posts>){
        this.postList=newPostList
        notifyDataSetChanged()

    }

}
interface MyPostCLickListener{
    fun postClickListener(currentPosts: Posts,bundle: Bundle)
}
