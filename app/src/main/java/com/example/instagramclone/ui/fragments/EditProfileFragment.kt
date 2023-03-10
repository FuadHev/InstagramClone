package com.example.instagramclone.ui.fragments

import android.app.Activity
import android.app.ProgressDialog
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
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.example.instagramclone.R
import com.example.instagramclone.databinding.FragmentEditProfileBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.util.*

class EditProfileFragment : Fragment() {


    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionResultLauncher: ActivityResultLauncher<String>
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    var selectPicture: Uri? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_edit_profile, container,false)
        // Inflate the layout for this fragment
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage
        getUserInfo()


        registerLauncher()

        binding.changeProfilPhoto.setOnClickListener {
            selectImage(it)
        }
        binding.save.setOnClickListener {
            upload(it)
        }



    }

    fun getUserInfo(){
        firestore.collection("user").document(auth.currentUser!!.uid).addSnapshotListener { value, error ->
            if (error!=null){

            }else{
                val bio=value?.get("bio") as String
                val username= value.get("username") as String
                val image_url=value.get("image_url") as String

                Picasso.get().load(image_url).into(binding.profilImage)
                binding.bio.setText(bio)
                binding.username.setText(username)
                binding.profilName.text=username
            }
        }
    }




    fun upload(view: View) {

        val progress= ProgressDialog(requireContext())
        progress.setMessage("Please wait updating profile")
        progress.show()

        val uuid = UUID.randomUUID()
        val imageName = "$uuid.jpg"

        val reference = storage.reference
        val imageReference = reference.child("profilImage/$imageName")

        if (selectPicture != null) {
            imageReference.putFile(selectPicture!!).addOnSuccessListener {
                val uploadPictureReference = storage.reference.child("profilImage").child(imageName)
                uploadPictureReference.downloadUrl.addOnSuccessListener {
                    val downloadUrl = it.toString()
                    val ref=firestore.collection("user").document(auth.currentUser!!.uid)

                    ref.update("bio",binding.bio.text.toString())
                    ref.update("image_url",downloadUrl)
                    ref.update("username",binding.username.text.toString()).addOnSuccessListener {
                        progress.dismiss()
                    }

                }


            }.addOnFailureListener {
                Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }else{
            val ref=firestore.collection("user").document(auth.currentUser!!.uid)

            ref.update("bio",binding.bio.text.toString())
            ref.update("username",binding.username.text.toString()).addOnSuccessListener {
                progress.dismiss()
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
                if (result.resultCode == Activity.RESULT_OK) {
                    val intentFromResult = result.data

                    if (intentFromResult != null) {
                        selectPicture = intentFromResult.data
                        selectPicture?.let {
                            binding.profilImage.setImageURI(it)
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