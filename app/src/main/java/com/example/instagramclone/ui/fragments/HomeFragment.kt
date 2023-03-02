package com.example.instagramclone.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramclone.R
import com.example.instagramclone.data.entity.Posts
import com.example.instagramclone.databinding.FragmentHomeBinding
import com.example.instagramclone.ui.adapters.PostsAdapters
import com.google.api.Distribution.BucketOptions.Linear
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {


    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: FragmentHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: PostsAdapters
    private lateinit var postList: ArrayList<Posts>
    private lateinit var followList: ArrayList<String>

    private lateinit var userUID: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        postList = ArrayList()
        followList = ArrayList()
        auth = Firebase.auth

        firestore = Firebase.firestore

        binding.postRv.setHasFixedSize(true)
        val linerLayoutManager = LinearLayoutManager(requireActivity())
        linerLayoutManager.reverseLayout = true
        linerLayoutManager.stackFromEnd = true
        binding.postRv.layoutManager = linerLayoutManager


        checkFollowing()









        return binding.root
    }

    private fun checkFollowing(){



        firestore.collection("Follow").document(auth.currentUser!!.uid)
            .addSnapshotListener { documentSnapshot, error ->
                if(error!=null){
                    error.localizedMessage?.let { Log.e("", it)
                        return@addSnapshotListener}
                }else{
                    if (documentSnapshot!=null&&documentSnapshot.exists()){
                        val follow=documentSnapshot.data

                        if (follow != null) {
                            val following= follow["following"] as HashMap<*,*>
                            followList.clear()
                            for (i in following){
                                followList.add(i.key as String)

                            }
                            readPost()
                        }

                    }else{
                        Log.e("","")
                    }
                }
            }



    }

    @SuppressLint("NotifyDataSetChanged")
    fun readPost() {

        firestore.collection("Posts").addSnapshotListener { value, error ->
            if (error != null) {
                error.localizedMessage?.let { Log.e("", it) }
            } else {

                if (value != null) {

                    for (document in value.documents) {
                        val post_id = document.get("postId") as String
                        val postImage = document.get("postImage") as String
                        val description = document.get("description") as String
                        val publisher = document.get("publisher") as String
                        val post = Posts(post_id, postImage, description, publisher)

                        for (id in followList) {
                            if (publisher == id) {
                                postList.add(post)
                            }
                        }


                    }
                    adapter = PostsAdapters(requireContext(), postList)
                    binding.postRv.adapter=adapter

                }


            }

        }

    }


}



