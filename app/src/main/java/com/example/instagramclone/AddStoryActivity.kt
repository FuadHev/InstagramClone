package com.example.instagramclone

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.example.instagramclone.databinding.ActivityAddStoryBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ServerValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.*

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionResultLauncher: ActivityResultLauncher<String>
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    var selectPicture: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage


        registerLauncher()

        binding.imageView.setOnClickListener {
            selectImage(it)
        }


        binding.imageViewLottie.setOnClickListener {
            selectImage(it)
        }

        binding.sharePost.setOnClickListener {

            upload(it)

        }

    }


    fun upload(view: View) {

        val progress = ProgressDialog(this)
        progress.setMessage("Please wait adding the post")
        progress.show()
        val uuid = UUID.randomUUID()
        val imageName = "$uuid.jpg"

        val reference = storage.reference
        val imageReference = reference.child("story/$imageName")

        if (selectPicture != null) {
            imageReference.putFile(selectPicture!!).addOnSuccessListener {
                val uploadPictureReference = storage.reference.child("story").child(imageName)
                uploadPictureReference.downloadUrl.addOnSuccessListener {
                    val downloadUrl = it.toString()

                    val ramdonkey = UUID.randomUUID().toString()
                    val myId = Firebase.auth.currentUser!!.uid
                    val ref = firestore.collection("Story").document(myId)
                    val hmapkey = hashMapOf<String, Any>()
                    val hmap = hashMapOf<String, Any>()
                    val timeEnd=System.currentTimeMillis()+86400000

                    hmap["imageurl"]=downloadUrl
                    hmap["timeStart"]=System.currentTimeMillis()
                    hmap["timeEnd"]=timeEnd
                    hmap["storyId"]=ramdonkey
                    hmap["userId"]=myId

                    hmapkey[ramdonkey]=hmap

                    ref.set(hmapkey, SetOptions.merge())
                    progress.dismiss()

                }


            }.addOnFailureListener {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        } else {
            progress.dismiss()
            Toast.makeText(this, "Please select photo", Toast.LENGTH_SHORT).show()
        }


    }

    fun selectImage(view: View) {

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
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
                            binding.imageView.visibility=View.VISIBLE
                            binding.imageViewLottie.visibility=View.GONE
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

                    Toast.makeText(this, "Permission needed", Toast.LENGTH_SHORT).show()

                }


            }


    }
}