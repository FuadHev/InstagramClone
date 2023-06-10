package com.example.instagramclone.ui.view.activity

import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.instagramclone.R
import com.example.instagramclone.databinding.ActivityHomeBinding
import com.example.instagramclone.utils.Constant
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.onesignal.OneSignal


class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private val ONESIGNAL_APP_ID = Constant.APP_ID
    private lateinit var firebaseUser: FirebaseUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseUser = Firebase.auth.currentUser!!
        setBottomNavBar()
        setOnesingnal()
        setPlayerId()
        Firebase.firestore.collection("user").document(firebaseUser.uid).update("online", true)

        notifiCount()
    }

    private fun setOnesingnal() {

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

        val isOneSignalInitialized = OneSignal.getDeviceState() != null
        if (isOneSignalInitialized) {
            // OneSignal zaten başarıyla başlatıldıysa burada gerekli işlemleri yapabilirsiniz
            OneSignal.promptForPushNotifications()
        } else {
            // OneSignal henüz başlatılmadıysa init işlemini gerçekleştirin
            OneSignal.initWithContext(this)
            OneSignal.setAppId(ONESIGNAL_APP_ID)
            OneSignal.promptForPushNotifications()
        }


//        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
//        OneSignal.initWithContext(this)
//        OneSignal.setAppId(ONESIGNAL_APP_ID)
//        OneSignal.promptForPushNotifications();
    }

    private fun setPlayerId() {
        val deviceState = OneSignal.getDeviceState()
        val userId = deviceState?.userId
        if (userId != null) {
            Firebase.firestore.collection("user").document(firebaseUser.uid)
                .update("playerId", userId)
        }

    }

    private fun setBottomNavBar() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        NavigationUI.setupWithNavController(binding.bottomNav, navHostFragment.navController)

        navHostFragment.navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.heartFragment -> {
                    val badge = binding.bottomNav.getBadge(R.id.heartFragment)
                    badge?.isVisible = false
                    if (badge?.number != null && badge.number > 0) {
                        Firebase.firestore.collection("NotificationCount")
                            .document(Firebase.auth.currentUser!!.uid).update(
                                "isawnotification",
                                FieldValue.increment(badge.number.toLong())
                            )
                    }
                }
                R.id.commentsFragment -> {
                    binding.bottomNav.visibility = GONE
                }
                else -> {
                    binding.bottomNav.visibility = VISIBLE
                }

            }

        }
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

    fun notifiCount() {
        var nshow = 0
        val ref = Firebase.firestore.collection("NotificationCount")
            .document(Firebase.auth.currentUser!!.uid)
        // Bu hisseni axirda silmeliyem. Singupda yazilib.// evvel notification bolmesi yoxuydu deye diger userlerde bu yoxdu
//        val hmap = hashMapOf<String, Any>("isawnotification" to 0)
//        ref.set(hmap)
        ref.addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(this, error.localizedMessage, Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            try {
                if (value != null) {
                    val data = value.data as? HashMap<*, *>
                    val count = data?.get("isawnotification") as Long
                    nshow = count.toInt()
                }
            } catch (e: NullPointerException) {
                e.localizedMessage?.let { Log.e("IsawNotifi", it) }

            }

        }
        Firebase.firestore.collection("Notification").document(Firebase.auth.currentUser!!.uid)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, error.localizedMessage, Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                try {
                    if (value != null) {
                        val allcomments = value.data as? HashMap<*,*>
                        val count = allcomments?.count()?:0
                        val ncount = count.minus(nshow)

                        if (ncount != 0) {
                            val badge = binding.bottomNav.getOrCreateBadge(R.id.heartFragment)
                            badge.isVisible = true
                            badge.number = ncount
                        }
                    }
                } catch (e: NullPointerException) {
                    Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()

                }

            }
    }
}