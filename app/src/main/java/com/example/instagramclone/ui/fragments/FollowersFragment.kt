package com.example.instagramclone.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.instagramclone.FollowFollowing
import com.example.instagramclone.R
import com.example.instagramclone.databinding.FragmentFollowersBinding
import com.example.instagramclone.ui.viewmodel.FollowerViewModel
import com.example.instagramclone.ui.viewmodel.HomeViewModel
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

        val adapter = MyViewPagerAdapter(requireActivity())

        binding.viewpager.adapter = adapter



        TabLayoutMediator(binding.tabLayout, binding.viewpager) { tab, position ->
            tab.text = fragmentTitleList[position]

        }.attach()

        if (argument?.getString("follow")=="following"){
            Handler().postDelayed({
                val nextpage=binding.viewpager.currentItem+1
                binding.viewpager.setCurrentItem(nextpage,true)
            },500)
        }







    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = Bundle().also {
            it.putString("profileId", arguments?.getString("id"))
        }
        fragmentList.add(FollowerFragment().also {
            it.arguments = args
        })
        fragmentList.add(FollowingFragment().also {
            it.arguments = args
        })
        fragmentTitleList.add("follower")
        fragmentTitleList.add("following")
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