package com.example.instagramclone.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.example.instagramclone.HomeActivity
import com.example.instagramclone.R
import com.example.instagramclone.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.util.Objects
import kotlin.random.Random


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var auth:FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_login,container,false)
        // Inflate the layout for this fragment
        val view=binding.root
        auth= Firebase.auth

        firestore=Firebase.firestore

        binding.loginFragment=this







        return view
    }

    fun singIn(btn:Button,email:String,password:String){




        if (email == "" || password==""){
            Toast.makeText(requireContext(), "Enter Email and Password ", Toast.LENGTH_SHORT).show()

        }else{

            auth.signInWithEmailAndPassword(email,password).addOnSuccessListener {

                activity?.let {
                    val intent= Intent(it,HomeActivity::class.java)
                    it.startActivity(intent)

                }

            }.addOnFailureListener {
                Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()

            }


        }



    }

    fun singUp(sing:TextView){
        Navigation.findNavController(sing).navigate(R.id.goToSingUp)

    }




}