package com.example.instagramclone.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramclone.R
import com.example.instagramclone.data.entity.Notification
import com.example.instagramclone.databinding.FragmentHeartBinding
import com.example.instagramclone.ui.adapters.NotificationsAdapter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HeartFragment : Fragment() {

    private lateinit var binding: FragmentHeartBinding
    private lateinit var nList: ArrayList<Notification>
    private lateinit var adapter: NotificationsAdapter
    private lateinit var firebaseUser: FirebaseUser
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_heart, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nList = ArrayList()
        firebaseUser=Firebase.auth.currentUser!!

        binding.notificationrv.setHasFixedSize(true)
        binding.notificationrv.layoutManager = LinearLayoutManager(requireContext())
        adapter = NotificationsAdapter(nList)
        binding.notificationrv.adapter = adapter

        readNotification()

    }


    fun readNotification() {
        Firebase.firestore.collection("Notification").document(firebaseUser.uid)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    error.localizedMessage?.let { Log.e("error", it) }
                } else {
                    if (value != null) {
                        nList.clear()
                        try {
                            val datakeys = value.data as java.util.HashMap<*, *>
                            for (data in datakeys){
                                val value=data.value as HashMap<*,*>
                                val userId=value["userId"] as String
                                val ntext=value["nText"] as String
                                val postId=value["postId"] as String
                                val isPost=value["isPost"] as Boolean
                                val time=value["time"] as Timestamp
                                val notification=Notification(userId,ntext,postId,isPost,time)
                                nList.add(notification)

                            }
                            nList.sortByDescending {
                                it.time
                            }
                            adapter.notifyDataSetChanged()

                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        }

                    }

                }

            }
    }


}