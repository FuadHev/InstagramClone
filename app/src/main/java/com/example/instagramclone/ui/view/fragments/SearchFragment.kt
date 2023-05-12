package com.example.instagramclone.ui.view.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramclone.R
import com.example.instagramclone.base.BaseFragment
import com.example.instagramclone.model.Users
import com.example.instagramclone.databinding.FragmentSearchBinding
import com.example.instagramclone.ui.adapters.ClickListener
import com.example.instagramclone.ui.adapters.UserAdapter
import com.example.instagramclone.ui.viewmodel.SearchViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class SearchFragment : BaseFragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var firestore: FirebaseFirestore
    private val viewModel by viewModels<SearchViewModel>()
    private val adapter by lazy {
       UserAdapter(requireContext(),object : ClickListener {
            override fun userClickListener(bundle: Bundle) {
                val fNav=findNavController()
                if ( fNav.currentDestination?.id==R.id.searctoFragment){
                    fNav.navigate(R.id.action_searctoFragment_to_search_nav,bundle)
                }else if ( fNav.currentDestination?.id==R.id.followersFragment){
                    fNav.navigate(R.id.action_followersFragment_to_profileFragment,bundle)
                }
            }

        }, emptyList())
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun addObserves() {
        viewModel.userLiveData.observe(viewLifecycleOwner){
            adapter.updateUsers(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = Firebase.firestore
        binding.rv.setHasFixedSize(true)
        binding.rv.layoutManager = LinearLayoutManager(requireActivity())
        binding.rv.adapter = adapter
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.searchUsers(query.toString())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchUsers(newText.toString())
                return true
            }


        })
    }

}