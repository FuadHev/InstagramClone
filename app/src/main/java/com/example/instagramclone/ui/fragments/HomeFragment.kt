package com.example.instagramclone.ui.fragments

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramclone.R
import com.example.instagramclone.data.entity.Posts
import com.example.instagramclone.databinding.FragmentHomeBinding
import com.example.instagramclone.ui.adapters.PostsAdapters
import com.example.instagramclone.ui.viewmodel.HomeViewModel
import com.google.firebase.Timestamp
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
    private lateinit var viewModel: HomeViewModel

    private lateinit var userUID: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tempViewModel: HomeViewModel by viewModels()
        viewModel = tempViewModel


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        postList = ArrayList()
        followList = ArrayList()
        auth = Firebase.auth

        firestore = Firebase.firestore
        viewModel.auth = auth.currentUser!!

        val progress = ProgressDialog(requireContext())
        progress.setMessage("Please wait")
        progress.show()

        binding.postRv.setHasFixedSize(true)
        val linerLayoutManager = LinearLayoutManager(requireActivity())
//        linerLayoutManager.reverseLayout = true
//        linerLayoutManager.stackFromEnd = true
        binding.postRv.layoutManager = linerLayoutManager
        adapter = PostsAdapters(requireContext(), emptyList())

        viewModel.checkFollowing()
        viewModel.readPost()

        viewModel.postsList.observe(viewLifecycleOwner) {
            adapter.updatePosts(it)
            binding.postRv.adapter = adapter
            Handler().postDelayed({
                progress.dismiss()
            }, 2000)
        }





//        checkFollowing()
//
//        readPost()


        return binding.root
    }


    private fun checkFollowing() {

        firestore.collection("Follow").document(auth.currentUser!!.uid)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    error.localizedMessage?.let {
                        Log.e("", it)
                        return@addSnapshotListener
                    }
                } else {


                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val follow = documentSnapshot.data

                        if (follow != null) {
                            try {

                                val following = follow["following"] as HashMap<*, *>
                                followList.clear()
                                for (i in following) {
                                    followList.add(i.key as String)

                                }

                            } catch (_: java.lang.NullPointerException) {

                            }
                        }

                    } else {
                        Log.e("", "")
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
                        try {
                            val post_id = document.get("postId") as String
                            val postImage = document.get("postImage") as String
                            val description = document.get("description") as String
                            val publisher = document.get("publisher") as String
                            val time = document.get("time") as Timestamp
                            val post = Posts(post_id, postImage, description, publisher, time)


                            for (id in followList) {
                                if (publisher == id) {
                                    postList.add(post)
                                }
                            }

                        } catch (_: java.lang.NullPointerException) {


                        }


                    }
                    adapter.notifyDataSetChanged()

                }


            }

        }

    }


}



