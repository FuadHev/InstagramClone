package com.example.instagramclone.ui.view.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramclone.R
import com.example.instagramclone.model.Users
import com.example.instagramclone.databinding.FragmentFollowingBinding
import com.example.instagramclone.ui.adapters.ClickListener
import com.example.instagramclone.ui.adapters.UserAdapter
import com.example.instagramclone.ui.viewmodel.FollowingViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class FollowingFragment : Fragment() {

    private lateinit var binding:FragmentFollowingBinding
    private lateinit var adapter:UserAdapter
    private lateinit var idList: ArrayList<String>
    private lateinit var usersList: ArrayList<Users>
    private lateinit var id: String
    private lateinit var firestore: FirebaseFirestore
    private val viewModel by viewModels<FollowingViewModel> ()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_following, container, false)

        // Inflate the layout for this fragment
        return binding.root
    }

       override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        idList = ArrayList()
        usersList = ArrayList()
        id = arguments?.getString("profileId") as String
        firestore = Firebase.firestore
        Log.e("id",id.toString())

        viewModel.getFollowings(id)


        binding.followingRv.layoutManager=LinearLayoutManager(requireActivity())
        adapter= UserAdapter(requireContext(),object :ClickListener{
            override fun userClickListener(bundle: Bundle) {
                if ( findNavController().currentDestination?.id==R.id.searctoFragment){
                    findNavController().navigate(R.id.action_searctoFragment_to_search_nav,bundle)
                }else if ( findNavController().currentDestination?.id==R.id.followersFragment){
                    findNavController().navigate(R.id.action_followersFragment_to_profileFragment,bundle)
                }
            }

        },usersList,)
        binding.followingRv.adapter=adapter

        viewModel.userList.observe(viewLifecycleOwner){
            adapter.updateUsers(it)
        }




    }
//
//
//    fun getUsers() {
//        firestore.collection("user").addSnapshotListener { value, error ->
//            if (error != null) {
//
//            } else {
//                if (value != null) {
//                    if (!value.isEmpty) {
//                        for (user in value.documents) {
//                            val user_id = user.get("user_id") as String
//                            val email = user.get("email") as String
//                            val username = user.get("username") as String
//                            val password = user.get("password") as String
//                            val imageurl = user.get("image_url") as String
//                            val bio = user.get("bio") as String
//                            val user = Users(user_id, email, username, password, imageurl, bio)
//                            for (id in idList) {
//                                if (user_id == id) {
//                                    usersList.add(user)
//                                }
//                            }
//                            adapter.notifyDataSetChanged()
//
//
//                        }
//
//
//                    }
//
//                }
//            }
//        }
//
//
//    }
//
//    private fun getFollowings(){
//
//            firestore.collection("Follow").document(id).addSnapshotListener { value, error ->
//                if (error != null) {
//
//                } else {
//                    try {
//                        if (value != null) {
//                            val data = value.get("following") as HashMap<*, *>
//
//                            for (follower in data) {
//                                idList.add(follower.key.toString())
//                            }
//
//                            getUsers()
//                        }
//                    } catch (e: java.lang.NullPointerException) {
//
//                    }
//
//                }
//
//
//            }
//
//
//
//
//    }


}