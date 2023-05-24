package com.example.instagramclone.ui.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.example.instagramclone.R
import com.example.instagramclone.databinding.ActivityChatBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatActivity : AppCompatActivity() {
    private lateinit var binding:ActivityChatBinding

    ////36562127-4f41522f3d336a3fe6b64a038
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this, R.layout.activity_chat)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }
    override fun onRestart() {
        super.onRestart()
        Firebase.firestore.collection("user").document(Firebase.auth.currentUser!!.uid)
            .update("online", true)
    }
    override fun onStop() {
        super.onStop()
        Firebase.firestore.collection("user").document(Firebase.auth.currentUser!!.uid)
            .update("online", false)
    }
}