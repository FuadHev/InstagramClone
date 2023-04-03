package com.example.instagramclone.ui.adapters

import android.opengl.Visibility
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.data.entity.Notification
import com.example.instagramclone.databinding.NotificationItemBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class NotificationsAdapter(private val notificationList: List<Notification>) :
    RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {


    inner class ViewHolder(val view: NotificationItemBinding) : RecyclerView.ViewHolder(view.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: NotificationItemBinding =
            NotificationItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notificationList[position]
        val b = holder.view

        getUserInfo(b.profilImage,b.username,notification.userId)





        val maxLength = 30 // Metnin maksimum uzunluğu
        if (notification.ntext.length> maxLength) {
            val kisaMesaj =
                notification.ntext.substring(0, maxLength) + "..." // Metni kısaltma ve 3 nokta ekleme
            b.comment.text=kisaMesaj
        } else {
            b.comment.text=notification.ntext

        }



        if(notification.isPost){
            b.postImage.visibility= VISIBLE
            getPostImage(notification.postId,b.postImage)
        }else{
            b.postImage.visibility= GONE
        }

    }


    fun getUserInfo(profilImage: CircleImageView, username: TextView, userId: String) {

        Firebase.firestore.collection("user").document(userId).addSnapshotListener { value, error ->

            if (error != null) {

            }else{
                try {
                    if (value!=null){
                        val pImage=value.get("image_url") as String
                        val uName=value.get("username") as String
                        Picasso.get().load(pImage).into(profilImage)
                        username.text=uName

                    }
                }catch (e:java.lang.NullPointerException){
                    Log.e("e","")
                }

            }

        }

    }

    fun getPostImage(postId:String,postImageView:ImageView){
        Firebase.firestore.collection("Posts").document(postId).addSnapshotListener { value, error ->
            if (error != null) {

            }else{
                try {
                    if (value!=null){
                        val postImage=value.get("postImage") as String
                        Picasso.get().load(postImage).into(postImageView)

                    }
                }catch (e:java.lang.NullPointerException){
                    Log.e("e","")
                }
            }

        }
    }
}