package com.example.instagramclone.ui.view.login_singup


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup

import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.instagramclone.ui.view.activity.HomeActivity
import com.example.instagramclone.R
import com.example.instagramclone.databinding.FragmentLoginBinding
import com.example.instagramclone.ui.view.login_singup.LoginFragmentDirections
import com.example.instagramclone.utils.PreferenceHelper
import com.example.instagramclone.utils.PreferenceHelper.set
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPreferences:SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        // Inflate the layout for this fragment
        val view = binding.root
        auth = Firebase.auth
        binding.loginFragment = this
        firestore = Firebase.firestore

        sharedPreferences=PreferenceHelper.getDefault(requireActivity())



        return view
    }

    fun forgotPassword() {
        findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
    }

    fun singIn(email: String, password: String) {

        val email1=binding.email.text.toString()
        val password1=binding.password.text.toString()

        if (email.trim() == "" || password.trim() == "") {
            Toast.makeText(requireContext(), "Enter Email and Password", Toast.LENGTH_SHORT).show()
        } else {
            binding.singIn.visibility= INVISIBLE
            binding.loadingLottie.visibility= VISIBLE

            auth.signInWithEmailAndPassword(email.trim(), password.trim()).addOnSuccessListener {
                sharedPreferences["email"] = email
                sharedPreferences["password"] = password

                activity?.let {
                    val intent = Intent(it, HomeActivity::class.java)
                    it.finish()
                    it.startActivity(intent)
                }
            }.addOnFailureListener {
                binding.singIn.visibility= VISIBLE
                binding.loadingLottie.visibility= GONE


                Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()

            }
        }


    }
    fun singUp() {
       findNavController().navigate(LoginFragmentDirections.goToSingUp())
    }


}