package com.example.instagramclone.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.instagramclone.MainActivity
import com.example.instagramclone.R
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
import com.onesignal.OneSignal
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var firbaseUser: FirebaseUser
    private  val adapter by lazy{
        MyFotoAdapter(emptyList())
    }
    private lateinit var firestore: FirebaseFirestore
    private var profileid: String? = null
    private lateinit var viewModel: ProfileViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tempViewModel: ProfileViewModel by viewModels()
        viewModel = tempViewModel

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firbaseUser = Firebase.auth.currentUser!!
        firestore = Firebase.firestore


        val args = arguments
        profileid = args?.getString("profileid") ?: firbaseUser.uid


        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar2)

        binding.fotosRv.visibility = VISIBLE
        binding.savesRv.visibility = GONE
        binding.fotosRv.setHasFixedSize(true)
        binding.fotosRv.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.savesRv.layoutManager = GridLayoutManager(requireContext(), 3)

        viewModel.postsList.observe(viewLifecycleOwner) {
            adapter.updateMyPosts(it)
            binding.fotosRv.adapter = adapter
        }

        profileid?.let {
            viewModel.userInfo(
                requireContext(),
                it,
                binding.username,
                binding.bio,
                binding.imageProfile
            )
        }
        getFollower()
        getNrPost()
        profileid?.let { viewModel.myFotos(firestore, it) }
        viewModel.mySaves(firestore, firbaseUser)


        if (profileid == firbaseUser.uid) {
            binding.editProfil.text = "edit profile"
            binding.message.visibility=GONE

        } else {
            checkFollow()
            binding.save.visibility = GONE
        }

        binding.options.setOnClickListener {

            val popupMenu=PopupMenu(requireActivity(),binding.options)

            popupMenu.menuInflater.inflate(R.menu.option_menu,popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { item->

            when(item.itemId){
                R.id.log_out-> {
                    val intent =Intent(requireActivity(),MainActivity::class.java)
                    requireActivity().finish()
                    startActivity(intent)
                    true
                }
                else-> false
            }

            }

            popupMenu.show()

        }


        binding.message.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionChatsFragmentToMessagesFragment(profileid!!))
        }

        binding.editProfil.setOnClickListener {
            val btn = binding.editProfil.text.toString().lowercase()
            when (btn) {
                "edit profile" -> {
                    Navigation.findNavController(it)
                        .navigate(R.id.action_profilfragment_to_editProfileFragment)
                }
                "follow" -> {

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


                    addNotification()
                    getPlayerIdSendNotification(profileid!!)



                    binding.editProfil.text = "following"

                }
                "following" -> {
                    firestore.collection("Follow").document(firbaseUser.uid)
                        .update("following.${profileid}", FieldValue.delete())
                    firestore.collection("Follow").document(profileid!!)
                        .update("followers.${firbaseUser.uid}", FieldValue.delete())
                    binding.editProfil.text = "follow"
                }
            }


        }
        binding.save.setOnClickListener {
            viewModel.savesList.observe(viewLifecycleOwner) {
                adapter.updateMyPosts(it)
                binding.savesRv.adapter = adapter
            }
            binding.fotosRv.visibility = GONE
            binding.savesRv.visibility = VISIBLE
        }
        binding.myPhotos.setOnClickListener {
            viewModel.postsList.observe(viewLifecycleOwner) {
                adapter.updateMyPosts(it)
                binding.fotosRv.adapter = adapter
            }
            binding.fotosRv.visibility = VISIBLE
            binding.savesRv.visibility = GONE
        }

        binding.following.setOnClickListener {


            val arg = Bundle()

            arg.putString("id", profileid)
            arg.putString("follow", "following")

            Navigation.findNavController(it)
                .navigate(R.id.action_profilfragment_to_followersFragment, arg)

        }

        binding.followers.setOnClickListener {

            val arg = Bundle()

            arg.putString("id", profileid)
            arg.putString("follow", "followers")


            Navigation.findNavController(it)
                .navigate(R.id.action_profilfragment_to_followersFragment, arg)

        }


    }


    private fun addNotification() {

        val ref = Firebase.firestore.collection("Notification").document(profileid!!)
        val nKey = UUID.randomUUID()
        val notification = hashMapOf<String, Any>()
        val notifi = hashMapOf<String, Any>()
        notifi["userId"] = Firebase.auth.currentUser!!.uid
        notifi["nText"] = "started following you"
        notifi["postId"] = ""
        notifi["isPost"] = false
        notifi["notificationId"]=nKey.toString()
        notifi["time"] = Timestamp.now()

        notification[nKey.toString()] = notifi

        ref.set(notification, SetOptions.merge())
    }


    fun getPlayerIdSendNotification(userId: String) {

        var username = ""
        Firebase.firestore.collection("user").document(Firebase.auth.currentUser!!.uid)
            .addSnapshotListener { value, error ->
                if (error != null) {

                } else {
                    if (value != null) {
                        username = value.get("username") as String
                    }
                }
            }
        Firebase.firestore.collection("user").document(userId).addSnapshotListener { value, error ->
            if (error != null) {

            } else {
                if (value != null) {

                    val playerId = value.get("playerId") as String?
                    if (playerId != null) {
                        sentPushNotification(playerId, username)
                    }

                }
            }
        }

    }

    private fun sentPushNotification(playerId: String, username: String) {
        try {

            OneSignal.postNotification(
                JSONObject(
                    """{
          "contents": {"en": "started following you"},
          "include_player_ids": ["$playerId"],
          "headings": {"en": "$username"}
                  }
        """.trimIndent()
                ),
                null
            )


        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }


    private fun checkFollow() {
        binding.editProfil.text = "follow"

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