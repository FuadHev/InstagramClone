package com.example.instagramclone.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.model.Comment
import com.example.instagramclone.model.Users
import com.example.instagramclone.databinding.CommentItemBinding
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.log

class CommentAdapter(private val commentClickListener: CommentClickListener,
    private val mContext: Context,
    private var commentlist: List<Comment>,
    var allPublisherList:List<Users>
) :
    RecyclerView.Adapter<CommentAdapter.ViewHolder>() {



    inner class ViewHolder(val view: CommentItemBinding) : RecyclerView.ViewHolder(view.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: CommentItemBinding = CommentItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return commentlist.size
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateElements(newCommentList: ArrayList<Comment>) {
        this.commentlist = newCommentList
        notifyDataSetChanged()

//       val callBack=MyDiffUtilCallBack(this.commentlist,newCommentList)
//        val difference=DiffUtil.calculateDiff(callBack)
//        difference.dispatchUpdatesTo(this)
    }
    @SuppressLint("NotifyDataSetChanged")
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

        b.commentCard.setOnLongClickListener{
            if (comment.publiser==Firebase.auth.currentUser!!.uid){
                commentClickListener.commentLongClickListener(comment.postId, comment.commentId)
            }
            true
        }


        Picasso.get().load(user?.imageurl).into(b.imageProfile)
        b.username.text=user?.username
        b.comment.text = comment.comment


    }


}
interface CommentClickListener{
    fun commentLongClickListener(postId:String,commentId:String)
}