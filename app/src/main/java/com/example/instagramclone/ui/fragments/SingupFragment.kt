package com.example.instagramclone.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.instagramclone.HomeActivity
import com.example.instagramclone.R
import com.example.instagramclone.data.entity.Users
import com.example.instagramclone.databinding.FragmentSingupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.checkerframework.checker.units.qual.K


class SingupFragment : Fragment() {

    private lateinit var binding: FragmentSingupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_singup, container, false)
        val view = binding.root
        binding.singUpFragment = this
        firestore = Firebase.firestore
        auth = Firebase.auth







        return view
    }

    fun singUp(email: String, userName: String, password: String) {


        if (email == "" || userName == "" || password == "") {

            Toast.makeText(requireContext(), "Enter all informations", Toast.LENGTH_SHORT).show()

        } else {

            auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
                creatAccount(email, userName, password)


            }.addOnFailureListener {
                Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
            }

        }


    }


    fun creatAccount(email: String, userName: String, password: String) {
        if (auth.currentUser != null) {

            val ref = Firebase.firestore.collection("NotificationCount")
                .document(Firebase.auth.currentUser!!.uid)
            val hmap = hashMapOf<String, Any>("isawnotification" to 0)
            ref.set(hmap)

            val userMap = hashMapOf<String, Any>()

            userMap["user_id"] = auth.currentUser!!.uid
            userMap["email"] = email
            userMap["username"] = userName
            userMap["password"] = password
            userMap["image_url"] =
                "https://firebasestorage.googleapis.com/v0/b/instagramclone-9f5ee.appspot.com/o/defaultimage%2Fdefaultimage.png?alt=media&token=525e573c-6b43-4730-b4a2-17b30de66234"
            userMap["bio"] = ""

            firestore.collection("user").document(auth.currentUser!!.uid).set(userMap)
                .addOnSuccessListener {

                    activity?.let {
                        val intent = Intent(it, HomeActivity::class.java)
                        it.startActivity(intent)
                        it.finish()

                    }


                }.addOnFailureListener {
                    Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
                }


        }


    }


}


