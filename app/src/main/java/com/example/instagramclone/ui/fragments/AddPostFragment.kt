package com.example.instagramclone.ui.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher

import androidx.activity.result.contract.ActivityResultContracts

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.example.instagramclone.databinding.FragmentAddPostBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.*


class AddPostFragment : Fragment() {
    private lateinit var binding: FragmentAddPostBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionResultLauncher: ActivityResultLauncher<String>
    private lateinit var auth:FirebaseAuth
    private lateinit var firestore:FirebaseFirestore
    private lateinit var stroge:FirebaseStorage
    var selectPicture: Uri? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddPostBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        val view = binding.root
        auth=Firebase.auth
        firestore=Firebase.firestore
        stroge=Firebase.storage




        
        registerLauncher()

        binding.imageView.setOnClickListener {
            Log.e("", auth.currentUser?.uid.toString())
            selectImage(it)
        }


        binding.sharePost.setOnClickListener {

            upload(it)

        }











        return view
    }



    fun upload(view: View) {

        val uuid= UUID.randomUUID()
        val imageName="$uuid.jpg"

        val reference=stroge.reference
        val imageReference=reference.child("images/$imageName")

        if (selectPicture !=null){
            imageReference.putFile(selectPicture!!).addOnSuccessListener {


            }.addOnFailureListener{
                Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }


    }

    fun selectImage(view: View) {

        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Give permission") {

                        permissionResultLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }.show()
            } else {

                permissionResultLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)

            }

        } else {
            val intentToGallery =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)


        }


    }

    private fun registerLauncher() {

        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val intentFromResult = result.data
                    if (intentFromResult != null) {
                        selectPicture = intentFromResult.data
                        selectPicture?.let {
                            binding.imageView.setImageURI(it)
                        }
                    }
                }

            }

        permissionResultLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {

                if (it) {

                    val intentToGallery =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

                    activityResultLauncher.launch(intentToGallery)


                } else {


                    Toast.makeText(requireContext(), "Permission needed", Toast.LENGTH_SHORT).show()
                }


            }


    }


}