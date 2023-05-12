package com.example.instagramclone.ui.view.fragments

import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.instagramclone.ui.view.activity.MainActivity
import com.example.instagramclone.R
import com.example.instagramclone.base.BaseFragment
import com.example.instagramclone.databinding.FragmentProfileBinding
import com.example.instagramclone.ui.adapters.MyFotoAdapter
import com.example.instagramclone.ui.view.fragments.ProfileFragmentDirections
import com.example.instagramclone.ui.viewmodel.ProfileViewModel
import com.example.instagramclone.utils.Constant
import com.example.instagramclone.utils.PreferenceHelper
import com.example.instagramclone.utils.PreferenceHelper.set
import com.example.instagramclone.utils.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.onesignal.OneSignal
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class ProfileFragment : BaseFragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var firbaseUser: FirebaseUser
    private val adapter by lazy {
        MyFotoAdapter(emptyList())
    }
    private lateinit var firestore: FirebaseFirestore
    private var profileid: String? = null
    private val viewModel by viewModels<ProfileViewModel> {
        ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
    }
    private val arg = Bundle()
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
        binding.profileFragment=this
        profileid = args?.getString("profileid") ?: firbaseUser.uid
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar2)
        binding.fotosRv.setHasFixedSize(true)
        binding.fotosRv.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.fotosRv.adapter = adapter


        profileid?.let {
            viewModel.userInfo(
                it
            )
        }

        if (profileid == firbaseUser.uid) {
            binding.editProfil.text = "edit profile"
            binding.message.visibility = GONE
            binding.options.visibility = VISIBLE
            viewModel.mySaves()

        } else {
            viewModel.checkFollow(profileid!!)
//            checkFollow()
            binding.save.visibility = GONE
            binding.options.visibility = INVISIBLE

        }

        viewModel.getFollower(profileid!!)
        viewModel.getNrPost(profileid!!)

        profileid?.let { viewModel.myFotos(it) }




        binding.options.setOnClickListener {

            val popupMenu = PopupMenu(requireActivity(),binding.options)
            popupMenu.menuInflater.inflate(R.menu.option_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.log_out -> {
                        val sharedPreferences = PreferenceHelper.getDefault(requireActivity())
                        val intent = Intent(requireActivity(), MainActivity::class.java)

                        sharedPreferences["email"] = null
                        sharedPreferences["password"] = null
                        requireActivity().finish()
                        startActivity(intent)
                        true
                    }
                    R.id.settings -> {

                        true
                    }
                    else -> false
                }

            }
            popupMenu.show()
        }

        binding.message.setOnClickListener {
            findNavController().navigate(
                ProfileFragmentDirections.actionChatsFragmentToMessagesFragment(
                    profileid!!
                )
            )
        }
        binding.save.setOnClickListener {
            viewModel.savesList.value?.let { it1 -> adapter.updateMyPosts(it1) }
        }
        binding.myPhotos.setOnClickListener {
            viewModel.postsList.value?.data?.let { it1 -> adapter.updateMyPosts(it1) }
        }


    }
    fun goFollowing(){
        arg.putString("id", profileid)
        arg.putString("follow", "following")
        findNavController().navigate(R.id.action_profilfragment_to_followersFragment,arg)
    }
    fun goFollowers(){
        arg.putString("id", profileid)
        arg.putString("follow", "followers")
        findNavController().navigate(R.id.action_profilfragment_to_followersFragment,arg)
    }

     fun clickEProfileFollowbtn() {
        val btn = binding.editProfil.text.toString().lowercase()
        when (btn) {
            "edit profile" -> {
                findNavController()
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
            }
            "following" -> {
                firestore.collection("Follow").document(firbaseUser.uid)
                    .update("following.${profileid}", FieldValue.delete())
                firestore.collection("Follow").document(profileid!!)
                    .update("followers.${firbaseUser.uid}", FieldValue.delete())
            }
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
        notifi["notificationId"] = nKey.toString()
        notifi["time"] = Timestamp.now()
        notification[nKey.toString()] = notifi

        ref.set(notification, SetOptions.merge())
    }


    private fun getPlayerIdSendNotification(userId: String) {

        var username = ""
        var profileImage=""
        Firebase.firestore.collection("user").document(Firebase.auth.currentUser!!.uid).get()
            .addOnSuccessListener { value ->
                    if (value != null) {
                        username = value.get("username") as String
                        profileImage = value.get("image_url") as String
                    }

            }
        Firebase.firestore.collection("user").document(userId).get()
            .addOnSuccessListener { value ->

                if (value != null) {

                    val playerId = value.get("playerId") as String?
                    if (playerId != null) {
                        sentPushNotification(playerId, username,profileImage)
                    }

                }

        }

    }

    private fun sentPushNotification(playerId: String, username: String,profileImage:String) {
        try {
            val notificationContent = JSONObject(
                """{
        "app_id": "${Constant.APP_ID}", 
        "include_player_ids": ["$playerId"],
        "headings": {"en": "$username"},
        "contents": {"en": "started following you"},
        "large_icon": "$profileImage"
    }"""
            )
            OneSignal.postNotification(notificationContent, null)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

// bu kodu silmeliyem.viewmodelde yazdim
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

    override fun addObserves() {
        viewModel.postsList.observe(viewLifecycleOwner) {
            when(it){
                is Resource.Loading->{

                }
                is Resource.Success->{
                    it.data?.let { it1 -> adapter.updateMyPosts(it1) }
                    if (it.data==null||it.data.isEmpty()){
                        binding.noPostsYet.visibility=VISIBLE
                    }else{
                        binding.noPostsYet.visibility= GONE
                    }

                }
                is Resource.Error->{
                    Toast.makeText(requireActivity(), it.data.toString(), Toast.LENGTH_SHORT).show()
                }

            }

        }
        viewModel.userInfo.observe(viewLifecycleOwner) {
            binding.username.text = it.username
            binding.bio.text = it.bio
            Picasso.get().load(it.imageurl).into(binding.imageProfile)
        }

        viewModel.followCount.observe(viewLifecycleOwner) {
            binding.following.text = it.toString()
        }
        viewModel.followerCount.observe(viewLifecycleOwner) {
            binding.followers.text = it.toString()
        }
        viewModel.postCount.observe(viewLifecycleOwner) {
            binding.post.text = it.toString()
        }
        viewModel.checkFollowLiveData.observe(viewLifecycleOwner){
            binding.editProfil.text=it
        }
    }


}