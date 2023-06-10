package com.example.instagramclone.ui.view.followers_view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
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
        setTabLayoutManager()

        return binding.root
    }

    private fun setTabLayoutManager() {
        val adapter = MyViewPagerAdapter(requireActivity())
        binding.viewpager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewpager) { tab, position ->

            if (position < 2) {
                Log.e("tab", position.toString())
                Log.e("tab", tab.toString())
                tab.text = fragmentTitleList[position]
            }

        }.attach()

        if (arguments?.getString("follow") == "following") {
            Handler(Looper.getMainLooper()).postDelayed({
                val nextpage = binding.viewpager.currentItem + 1
                binding.viewpager.setCurrentItem(nextpage, true)
            }, 500)
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

        if (fragmentTitleList.size < 3) {
            fragmentTitleList.add("follower")
            fragmentTitleList.add("following")
        }

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