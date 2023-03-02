package com.example.instagramclone.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SearchEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramclone.R
import com.example.instagramclone.data.entity.Users
import com.example.instagramclone.databinding.FragmentSearchBinding
import com.example.instagramclone.ui.adapters.UserAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.util.Objects


class SearchFragment : Fragment() {

    private lateinit var binding:FragmentSearchBinding
    private lateinit var adapter:UserAdapter
    private lateinit var usersList:ArrayList<Users>
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_search,container,false)

        firestore=Firebase.firestore
        usersList= ArrayList()
        binding.rv.setHasFixedSize(true)
        binding.rv.layoutManager=LinearLayoutManager(requireActivity())

        adapter= UserAdapter(requireContext(),usersList)
        binding.rv.adapter=adapter
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchView.setOnQueryTextListener(object:SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchUsers(query.toString())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
               searchUsers(newText.toString())
                return true
            }


        })

    }

    @SuppressLint("NotifyDataSetChanged", "SuspiciousIndentation")
    fun searchUsers(s:String){
        val query=firestore.collection("user").orderBy("username").startAt(s).endAt(s+"\uf8ff")

        query.addSnapshotListener { value, error ->

            usersList.clear()
            for (users in value!!.documents){

                val user_id=users.get("user_id") as String
                val email=users.get("email") as String
                val username=users.get("username") as String
                val password=users.get("password") as String
                val imageurl=users.get("image_url") as String
                val bio=users.get("bio") as String


                val user=Users(user_id,email,username,password,imageurl,bio)
                    usersList.add(user)

            }
            adapter.notifyDataSetChanged()
        }


    }







}