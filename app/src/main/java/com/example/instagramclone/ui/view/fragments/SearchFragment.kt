package com.example.instagramclone.ui.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramclone.R
import com.example.instagramclone.base.BaseFragment
import com.example.instagramclone.databinding.FragmentSearchBinding
import com.example.instagramclone.model.Posts
import com.example.instagramclone.ui.adapters.ClickListener
import com.example.instagramclone.ui.adapters.MyFotoAdapter
import com.example.instagramclone.ui.adapters.MyPostCLickListener
import com.example.instagramclone.ui.adapters.UserAdapter
import com.example.instagramclone.ui.viewmodel.SearchViewModel
import com.example.instagramclone.utils.Resource


class SearchFragment : BaseFragment() {

    private lateinit var binding: FragmentSearchBinding

    private val viewModel by viewModels<SearchViewModel>()
    private val fotoAdapter by lazy {
        MyFotoAdapter(object : MyPostCLickListener {
            override fun postClickListener(currentPosts: Posts, bundle: Bundle) {
                val fNav = findNavController()
                if (fNav.currentDestination?.id == R.id.profileFragment) {
                    findNavController().navigate(
                        R.id.action_profilfragment_to_profileDetailFragment,
                        bundle
                    )
                } else if (fNav.currentDestination?.id == R.id.searctoFragment) {
                    findNavController().navigate(SearchFragmentDirections.actionSearctoFragmentToDiscoverPostsFragment(currentPosts))
                }
            }

        }, emptyList())
    }
    private val userAdapter by lazy {
        UserAdapter(requireContext(), object : ClickListener {
            override fun userClickListener(bundle: Bundle) {
                val fNav = findNavController()
                if (fNav.currentDestination?.id == R.id.searctoFragment) {
                    fNav.navigate(R.id.action_searctoFragment_to_search_nav, bundle)
                } else if (fNav.currentDestination?.id == R.id.followersFragment) {
                    fNav.navigate(R.id.action_followersFragment_to_profileFragment, bundle)
                }
            }
        }, emptyList())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)

        return binding.root
    }

    override fun addObserves() {
        viewModel.userLiveData.observe(viewLifecycleOwner) {
            userAdapter.updateUsers(it)
        }
        viewModel.postMutableLiveData.observe(viewLifecycleOwner){
            when(it){
                is Resource.Loading->{

                }
                is Resource.Success->{
                    it.data?.let { it1 -> fotoAdapter.updateMyPosts(it1)}
                }
                is Resource.Error->{
                    Toast.makeText(requireContext(), it.data.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.readPost()
        setRecyclerView()
        setSearchViewLisneter()

    }

    private fun setSearchViewLisneter() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                binding.searchView.clearFocus()
                if (query.trim() != "") {
                    viewModel.searchUsers(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {

                if (newText.trim() == "") {
                    viewModel.userLiveData.postValue(emptyList())
                    binding.searchRv.visibility=GONE
                    binding.rv.visibility= VISIBLE
                } else {
                    viewModel.searchUsers(newText)
                    binding.rv.visibility=GONE
                    binding.searchRv.visibility= VISIBLE
                }
                return true
            }


        })
    }

    private fun setRecyclerView() {
        binding.rv.setHasFixedSize(true)
        binding.searchRv.setHasFixedSize(true)
        binding.rv.layoutManager = GridLayoutManager(requireActivity(),3)
        binding.searchRv.layoutManager = LinearLayoutManager(requireActivity(),)
        binding.searchRv.adapter=userAdapter
        binding.rv.adapter = fotoAdapter
    }

}