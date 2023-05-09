package com.example.instagramclone.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.model.Message
import com.example.instagramclone.databinding.MessageRecieverItemBinding
import com.example.instagramclone.databinding.MessageSendItemBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MessaggeAdapter(private val messageClickListener: MessageClickListener,private var messageList:List<Message>):RecyclerView.Adapter<RecyclerView.ViewHolder>(){


    val ITEM_RECEIVE=1
    val ITEM_SENT=2


    inner class SentViewHolder(val view:MessageSendItemBinding):RecyclerView.ViewHolder(view.root)

    inner class ReceiveViewHolder(val view:MessageRecieverItemBinding):RecyclerView.ViewHolder(view.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType==1){
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = MessageRecieverItemBinding.inflate(layoutInflater, parent, false)
            ReceiveViewHolder(view)
        }else{
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = MessageSendItemBinding.inflate(layoutInflater, parent, false)
            SentViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {

        val currentMessage=messageList[position]
        return if (Firebase.auth.currentUser!!.uid==currentMessage.senderId){
            ITEM_SENT
        }else{
            ITEM_RECEIVE
        }


    }

    fun updateMessages(newMessageList:List<Message>){
        this.messageList=newMessageList
        notifyDataSetChanged()

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val currentMessage=messageList[position]
        if (holder.javaClass==SentViewHolder::class.java){
            val viewHolder= holder as SentViewHolder
            val b=viewHolder.view

            b.txtSendMessage.text=currentMessage.messagetxt


            b.messageLayout.setOnLongClickListener {
                messageClickListener.messageClickListener(currentMessage.senderId,currentMessage.messageId)
                true
            }



        }else{
            val viewHolder= holder as ReceiveViewHolder
            val b=viewHolder.view


            // alert dialog qoymaliyam mesaji silinmesini tesdiqlemek ucun

            b.messageLayout.setOnLongClickListener {
                messageClickListener.messageClickListener(currentMessage.senderId,currentMessage.messageId)
                true
            }

            b.txtSendMessage.text=currentMessage.messagetxt



        }
    }


}
interface MessageClickListener{
    fun messageClickListener(senderId:String,messageId:String)
}