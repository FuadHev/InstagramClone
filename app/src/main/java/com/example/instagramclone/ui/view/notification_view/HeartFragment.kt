package com.example.instagramclone.ui.view.notification_view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramclone.R
import com.example.instagramclone.base.BaseFragment
import com.example.instagramclone.databinding.FragmentHeartBinding
import com.example.instagramclone.ui.adapters.NotificationsAdapter
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class HeartFragment : BaseFragment() {

    private lateinit var binding: FragmentHeartBinding
    private val adapter by lazy {
        NotificationsAdapter(emptyList())
    }
    private lateinit var firebaseUser: FirebaseUser
    private val viewModel by activityViewModels<HeartViewModel> ()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_heart, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseUser=Firebase.auth.currentUser!!

        setRecyclerView()
    }
    private fun setRecyclerView(){
        binding.notificationrv.setHasFixedSize(true)
        binding.notificationrv.layoutManager = LinearLayoutManager(requireContext())
        binding.notificationrv.adapter = adapter
    }

    override fun addObserves() {
        viewModel.notificationLiveData.observe(viewLifecycleOwner){
            adapter.updateList(it)
        }
    }



}