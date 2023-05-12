package com.example.instagramclone.ui.view.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileDetailFragment : Fragment() {


    private lateinit var binding: FragmentProfileDetailBinding
    private lateinit var adapter: PostsAdapters
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_detail, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val bundle = arguments
        val postlist = bundle?.getParcelableArrayList<Posts>("posts")


        val position = bundle!!.getInt("position")

        binding.postsRv.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(requireContext())

        binding.postsRv.layoutManager = layoutManager
        adapter = postlist?.let { PostsAdapters(object : PostClickListener {
            override fun pImage_uNameClickListener(bundle: Bundle) {
                if ( findNavController().currentDestination?.id==R.id.homeFragment){
                    findNavController().navigate(R.id.action_homeFragment_to_search_nav,bundle)
                }else if ( findNavController().currentDestination?.id==R.id.profileDetailFragment){
                    findNavController().navigate(R.id.action_profileDetailFragment_to_profileFragment,bundle)
                }
            }

            override fun postOptionCLickListener(postId: String, view: View) {
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
        },requireActivity(), it) }!!


        Handler().postDelayed({
            binding.postsRv.adapter = adapter
            layoutManager.scrollToPositionWithOffset(position, 0)
        },300)




    }
    private fun showDeletePostDialog(postId: String){
        val dialogBinding= DeleteMessageDialogBinding.inflate(layoutInflater)
        val mDialog= Dialog(requireContext())
        mDialog.setContentView(dialogBinding.root)
        mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogBinding.dInfo.text="Delete this post?"

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