package com.example.instagramclone.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramclone.ui.view.activity.ChatActivity
import com.example.instagramclone.R
import com.example.instagramclone.base.BaseFragment
import com.example.instagramclone.databinding.FragmentHomeBinding
import com.example.instagramclone.ui.adapters.PostClickListener
import com.example.instagramclone.ui.adapters.PostsAdapters
import com.example.instagramclone.ui.adapters.StoryAdapter
import com.example.instagramclone.ui.viewmodel.HomeViewModel
import com.example.instagramclone.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : BaseFragment() {


    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: FragmentHomeBinding
    private lateinit var auth: FirebaseAuth
    private val adapter by lazy {
        PostsAdapters(object : PostClickListener {
            override fun pImage_uNameClickListener(bundle: Bundle) {
                if (findNavController().currentDestination?.id == R.id.homeFragment) {
                    findNavController().navigate(R.id.action_homeFragment_to_search_nav, bundle)
                } else if (findNavController().currentDestination?.id == R.id.profileDetailFragment) {
                    findNavController().navigate(
                        R.id.action_profileDetailFragment_to_profileFragment,
                        bundle
                    )
                }
            }
        }, requireContext(), emptyList())
    }
    private val storyAdapter by lazy {
        StoryAdapter(requireContext(), emptyList())
    }
    private val viewModel by activityViewModels<HomeViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        firestore = Firebase.firestore
        binding.postRv.setHasFixedSize(true)
        val linerLayoutManager = LinearLayoutManager(requireActivity())
        binding.storyRv.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.postRv.layoutManager = linerLayoutManager




        binding.storyRv.adapter = storyAdapter
        binding.postRv.adapter = adapter


        binding.chat.setOnClickListener {
            val intent = Intent(requireActivity(), ChatActivity::class.java)
            requireActivity().startActivity(intent)
        }
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.checkFollowing()
            Handler(Looper.getMainLooper()).postDelayed({
                binding.swipeRefresh.isRefreshing = false
            }, 1000)

        }

    }


    override fun addObserves() {


        viewModel.storyMutableLiveData.observe(viewLifecycleOwner){
            when (it) {
                is Resource.Loading -> {
                    binding.shimmerstory.visibility = View.VISIBLE
                    binding.storyRv.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.shimmerstory.visibility = View.GONE
                    binding.shimmerstory.stopShimmer()
                    storyAdapter.updateStory(it.data ?: emptyList())
                    binding.storyRv.visibility = View.VISIBLE
                }
                is Resource.Error -> {
                    binding.storyRv.visibility = View.GONE
                    binding.shimmerstory.visibility = View.GONE
                    Toast.makeText(requireContext(), it.data.toString(), Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        viewModel.postMutableLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    binding.shimmer.visibility = View.VISIBLE
                    binding.postRv.visibility = View.GONE
                    binding.storyRv.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.shimmer.visibility = View.GONE
                    binding.shimmer.stopShimmer()
                    adapter.updatePosts(it.data ?: emptyList())
                    it.data?.let {list->
                        if (list.isEmpty()){
                            binding.followLottieLinear.visibility=View.VISIBLE
                        }
                    }
                    binding.postRv.visibility = View.VISIBLE
                }
                is Resource.Error -> {
                    binding.postRv.visibility = View.GONE
                    binding.storyRv.visibility = View.GONE
                    binding.shimmerstory.visibility = View.GONE
                    binding.shimmer.visibility = View.GONE
                    Toast.makeText(requireContext(), it.data.toString(), Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }


        viewModel.checkMessageLiveData.observe(viewLifecycleOwner) {
            if (it) {
                binding.checkMessage.visibility = View.GONE
            } else {
                binding.checkMessage.visibility = View.VISIBLE
            }
        }
    }


}



