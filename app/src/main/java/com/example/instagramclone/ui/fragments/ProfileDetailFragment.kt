package com.example.instagramclone.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.R
import com.example.instagramclone.data.entity.Posts
import com.example.instagramclone.databinding.FragmentProfileDetailBinding
import com.example.instagramclone.databinding.PostsCardViewBinding
import com.example.instagramclone.ui.adapters.PostsAdapters

class ProfileDetailFragment : Fragment() {


    private lateinit var binding: FragmentProfileDetailBinding
    private lateinit var adapter: PostsAdapters
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_profile_detail, container, false)


        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val bundle = arguments
        val postlist = bundle?.getParcelableArrayList<Posts>("posts")


        val position = bundle!!.getInt("position")

        binding.postsRv.setHasFixedSize(true)

        binding.postsRv.requestFocus()
        val layoutManager = LinearLayoutManager(requireContext())

        binding.postsRv.layoutManager = layoutManager

        layoutManager.scrollToPosition(position)

        adapter = postlist?.let { PostsAdapters(requireContext(), it) }!!


        binding.postsRv.adapter = adapter


    }


}