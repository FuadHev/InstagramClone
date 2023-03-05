package com.example.instagramclone

import androidx.recyclerview.widget.DiffUtil
import com.example.instagramclone.data.entity.Comment

class MyDiffUtilCallBack(val oldElements:List<Comment>,val newElements:List<Comment>):DiffUtil.Callback() {
    override fun getOldListSize(): Int=oldElements.size
    override fun getNewListSize():Int=newElements.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =oldElements[oldItemPosition].comment==newElements[newItemPosition].comment

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =oldElements[oldItemPosition]==newElements[newItemPosition]


}