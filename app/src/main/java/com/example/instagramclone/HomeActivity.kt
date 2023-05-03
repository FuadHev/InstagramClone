package com.example.instagramclone

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.instagramclone.databinding.ActivityHomeBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.onesignal.OneSignal
import org.json.JSONException
import org.json.JSONObject


class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

//    private val ONESIGNAL_APP_ID = "9b3b9701-9264-41ef-b08c-1c69f1fabfef"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val firebaseUser = Firebase.auth.currentUser

//        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
//
//        // OneSignal Initialization
//        OneSignal.initWithContext(this)
//        OneSignal.setAppId(ONESIGNAL_APP_ID)
//        // promptForPushNotifications will show the native Android notification permission prompt.
//        // We recommend removing the following code and instead using an In-App Message to prompt for notification permission (See step 7)
//        OneSignal.promptForPushNotifications();
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment

        NavigationUI.setupWithNavController(binding.bottomNav, navHostFragment.navController)

        navHostFragment.navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.heartFragment) {
                val badge=binding.bottomNav.getBadge(R.id.heartFragment)
                badge?.isVisible = false
                if (badge?.number !=null && badge.number >0 ){
                    Firebase.firestore.collection("NotificationCount")
                        .document(Firebase.auth.currentUser!!.uid).update("isawnotification",
                            FieldValue.increment(badge.number.toLong())
                        )
                }

            }

        }


        // get PlayerId
        val deviceState = OneSignal.getDeviceState()
        val userId = deviceState?.userId

        if(userId!=null){
            Firebase.firestore.collection("user").document(firebaseUser!!.uid)
                .update("playerId", userId)
        }
        Firebase.firestore.collection("user").document(firebaseUser!!.uid).update("online",true)



        notifiCount()
    }

    override fun onDestroy() {
        super.onDestroy()
        Firebase.firestore.collection("user").document(Firebase.auth.currentUser!!.uid).update("online",false)
    }

    fun notifiCount() {
        var nshow = 0
        val ref = Firebase.firestore.collection("NotificationCount")
            .document(Firebase.auth.currentUser!!.uid)
        // Bu hisseni axirda silmeliyem. Singupda yazilib.
//        val hmap = hashMapOf<String, Any>("isawnotification" to 0)
//        ref.set(hmap)
        ref.addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(this, error.localizedMessage, Toast.LENGTH_SHORT).show()
            } else {
                try {
                    if (value != null) {
                        val data = value.data as HashMap<*, *>
                        val count = data["isawnotification"] as Long
                        nshow = count.toInt()
                    }
                } catch (_: NullPointerException) {

                }


            }


        }

        Firebase.firestore.collection("Notification").document(Firebase.auth.currentUser!!.uid)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, error.localizedMessage, Toast.LENGTH_SHORT).show()
                } else {
                    try {
                        if (value != null) {

                            val allcomments = value.data as HashMap<*, *>
                            val count = allcomments.count()
                            val ncount = count - nshow

                            if(ncount!=0){
                                val badge = binding.bottomNav.getOrCreateBadge(R.id.heartFragment)
                                badge.isVisible = true
                                badge.number = ncount
                            }

                        }
                    } catch (_: NullPointerException) {

                    }


                }


            }
    }
}