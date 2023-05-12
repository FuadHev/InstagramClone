package com.example.instagramclone.ui.view.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.instagramclone.R
import com.example.instagramclone.databinding.FragmentForgotPasswordBinding
import com.google.android.gms.common.util.DataUtils
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class ForgotPasswordFragment : Fragment() {

    private lateinit var binding:FragmentForgotPasswordBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_forgot_password, container, false)
        binding.forgotFragment=this
        
        return binding.root
    }

    fun resetPassword(emailAddress:String){
        if (emailAddress.trim()!=""){
            Firebase.auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireActivity(), "E-mail sent successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("TAG", "Hata oluştu şifre sıfırlanamadı.", task.exception)
                    }
                }
        }else{
            Toast.makeText(requireActivity(), "Enter email", Toast.LENGTH_SHORT).show()

        }

    }

}