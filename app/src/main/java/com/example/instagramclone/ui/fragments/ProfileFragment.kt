package com.example.instagramclone.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.example.instagramclone.R
import com.example.instagramclone.data.entity.Posts
import com.example.instagramclone.databinding.FragmentProfileBinding
import com.example.instagramclone.ui.adapters.MyFotoAdapter
import com.example.instagramclone.ui.viewmodel.ProfileViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var firbaseUser: FirebaseUser
    private lateinit var adapter: MyFotoAdapter
    private lateinit var firestore: FirebaseFirestore
    private var profileid: String? = null
    private lateinit var postList: ArrayList<Posts>
    private lateinit var viewModel: ProfileViewModel
    private lateinit var sp:SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tempViewModel: ProfileViewModel by viewModels()
        viewModel = tempViewModel

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_profile,container,false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firbaseUser = Firebase.auth.currentUser!!
        firestore = Firebase.firestore
        sp = requireActivity().getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        profileid = sp.getString("profileid", firbaseUser.uid)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar2)

        binding.fotosRv.visibility=VISIBLE
        binding.savesRv.visibility= GONE


        binding.fotosRv.setHasFixedSize(true)

        binding.fotosRv.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.savesRv.layoutManager= GridLayoutManager(requireContext(), 3)
        adapter = MyFotoAdapter(emptyList())

        viewModel.postsList.observe(viewLifecycleOwner) {
            adapter.updateMyPosts(it)
            binding.fotosRv.adapter = adapter
        }

        profileid?.let { viewModel.userInfo(requireContext(),firestore, it,binding.username,binding.bio,binding.imageProfile) }
        getFollower()
        getNrPost()
        profileid?.let { viewModel.myFotos(firestore, it) }
        viewModel.mySaves(firestore,firbaseUser)


        if (profileid == firbaseUser.uid) {

            binding.editProfil.text = "edit profile"
        } else {
            checkFollow()
            binding.save.visibility = GONE
        }

        binding.editProfil.setOnClickListener {
            val btn = binding.editProfil.text.toString().lowercase()
            if (btn == "edit profile") {

                Navigation.findNavController(it).navigate(R.id.action_profilfragment_to_editProfileFragment)


            } else if (btn == "follow") {

                val following = hashMapOf<String, HashMap<String?, Boolean>>()
                val id = hashMapOf<String?, Boolean>()
                id[profileid] = true
                following["following"] = id

                val follower = hashMapOf<String, HashMap<String, Boolean>>()
                val id2 = hashMapOf<String, Boolean>()
                id2[firbaseUser.uid] = true
                follower["followers"] = id2
                firestore.collection("Follow").document(firbaseUser.uid).set(
                    following,
                    SetOptions.merge()
                )
                firestore.collection("Follow").document(profileid!!)
                    .set(follower, SetOptions.merge())

                binding.editProfil.text = "following"

            } else if (btn == "following") {


                firestore.collection("Follow").document(firbaseUser.uid)
                    .update("following.${profileid}", FieldValue.delete())
                firestore.collection("Follow").document(profileid!!)
                    .update("followers.${firbaseUser.uid}", FieldValue.delete())
                binding.editProfil.text = "follow"

            }


        }
        binding.save.setOnClickListener {
            viewModel.savesList.observe(viewLifecycleOwner){
                adapter.updateMyPosts(it)
                binding.savesRv.adapter=adapter
            }
            binding.fotosRv.visibility=GONE
            binding.savesRv.visibility= VISIBLE
        }
        binding.myPhotos.setOnClickListener {
            viewModel.postsList.observe(viewLifecycleOwner) {
                adapter.updateMyPosts(it)
                binding.fotosRv.adapter = adapter
            }
            binding.fotosRv.visibility=VISIBLE
            binding.savesRv.visibility= GONE
        }



    }

    override fun onDestroy() {
        super.onDestroy()
        sp.edit().remove("profileid").apply()
    }







//    private fun myFotos(){
//        firestore.collection("Posts").addSnapshotListener { value, error ->
//            if (error != null) {
//                error.localizedMessage?.let { Log.e("", it) }
//            } else {
//
//                if (value != null) {
//                    for (document in value.documents) {
//
//                        try {
//                            val post_id = document.get("postId") as String
//                            val postImage = document.get("postImage") as String
//                            val description = document.get("description") as String
//                            val publisher = document.get("publisher") as String
//                            val time = document.get("time") as Timestamp
//                            val post = Posts(post_id, postImage, description, publisher, time)
//
//                            if (publisher==profileid){
//                                postList.add(post)
//                            }
//
//
//                        } catch (_: java.lang.NullPointerException) {
//
//
//                        }
//
//
//                    }
//                    postList.sortByDescending {
//                        it.time
//                    }
//                    adapter.notifyDataSetChanged()
//
//
//
//                }
//
//
//            }
//
//        }
//
//
//    }

    private fun userInfo() {

        firestore.collection("user").document(profileid!!).addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_SHORT).show()
            } else {
                if (value != null && value.exists()) {

                    val username = value.get("username") as String
                    val imageurl = value.get("image_url") as String
                    val bio = value.get("bio") as String
                    Picasso.get().load(imageurl).into(binding.imageProfile)
                    binding.username.text = username
                    binding.bio.text = bio


                }


            }

        }


    }

    private fun checkFollow() {

        firestore.collection("Follow").document(firbaseUser.uid)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    error.localizedMessage?.let {
                        Log.e("", it)
                        return@addSnapshotListener
                    }
                } else {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val follow = documentSnapshot.data

                        if (follow != null) {

                            try {
                                val following = follow["following"] as HashMap<*, *>
                                if (following.containsKey(profileid)) {

                                    binding.editProfil.text = "following"
                                } else {
                                    binding.editProfil.text = "follow"
                                }
                            } catch (_: java.lang.NullPointerException) {

                            }

                        }

                    } else {
                        Log.e("", "Document not fount")
                    }
                }
            }


    }

    private fun getFollower() {

        firestore.collection("Follow").document(profileid!!)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    error.localizedMessage?.let {
                        Log.e("", it)
                        return@addSnapshotListener
                    }
                } else {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val follow = documentSnapshot.data

                        if (follow != null) {

                            try {
                                val followers = follow["followers"] as HashMap<*, *>
                                binding.followers.text = followers.keys.count().toString()

                            } catch (_: java.lang.NullPointerException) {

                            }

                        }

                    } else {
                        Log.e("", "")
                    }
                }
            }



        firestore.collection("Follow").document(profileid!!)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    error.localizedMessage?.let {
                        Log.e("", it)
                        return@addSnapshotListener
                    }
                } else {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val follow = documentSnapshot.data

                        if (follow != null) {

                            try {
                                val following = follow["following"] as HashMap<*, *>
                                binding.following.text = following.keys.count().toString()

                            } catch (_: java.lang.NullPointerException) {

                            }

                        }

                    } else {
                        Log.e("", "")
                    }
                }
            }

    }

    private fun getNrPost() {
        firestore.collection("Posts").addSnapshotListener { value, error ->
            if (error != null) {
                error.localizedMessage?.let {
                    Log.e("", it)
                    return@addSnapshotListener
                }
            } else {
                if (value != null) {
                    var i = 0
                    for (doc in value.documents) {
                        if (profileid == doc.get("publisher").toString()) {
                            i++
                        }
                    }
                    binding.post.text = "$i"
                }
            }

        }


    }


}