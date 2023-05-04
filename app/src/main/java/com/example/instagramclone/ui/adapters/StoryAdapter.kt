package com.example.instagramclone.ui.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.AddStoryActivity
import com.example.instagramclone.StoryActivity
import com.example.instagramclone.data.entity.Story
import com.example.instagramclone.databinding.PostsCardViewBinding
import com.example.instagramclone.databinding.StoryItemBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.NonDisposableHandle.parent

class StoryAdapter(val mContext: Context, private var storyList:List<Story>) :
    RecyclerView.Adapter<StoryAdapter.ViewHolder>() {

    inner class ViewHolder(val view: StoryItemBinding) :
        RecyclerView.ViewHolder(view.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: StoryItemBinding = StoryItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return storyList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = storyList[position]

        val b = holder.view

        userInfo(b, story.userId, position)

        if (position!=0){
            seenStory(b.profilImage,story.userId)
        }



        if (position == 0) {
            b.addStory.visibility = View.VISIBLE
            myStory(b.username, b.profilImage, false)
        } else {
            b.addStory.visibility = View.GONE

        }
        b.profilImage.setOnClickListener {
            if (position == 0) {
                myStory(b.username, b.profilImage, true)
                Log.e("", position.toString())
            } else {
                // go to Story
                val intent=Intent(mContext,StoryActivity::class.java)
                intent.putExtra("userId",story.userId)
                mContext.startActivity(intent)
            }

        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateStory(newStories:ArrayList<Story>){
        this.storyList=newStories
        notifyDataSetChanged()
    }



    private fun userInfo(viewHoder: StoryItemBinding, userId: String, position: Int) {
        Firebase.firestore.collection("user").document(userId).addSnapshotListener { value, error ->
            if (error != null) {

            } else {
                if (value != null) {
                    val username = value.get("username") as String
                    val imageurl = value.get("image_url") as String
                    if (position == 0) {
                        Picasso.get().load(imageurl).into(viewHoder.profilImage)
                        viewHoder.username.text = "My story"
                        viewHoder.profilImage.borderWidth = 0
                    } else {
                        Picasso.get().load(imageurl).into(viewHoder.profilImage)
                        viewHoder.profilImage.borderWidth = 6
                        viewHoder.username.text = username
                    }
                }
            }


        }

    }

    private fun myStory(textView: TextView, imageView: CircleImageView, click: Boolean) {
        Firebase.firestore.collection("Story").document(Firebase.auth.currentUser!!.uid)
            .addSnapshotListener { value, error ->
                if (error != null) {

                } else {
                    var count = 0
                    if (value != null) {
                        try {

                            val stories = value.data as HashMap<*, *>

                            for (story in stories) {
                                val storyinfo = story.value as HashMap<*, *>
                                val timecurrent = System.currentTimeMillis()
                                val timeStart = storyinfo["timeStart"] as Long
                                val timeEnd = storyinfo["timeEnd"] as Long
                                if (timecurrent in (timeStart + 1) until timeEnd) {
                                    ++count
                                }
                            }

                        } catch (e: NullPointerException) {

                        }

                        if (click) {

                            if (count > 0) {
                                val alert = AlertDialog.Builder(mContext)
                                alert.setNegativeButton("view story") { d, i ->
                                    val intent=Intent(mContext,StoryActivity::class.java)
                                    intent.putExtra("userId",Firebase.auth.currentUser!!.uid)
                                    mContext.startActivity(intent)

                                }
                                alert.setPositiveButton("add story") { d, i ->
                                    val intent = Intent(mContext, AddStoryActivity::class.java)
                                    mContext.startActivity(intent)
                                }
                                alert.create().show()

                            } else {
                                val intent = Intent(mContext, AddStoryActivity::class.java)
                                mContext.startActivity(intent)
                            }


                        } else {
                            if (count > 0) {
                                textView.text = "My story"
                            } else {
                                textView.text = "Add story"
                            }
                        }


                    }
                }


            }
//        if (click) {
//            Log.e("count",count.toString())
//            if (count>0){
//                val alert=AlertDialog.Builder(mContext)
//                alert.setNegativeButton("view story"){d,i->
//                }
//                alert.setPositiveButton("add story"){d,i->
//                    val intent=Intent(mContext,AddStoryActivity::class.java)
//                    mContext.startActivity(intent)
//                }
//                alert.create().show()
//
//            }else{
//                val intent=Intent(mContext,AddStoryActivity::class.java)
//                mContext.startActivity(intent)
//            }
//
//
//        } else {
//            if (count > 0) {
//                textView.text = "My story"
//            } else {
//                textView.text = "Add story"
//            }
//        }


    }

    private fun seenStory(profilImage: CircleImageView, userId: String) {


        val ref=Firebase.firestore.collection("Story").document(userId)

        ref.addSnapshotListener { value, error ->

            if (error != null) {
            } else {
                if (value != null && value.exists()) {
                    val stories = value.data as HashMap<*,*>
                    try {
                        var i=true
                        for (storykey in stories) {
                            val story = storykey.value as HashMap<*,*>
                            val timeend = story["timeEnd"] as Long
                            val views=story["views"] as HashMap<*,*>


                            Log.e("contain",(!views.containsKey(Firebase.auth.currentUser!!.uid) && System.currentTimeMillis()<timeend).toString())
                            Log.e("contain",(!views.containsKey(Firebase.auth.currentUser!!.uid)).toString())

                            if (views.containsKey(Firebase.auth.currentUser!!.uid) && System.currentTimeMillis()<timeend){
                               i=true
                            }else if (System.currentTimeMillis()<timeend){
                                i=false
                            }
                        }
                        if (i){
                            profilImage.borderWidth=0
                        }else{
                            profilImage.borderWidth=6
                        }



                    } catch (e: java.lang.NullPointerException) {


                    }


                }

            }


        }

    }


//    override fun getItemViewType(position: Int): Int {
//        if (position==0){
//            return 0
//        }
//        return 1
//    }
}