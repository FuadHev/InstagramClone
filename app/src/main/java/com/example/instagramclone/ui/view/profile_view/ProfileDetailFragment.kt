package com.example.instagramclone.ui.view.profile_view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramclone.R
import com.example.instagramclone.databinding.DeleteMessageDialogBinding
import com.example.instagramclone.model.Posts
import com.example.instagramclone.databinding.FragmentProfileDetailBinding
import com.example.instagramclone.ui.adapters.PostClickListener
import com.example.instagramclone.ui.adapters.PostsAdapters
import com.example.instagramclone.ui.view.discover_posts.DiscoverPostsFragmentDirections
import com.example.instagramclone.ui.view.home.HomeFragmentDirections
import com.example.instagramclone.ui.view.profile_view.ProfileDetailFragmentDirections
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileDetailFragment : Fragment() {


    private lateinit var binding: FragmentProfileDetailBinding
    private val adapter by lazy {
        PostsAdapters(object : PostClickListener{
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
                popUpMenu(postId,view)
            }

            override fun commentsClickListener(postId: String, publisherId: String) {
                setCommentClickListener(postId,publisherId)
            }

        },requireActivity(), emptyList())
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_detail, container, false)
        return binding.root
    }

    fun setCommentClickListener(postId: String,publisherId:String){
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle = arguments
        // duzgun yol olmadigini bilirem sadece asand olsun deye etmisem
        val postlist = bundle?.getParcelableArrayList<Posts>("posts")
        val position = bundle!!.getInt("position")

        binding.postsRv.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(requireContext())
        binding.postsRv.layoutManager = layoutManager
        binding.postsRv.adapter = adapter
        postlist?.let { adapter.updatePosts(postlist) }
        Handler(Looper.getMainLooper()).postDelayed({
            layoutManager.scrollToPositionWithOffset(position, 0)
        },300)




    }
    private fun popUpMenu(postId: String,view: View){
        val popupMenu = PopupMenu(requireActivity(),view)
        popupMenu.menuInflater.inflate(R.menu.post_option_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {item->
            when(item.itemId){
                R.id.post_delete->{
                    showDeletePostDialog(postId)
                    true
                }

                else-> false
            }
        }
        popupMenu.show()
    }
    private fun showDeletePostDialog(postId: String){
        val dialogBinding= DeleteMessageDialogBinding.inflate(layoutInflater)
        val mDialog= Dialog(requireContext())
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