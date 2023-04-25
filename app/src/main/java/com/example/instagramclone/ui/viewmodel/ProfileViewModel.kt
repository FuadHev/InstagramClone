package com.example.instagramclone.ui.viewmodel

import android.content.Context
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagramclone.data.entity.Posts
import com.example.instagramclone.data.entity.Users
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ProfileViewModel:ViewModel() {
    val postsList= MutableLiveData<ArrayList<Posts>>()
    val savesList=MutableLiveData<ArrayList<Posts>>()
    val userInfo=MutableLiveData<Users>()




    fun mySaves(){

       val mySaves=ArrayList<String>()
        Firebase.firestore.collection("Saves").document(Firebase.auth.currentUser!!.uid).addSnapshotListener { value, error ->
            if (error!=null){

            }else{
                try{
                    if (value!=null){
                        val data=value.data as HashMap<*,*>
                        for (savekeys in data){
                            mySaves.add(savekeys.key as String)
                        }
                        readSaves(mySaves)

                    }
                }catch (e:Exception){

                }

            }


        }


    }
    fun readSaves(mySaves:ArrayList<String>){
        val save_postList=ArrayList<Posts>()
        Firebase.firestore.collection("Posts").addSnapshotListener { value, error ->
            if (error!=null){
                error.localizedMessage?.let { Log.e("posts", it) }
            }else{
                if (value!=null){
                    for (document in value.documents){

                        try {
                            for (id in mySaves){
                                val post_id = document.get("postId") as String
                                val postImage = document.get("postImage") as String
                                val description = document.get("description") as String
                                val publisher = document.get("publisher") as String
                                val time = document.get("time") as Timestamp
                                val post = Posts(post_id, postImage, description, publisher, time)
                                if (post_id==id){
                                    save_postList.add(post)
                                }
                            }
                        }catch (e:Exception){

                        }


                    }

                    savesList.value=save_postList
                }
            }
        }


    }





    fun userInfo(context: Context, profileId:String) {

        Firebase.firestore.collection("user").document(profileId).addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(context, error.localizedMessage, Toast.LENGTH_SHORT).show()
            } else {
                if (value != null && value.exists()) {

                    val username = value.get("username") as String
                    val imageurl = value.get("image_url") as String
                    val bio = value.get("bio") as String
//                    Picasso.get().load(imageurl).into(profilImage)
//                    userName.text=username
//                    bioinfo.text=bio
                    val user=Users(profileId,"",username,"",imageurl,bio)
                    userInfo.value=user

                }


            }

        }


    }




    fun  myFotos(profileid:String){

        Firebase.firestore.collection("Posts").addSnapshotListener { value, error ->
            if (error != null) {
                error.localizedMessage?.let { Log.e("", it) }
            } else {

                if (value != null) {
                    val postList=ArrayList<Posts>()
                    for (document in value.documents) {

                        try {
                            val post_id = document.get("postId") as String
                            val postImage = document.get("postImage") as String
                            val description = document.get("description") as String
                            val publisher = document.get("publisher") as String
                            val time = document.get("time") as Timestamp
                            val post = Posts(post_id, postImage, description, publisher, time)

                            if (publisher==profileid){
                                postList.add(post)
                            }



                        } catch (_: java.lang.NullPointerException) {


                        }


                    }
                    postList.sortByDescending {
                        it.time
                    }
                    postsList.postValue(postList)

                }


            }

        }

    }


}