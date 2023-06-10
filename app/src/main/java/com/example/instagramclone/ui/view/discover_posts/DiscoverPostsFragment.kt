package com.example.instagramclone.ui.view.discover_posts

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramclone.R
import com.example.instagramclone.base.BaseFragment
import com.example.instagramclone.databinding.DeleteMessageDialogBinding
import com.example.instagramclone.databinding.FragmentDiscoverPostsBinding
import com.example.instagramclone.ui.adapters.PostClickListener
import com.example.instagramclone.ui.adapters.PostsAdapters
import com.example.instagramclone.ui.view.home.HomeFragmentDirections
import com.example.instagramclone.ui.view.profile_view.ProfileDetailFragmentDirections
import com.example.instagramclone.utils.Resource
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class DiscoverPostsFragment : BaseFragment() {

    private lateinit var binding: FragmentDiscoverPostsBinding
    private val viewModel by viewModels<DiscoverPostsViewModel>()
    private val args by navArgs<DiscoverPostsFragmentArgs>()
    private val postAdapter by lazy {
        PostsAdapters(object : PostClickListener {
            override fun pImageuNameClickListener(bundle: Bundle) {
                val fNav = findNavController()
                when (fNav.currentDestination?.id) {
                    R.id.homeFragment -> {
                        fNav.navigate(R.id.action_homeFragment_to_search_nav, bundle)
                    }
                    R.id.profileDetailFragment -> {
                        fNav.navigate(R.id.action_profileDetailFragment_to_profileFragment, bundle)
                    }
                    R.id.discoverPostsFragment -> {
                        fNav.navigate(R.id.action_discoverPostsFragment_to_search_nav,bundle)
                    }
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_discover_posts, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun addObserves() {
        viewModel.postMutableLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    binding.progressBar2.visibility = VISIBLE
                }
                is Resource.Success -> {

                    postAdapter.updatePosts(it.data ?: emptyList())
                    binding.progressBar2.visibility = GONE

                }
                is Resource.Error -> {
                    binding.progressBar2.visibility = GONE
                    Toast.makeText(requireContext(), it.data.toString(), Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentPost = args.post
        setRecyclerView()
        viewModel.readPost(currentPost)


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

        Handler(Looper.getMainLooper()).postDelayed({

        },2000)

    }


    fun setRecyclerView() {
        binding.discoverRv.setHasFixedSize(true)
        binding.discoverRv.layoutManager = LinearLayoutManager(requireActivity())
        binding.discoverRv.adapter = postAdapter
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

    private fun showDeletePostDialog(postId: String) {
        val dialogBinding = DeleteMessageDialogBinding.inflate(layoutInflater)
        val mDialog = Dialog(requireContext())
        mDialog.setContentView(dialogBinding.root)
        mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogBinding.dInfo.setText(R.string.delete_post_txt)

        dialogBinding.yes.setOnClickListener {
            Firebase.firestore.collection("Posts").document(postId).delete().addOnSuccessListener {
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


}