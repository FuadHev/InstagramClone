package com.example.instagramclone

import android.app.Application
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MyApp:Application() {

//
//    companion object {
//        private const val TAG = "MyApp"
//        private const val USER_COLLECTION = "users"
//        private const val ONLINE_FIELD = "online"
//        private const val LAST_SEEN_FIELD = "lastSeen"
//    }
//
//
//    private  val userRef=Firebase.firestore.collection(USER_COLLECTION).document(Firebase.auth.currentUser!!.uid)
//
//    override fun onCreate() {
//        super.onCreate()
//
//        // Listen for changes in the user's online status
//        val listener = userRef.addSnapshotListener { snapshot, e ->
//            if (e != null) {
//                Log.w(TAG, "Listen failed", e)
//                return@addSnapshotListener
//            }
//
//            if (snapshot != null && snapshot.exists()) {
////                val isOnline = snapshot.getBoolean(ONLINE_FIELD) ?: false
//                userRef.update(ONLINE_FIELD,true)
//                // Do something with the online status
//            } else {
//                Log.d(TAG, "Current data: null")
//            }
//        }
//    }
//
//    override fun onTerminate() {
//        super.onTerminate()
//
//        // Set the user's online status to false
//        userRef.update(ONLINE_FIELD, false)
//
//        // Set the user's last seen time to the current time
////        val currentTime = FieldValue.serverTimestamp()
////        userRef.update(LAST_SEEN_FIELD, currentTime)
////            .addOnSuccessListener {
////                Log.d(TAG, "Last seen time updated")
////            }
////            .addOnFailureListener { e ->
////                Log.w(TAG, "Error updating last seen time", e)
////            }
//    }
}