package com.example.instagramclone.ui.view.login_singup


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.instagramclone.R
import com.example.instagramclone.databinding.FragmentSingupBinding
import com.example.instagramclone.ui.view.login_singup.SingupFragmentDirections
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar


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

        if (email.trim() == "" || userName.trim() == "" || password.trim() == "") {

            Toast.makeText(requireContext(), "Enter all informations", Toast.LENGTH_SHORT).show()

        } else {
            binding.progressBar3.visibility=VISIBLE
            auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
                creatAccount(email, userName, password)
            }.addOnFailureListener {
                binding.progressBar3.visibility= GONE
                Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
            }

        }


    }


    private fun creatAccount(email: String, userName: String, password: String) {
        if (auth.currentUser != null) {
            val ref = Firebase.firestore.collection("NotificationCount")
                .document(Firebase.auth.currentUser!!.uid)
            // gorulen notification sayi  ucun data yaradir
            val hmap = hashMapOf<String, Any>("isawnotification" to 0)
            ref.set(hmap)
            // follow documenti yaradilir
            val refFollow=firestore.collection("Follow").document(auth.currentUser!!.uid)
            refFollow.set({})
            refFollow.update("arity", FieldValue.delete())

            // user collectionunda melumatlari yaradiram
            val userMap = hashMapOf<String, Any>()
            userMap["user_id"] = auth.currentUser!!.uid
            userMap["email"] = email
            userMap["username"] = userName.trim()
            userMap["password"] = password.trim()
            userMap["online"] = false
            userMap["image_url"] =
                "https://firebasestorage.googleapis.com/v0/b/instagramclone-9f5ee.appspot.com/o/defaultimage%2Fdefaultimage.png?alt=media&token=525e573c-6b43-4730-b4a2-17b30de66234"
            userMap["playerId"] = ""
            userMap["bio"] = ""
            firestore.collection("user").document(auth.currentUser!!.uid).set(userMap)
                .addOnSuccessListener {
                    binding.progressBar3.visibility= GONE
                    Toast.makeText(
                        requireContext(),
                        "User successfully created",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(SingupFragmentDirections.actionSingupFragmentToLoginFragment())

                }.addOnFailureListener {
                    binding.progressBar3.visibility= GONE
                    Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
                }


        }


    }


}


