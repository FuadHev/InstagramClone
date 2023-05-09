package com.example.instagramclone.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.model.Comment
import com.example.instagramclone.model.Users
import com.example.instagramclone.databinding.CommentItemBinding
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlin.collections.ArrayList

class CommentAdapter(
    private val mContext: Context,
    private var commentlist: List<Comment>,
    var allPublisherList:List<Users>
) :
    RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    private lateinit var firbaseUser: FirebaseUser
    private var firestore = Firebase.firestore

    inner class ViewHolder(val view: CommentItemBinding) : RecyclerView.ViewHolder(view.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: CommentItemBinding = CommentItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return commentlist.size
    }


    fun updateElements(newCommentList: ArrayList<Comment>) {
        this.commentlist = newCommentList
        notifyDataSetChanged()

//       val callBack=MyDiffUtilCallBack(this.commentlist,newCommentList)
//        val difference=DiffUtil.calculateDiff(callBack)
//        difference.dispatchUpdatesTo(this)
    }
    fun updatePublisher(newPublisherList:List<Users>){
        this.allPublisherList=newPublisherList
        notifyDataSetChanged()
    }

    @SuppressLint("CommitTransaction", "SuspiciousIndentation")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = commentlist[position]
        val b = holder.view
        val user=allPublisherList.find {
            it.user_id==comment.publiser
        }
        Picasso.get().load(user?.imageurl).into(b.imageProfile)
        b.username.text=user?.username
        b.comment.text = comment.comment


    }





}