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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramclone.R
import com.example.instagramclone.data.entity.Posts
import com.example.instagramclone.data.entity.Story
import com.example.instagramclone.databinding.FragmentHomeBinding
import com.example.instagramclone.ui.adapters.PostsAdapters
import com.example.instagramclone.ui.adapters.StoryAdapter
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
    private val viewModel by activityViewModels<HomeViewModel>()

    private lateinit var storyAdapter: StoryAdapter
    private var storyList = ArrayList<Story>()


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

//        val progress = ProgressDialog(requireContext())
//        progress.setMessage("Please wait")
//        progress.show()
        viewModel.checkFollowing()
        viewModel.readPost()
        checkFollowing()
        binding.postRv.setHasFixedSize(true)
        val linerLayoutManager = LinearLayoutManager(requireActivity())
        binding.storyRv.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.postRv.layoutManager = linerLayoutManager

        adapter = PostsAdapters(requireContext(), viewModel.postList)
        storyAdapter = StoryAdapter(requireContext(), storyList)

        binding.storyRv.adapter = storyAdapter
        binding.postRv.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.checkFollowing()
            viewModel.readPost()
            checkFollowing()
            adapter.updatePosts(viewModel.postList)
            storyAdapter.updateStory(storyList)
            Handler().postDelayed({
                binding.swipeRefresh.isRefreshing = false
            }, 1200)

        }
        return binding.root
    }

    private fun readStory() {


        storyList.clear()
        storyList.add(Story("", 0, 0, "", Firebase.auth.currentUser!!.uid))
        for (id in followList) {
            var countStory = 0
            firestore.collection("Story").document(id).addSnapshotListener { value, error ->
                if (error != null) {
                } else {
                    if (value != null && value.exists()) {
                        val doc = value.data as HashMap<*, *>
                        try {

                            var ustory: Story? = null
                            val timecurrent = System.currentTimeMillis()
                            for (i in doc) {
                                val story = i.value as HashMap<*, *>
                                val timestart = story["timeStart"] as Long
                                val timeend = story["timeEnd"] as Long
                                val imageurl = story["imageurl"] as String
                                val storyId = story["storyId"] as String
                                val userId = story["userId"] as String
                                if (timecurrent > timestart && timecurrent < timeend) {
                                    countStory++
                                }
                                ustory = Story(imageurl, timestart, timeend, storyId, userId)

                            }
                            if (countStory > 0) {
                                if (ustory != null) {
                                    storyList.add(ustory)
                                }
                            }

                            adapter.notifyDataSetChanged()


                        } catch (e: java.lang.NullPointerException) {


                        }


                    }

                }

            }
        }


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
                        followList.clear()
                        val follow = documentSnapshot.data

                        if (follow != null) {
                            try {

                                val following = follow["following"] as HashMap<*, *>
                                followList.clear()
                                for (i in following) {
                                    followList.add(i.key as String)

                                }

                                readStory()

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



