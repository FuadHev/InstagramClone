package com.example.instagramclone.ui.view.fragments

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.opengl.Visibility
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.instagramclone.ui.view.activity.HomeActivity
import com.example.instagramclone.R
import com.example.instagramclone.databinding.FragmentLoginBinding
import com.example.instagramclone.ui.view.fragments.LoginFragmentDirections
import com.example.instagramclone.utils.PreferenceHelper
import com.example.instagramclone.utils.PreferenceHelper.get
import com.example.instagramclone.utils.PreferenceHelper.set
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.onesignal.OneSignal


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPreferences:SharedPreferences
    private val ONESIGNAL_APP_ID = "9b3b9701-9264-41ef-b08c-1c69f1fabfef"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        // Inflate the layout for this fragment
        val view = binding.root
        auth = Firebase.auth
        binding.loginFragment = this
        firestore = Firebase.firestore

        sharedPreferences=PreferenceHelper.getDefault(requireActivity())



        // bu one signal telefonun idsin qeyd etme hissesi 1 2 yerde yene yazilib harda stabil isleyirse onu saxlamaliyam,
        // burda heleki stabil isleyir
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

        // OneSignal Initialization
        OneSignal.initWithContext(requireContext())
        OneSignal.setAppId(ONESIGNAL_APP_ID)
        // promptForPushNotifications will show the native Android notification permission prompt.
        // We recommend removing the following code and instead using an In-App Message to prompt for notification permission (See step 7)
        OneSignal.promptForPushNotifications()


        return view
    }

    fun forgotPassword() {
        findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
    }

    fun singIn(email: String, password: String) {

//        val progress = ProgressDialog(requireContext())
//        progress.setMessage("Please wait")
        if (email.trim() == "" || password.trim() == "") {
            Toast.makeText(requireContext(), "Enter Email and Password ", Toast.LENGTH_SHORT).show()
//            progress.dismiss()

        } else {
            binding.singIn.visibility= INVISIBLE
            binding.loadingLottie.visibility= VISIBLE
//            progress.show()
            auth.signInWithEmailAndPassword(email.trim(), password.trim()).addOnSuccessListener {

                sharedPreferences["email"] = email
                sharedPreferences["password"] = password
//                progress.dismiss()

                activity?.let {
                    val intent = Intent(it, HomeActivity::class.java)
                    it.finish()
                    it.startActivity(intent)
                }

            }.addOnFailureListener {
//                progress.dismiss()
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