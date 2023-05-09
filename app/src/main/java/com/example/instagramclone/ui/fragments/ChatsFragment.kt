package com.example.instagramclone.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramclone.databinding.FragmentChatsBinding
import com.example.instagramclone.ui.adapters.ChatAdapter
import com.example.instagramclone.ui.adapters.UserClickListener
import com.example.instagramclone.ui.viewmodel.ChatsViewModel
import com.example.instagramclone.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class ChatsFragment : Fragment() {

    private lateinit var binding: FragmentChatsBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val adapter by lazy {
        ChatAdapter(object : UserClickListener {
            override fun chatUserCLickListener(currentUser: String) {

                findNavController().navigate(
                    ChatsFragmentDirections.actionChatsFragmentToMessagesFragment3(
                        currentUser
                    )
                )
                firestore.collection("Chats").document(auth.currentUser!!.uid + currentUser)
                    .update("seen", true)

            }

        }, emptyList())
    }
    private val viewModel by activityViewModels<ChatsViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = Firebase.firestore
        auth = Firebase.auth

        binding.chatRv.layoutManager = LinearLayoutManager(requireActivity())
        binding.chatRv.adapter = adapter

        viewModel.getUsersId()
        viewModel.chatLiveData.observe(viewLifecycleOwner) {

            when(it){
                is Resource.Loading->{
                    binding.messageLottie.visibility=VISIBLE
                    binding.chatRv.visibility= GONE
                }
                is Resource.Success->{
                    binding.messageLottie.visibility= GONE
                    binding.chatRv.visibility= VISIBLE
                    it.data?.let { list -> adapter.updateChatList(list) }?:adapter.updateChatList(
                        emptyList()
                    )

                }
                is Resource.Error->{
                    Toast.makeText(requireContext(), it.data.toString(), Toast.LENGTH_SHORT).show()
                }
            }

        }


    }


}