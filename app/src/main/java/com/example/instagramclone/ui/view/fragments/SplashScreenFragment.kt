package com.example.instagramclone.ui.view.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.instagramclone.R
import com.example.instagramclone.databinding.FragmentSplashScreenBinding
import com.example.instagramclone.ui.view.fragments.SplashScreenFragmentDirections
import com.example.instagramclone.ui.view.activity.HomeActivity
import com.example.instagramclone.utils.PreferenceHelper
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase


@SuppressLint("CustomSplashScreen")
class SplashScreenFragment : Fragment() {

    private lateinit var binding: FragmentSplashScreenBinding
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_splash_screen, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = PreferenceHelper.getDefault(requireActivity())

        val email = sharedPreferences.getString("email", null)
        val password = sharedPreferences.getString("password", null)


        if (email != null && password != null) {
            Firebase.auth.signInWithEmailAndPassword(email.trim(), password.trim())
                .addOnSuccessListener {

                    Handler(Looper.getMainLooper()).postDelayed({
                        activity?.let {
                            val intent = Intent(it, HomeActivity::class.java)
                            it.finish()
                            it.startActivity(intent)
                        }
                    }, 900)
                }.addOnFailureListener {
                    binding.lottieAnim.setAnimation(R.raw.lost_connection)
                    binding.lottieAnim.playAnimation()
                    Toast.makeText(requireActivity(), it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
        } else {

            Handler(Looper.getMainLooper()).postDelayed({
                findNavController().navigate(SplashScreenFragmentDirections.actionSplashScreenFragmentToLoginFragment())
            }, 2000)

        }


    }

}