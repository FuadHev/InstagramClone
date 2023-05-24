package com.example.instagramclone.ui.view.fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramclone.ui.view.activity.ChatActivity
import com.example.instagramclone.R
import com.example.instagramclone.base.BaseFragment
import com.example.instagramclone.databinding.DeleteMessageDialogBinding
import com.example.instagramclone.databinding.FragmentHomeBinding
import com.example.instagramclone.ui.adapters.PostClickListener
import com.example.instagramclone.ui.adapters.PostsAdapters
import com.example.instagramclone.ui.adapters.StoryAdapter
import com.example.instagramclone.ui.adapters.StoryClickListener
import com.example.instagramclone.ui.viewmodel.HomeViewModel
import com.example.instagramclone.utils.Resource
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : BaseFragment() {


    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: FragmentHomeBinding
    private val viewModel by activityViewModels<HomeViewModel>()
    private lateinit var auth: FirebaseAuth

    private val adapter by lazy {
        PostsAdapters(object : PostClickListener {
            override fun pImage_uNameClickListener(bundle: Bundle) {
                val fNav = findNavController()
                if (fNav.currentDestination?.id == R.id.homeFragment) {
                    fNav.navigate(R.id.action_homeFragment_to_search_nav, bundle)
                } else if (fNav.currentDestination?.id == R.id.profileDetailFragment) {
                    fNav.navigate(R.id.action_profileDetailFragment_to_profileFragment, bundle)
                }
            }

            override fun postOptionCLickListener(postId: String, view: View) {
                setPopUpMenu(postId, view)
            }

            override fun commentsClickListener(postId: String, publisherId: String) {
                setCommentClickListener(postId, publisherId)
            }
        }, requireContext(), emptyList())
    }
    private val storyAdapter by lazy {
        StoryAdapter(object : StoryClickListener {
            override fun storyclickListener() {
                findNavController().navigate(R.id.action_homeFragment_to_addStoryFragment)
            }

        }, requireContext(), emptyList())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.homeFragment = this
        auth = Firebase.auth
        firestore = Firebase.firestore

        setRecyclerViews()


        binding.swipeRefresh.setOnRefreshListener {
            viewModel.checkFollowing()
            Handler(Looper.getMainLooper()).postDelayed({
                binding.swipeRefresh.isRefreshing = false
            }, 1000)

        }

    }

    private fun setRecyclerViews() {

        binding.postRv.setHasFixedSize(true)
        val linerLayoutManager = LinearLayoutManager(requireActivity())
        binding.storyRv.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.postRv.layoutManager = linerLayoutManager
        binding.storyRv.adapter = storyAdapter
        binding.postRv.adapter = adapter
    }

    fun chatClickListener() {
        val intent = Intent(requireActivity(), ChatActivity::class.java)
        requireActivity().startActivity(intent)
    }

    private fun setCommentClickListener(postId: String, publisherId: String) {
        val fNav = findNavController()
        when (fNav.currentDestination?.id) {
            R.id.homeFragment -> {
                fNav.navigate(
                    HomeFragmentDirections.actionHomeFragmentToCommentsFragment(
                        postId,
                        publisherId
                    )
                )
            }
            R.id.profileDetailFragment -> {
                fNav.navigate(
                    ProfileDetailFragmentDirections.actionProfileDetailFragmentToCommentsFragment(
                        postId,
                        publisherId
                    )
                )
            }
            R.id.discoverPostsFragment -> {
                fNav.navigate(
                    DiscoverPostsFragmentDirections.actionDiscoverPostsFragmentToCommentsFragment(
                        postId,
                        publisherId
                    )
                )
            }
        }
    }

    private fun setPopUpMenu(postId: String, view: View) {
        val popupMenu = PopupMenu(requireActivity(), view)
        popupMenu.menuInflater.inflate(R.menu.post_option_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.post_delete -> {
                    showDeletePostDialog(postId)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()

    }

    private fun showDeletePostDialog(postId: String) {
        val dialogBinding = DeleteMessageDialogBinding.inflate(layoutInflater)
        val mDialog = Dialog(requireContext())
        mDialog.setContentView(dialogBinding.root)
        mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogBinding.dInfo.text = "Delete this post?"

        dialogBinding.yes.setOnClickListener {
            firestore.collection("Posts").document(postId).delete().addOnSuccessListener {
                Toast.makeText(requireActivity(), "Post deleted", Toast.LENGTH_SHORT).show()
            }
            mDialog.dismiss()
        }

        dialogBinding.no.setOnClickListener {

            mDialog.dismiss()
        }

        mDialog.create()
        mDialog.show()

    }

    override fun addObserves() {


        viewModel.storyMutableLiveData.observe(viewLifecycleOwner) {
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
                }
                is Resource.Success -> {
                    binding.shimmer.visibility = View.GONE
                    binding.shimmer.stopShimmer()
                    binding.postRv.visibility = View.VISIBLE
                    binding.postRv.visibility = View.VISIBLE
                    adapter.updatePosts(it.data ?: emptyList())

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



