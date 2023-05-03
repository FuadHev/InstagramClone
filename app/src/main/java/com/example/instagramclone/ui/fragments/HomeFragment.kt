package com.example.instagramclone.ui.fragments

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.opengl.Visibility
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
import com.example.instagramclone.ChatActivity
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
    private lateinit var storyAdapter: StoryAdapter
    private val viewModel by activityViewModels<HomeViewModel>()




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)


        auth = Firebase.auth
        firestore = Firebase.firestore
        binding.postRv.setHasFixedSize(true)
        val linerLayoutManager = LinearLayoutManager(requireActivity())
        binding.storyRv.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.postRv.layoutManager = linerLayoutManager
//        val progress = ProgressDialog(requireContext())
//        progress.setMessage("Please wait loading post")
//        progress.show()


//        viewModel.checkFollowing()
//        viewModel.readStory()
//        viewModel.readPost()


        binding.shimmer.visibility=View.VISIBLE
        binding.shimmer.startShimmer()
        binding.shimmerstory.startShimmer()

        Handler().postDelayed({
            adapter = PostsAdapters(requireContext(),viewModel.postList)
            storyAdapter = StoryAdapter(requireContext(),viewModel.storyList)

            binding.storyRv.adapter = storyAdapter
            binding.postRv.adapter = adapter
//            progress.dismiss()

            binding.shimmer.stopShimmer()
            binding.shimmerstory.stopShimmer()
            binding.shimmerstory.visibility=View.GONE
            binding.shimmer.visibility=View.GONE
            binding.postRv.visibility=View.VISIBLE
            binding.storyRv.visibility=View.VISIBLE
        },1300)
        
        viewModel.checkMessageLiveData.observe(viewLifecycleOwner){
            if (it){
               binding.checkMessage.visibility=View.GONE
            }else{
                binding.checkMessage.visibility=View.VISIBLE
            }
        }
        binding.chat.setOnClickListener {
            val intent=Intent(requireActivity(),ChatActivity::class.java)
            requireActivity().startActivity(intent)
        }
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.readStory()
            viewModel.readPost()
            adapter.updatePosts(viewModel.postList)
            storyAdapter.updateStory(viewModel.storyList)
            Handler().postDelayed({
                binding.swipeRefresh.isRefreshing = false
            }, 1200)

        }
        return binding.root
    }




}



