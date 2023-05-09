package com.example.instagramclone.ui.view

import androidx.recyclerview.widget.DiffUtil
import com.example.instagramclone.model.Comment

class MyDiffUtilCallBack(val oldElements:List<Comment>, val newElements:List<Comment>):DiffUtil.Callback() {
    override fun getOldListSize(): Int=oldElements.size
    override fun getNewListSize():Int=newElements.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =oldElements[oldItemPosition].comment==newElements[newItemPosition].comment

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =oldElements[oldItemPosition]==newElements[newItemPosition]

}