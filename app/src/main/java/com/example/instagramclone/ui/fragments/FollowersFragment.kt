package com.example.instagramclone.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.instagramclone.FollowFollowing
import com.example.instagramclone.R
import com.example.instagramclone.databinding.FragmentFollowersBinding
import com.google.android.material.tabs.TabLayoutMediator

class FollowersFragment : Fragment() {


    private lateinit var binding: FragmentFollowersBinding
    private val fragmentList = ArrayList<Fragment>()
    private val fragmentTitleList = ArrayList<String>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_followers, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val argument = arguments

        val profileId = argument?.getString("id")

        FollowFollowing.arg = profileId

        val adapter = MyViewPagerAdapter(requireActivity())

        binding.viewpager.adapter = adapter



        TabLayoutMediator(binding.tabLayout, binding.viewpager) { tab, position ->
            tab.text = fragmentTitleList[position]

        }.attach()

        if (argument?.getString("follow") == "followers") {
            binding.viewpager.currentItem = 0
        } else {
            binding.viewpager.currentItem = 1
        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("Oncreate", "Oncreat")
        fragmentList.add(FollowerFragment())
        fragmentList.add(FollowingFragment())
        fragmentTitleList.add("follower")
        fragmentTitleList.add("following")
    }

    override fun onPause() {
        super.onPause()
        Log.e("Pause", "Pause")


    }

    override fun onStart() {

        Log.e("Start", "Start")
//        fragmentTitleList.add("follower")
//        fragmentTitleList.add("following")
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        Log.e("Stop", "Stop")
//        fragmentTitleList.clear()

    }

    override fun onResume() {
        super.onResume()
        Log.e("Resume", "Resume")
    }

    inner class MyViewPagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int {
            return fragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragmentList[position]
        }

    }


}