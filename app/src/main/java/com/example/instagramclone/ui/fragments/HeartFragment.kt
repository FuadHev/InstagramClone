package com.example.instagramclone.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramclone.R
import com.example.instagramclone.data.entity.Notification
import com.example.instagramclone.databinding.FragmentHeartBinding
import com.example.instagramclone.ui.adapters.NotificationsAdapter
import com.example.instagramclone.ui.viewmodel.HeartViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HeartFragment : Fragment() {

    private lateinit var binding: FragmentHeartBinding
//    private lateinit var nList: ArrayList<Notification>
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

        binding.notificationrv.setHasFixedSize(true)
        binding.notificationrv.layoutManager = LinearLayoutManager(requireContext())
        binding.notificationrv.adapter = adapter

        viewModel.notificationLiveData.observe(viewLifecycleOwner){
            adapter.updateList(it)
        }

//        readNotification()

    }


//    fun readNotification() {
//        Firebase.firestore.collection("Notification").document(firebaseUser.uid)
//            .addSnapshotListener { value, error ->
//                if (error != null) {
//                    error.localizedMessage?.let { Log.e("error", it) }
//                } else {
//                    if (value != null) {
//                        nList.clear()
//                        try {
//                            val datakeys = value.data as HashMap<*,*>
//                            for (data in datakeys){
//                                val valuedata=data.value as HashMap<*,*>
//                                val userId=valuedata["userId"] as String
//                                val ntext=valuedata["nText"] as String
//                                val postId=valuedata["postId"] as String
//                                val isPost=valuedata["isPost"] as Boolean
//                                val time=valuedata["time"] as Timestamp
//                                val notification=Notification(userId,ntext,postId,isPost,time)
//                                nList.add(notification)
//
//                            }
//                            nList.sortByDescending {
//                                it.time
//                            }
//                            adapter.notifyDataSetChanged()
//
//                        } catch (e: NullPointerException) {
//                            e.printStackTrace()
//                        }
//
//                    }
//
//                }
//
//            }
//    }


}