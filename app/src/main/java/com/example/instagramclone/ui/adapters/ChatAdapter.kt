package com.example.instagramclone.ui.adapters

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.model.ChatUser
import com.example.instagramclone.databinding.ChatItemBinding
import com.squareup.picasso.Picasso

class ChatAdapter(private val userClickListener: UserClickListener,var chatUserList: List<ChatUser>) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    inner class ViewHolder(val view: ChatItemBinding) : RecyclerView.ViewHolder(view.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = ChatItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return chatUserList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatUser = chatUserList[position]
        val b = holder.view

        b.message.text=chatUser.lastMessage
        if (!chatUser.seen){
            b.isseen.visibility= View.VISIBLE
            b.username.setTypeface( b.username.typeface, Typeface.BOLD)
            b.message.setTypeface( b.username.typeface, Typeface.BOLD)
        }else{
            b.isseen.visibility= View.GONE
        }

        b.chatCard.setOnClickListener {
            userClickListener.chatUserCLickListener(chatUser.receiverId)
        }

        b.username.text = chatUser.username
        Picasso.get().load(chatUser.imageUrl).into(b.profilImage)
    }

    fun updateChatList(newList:List<ChatUser>){
        this.chatUserList=newList
        notifyDataSetChanged()
    }


}
interface UserClickListener{
    fun chatUserCLickListener(currentUser:String)
}