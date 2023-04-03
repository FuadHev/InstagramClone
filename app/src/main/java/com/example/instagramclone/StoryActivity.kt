package com.example.instagramclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Toast
import com.example.instagramclone.data.entity.Comment
import com.example.instagramclone.databinding.ActivityStoryBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import com.squareup.picasso.Picasso
import jp.shts.android.storiesprogressview.StoriesProgressView

class StoryActivity : AppCompatActivity(), StoriesProgressView.StoriesListener {
    private lateinit var binding: ActivityStoryBinding
    private var counter = 0
    private var prestime: Long = 0L
    private var limit: Long = 500
    private lateinit var imageList: ArrayList<String>
    private lateinit var storyIds: ArrayList<String>
    private lateinit var userId: String
    private lateinit var storiesProgressView:StoriesProgressView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        storiesProgressView=binding.stories

        imageList= ArrayList()
        storyIds= ArrayList()

        userId = intent.getStringExtra("userId") as String

        getStories(userId)
        getUserInfo(userId)
        binding.skip.setOnClickListener {
            binding.stories.skip()
        }
        binding.reverse.setOnClickListener {
            binding.stories.reverse()
        }
        binding.skip.setOnTouchListener(onTouchListener)
        binding.reverse.setOnTouchListener(onTouchListener)


    }


    private fun getUserInfo(userId:String){

        Firebase.firestore.collection("user").document(userId).addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(this, error.localizedMessage, Toast.LENGTH_SHORT).show()
            } else {
                if (value != null && value.exists()) {

                    val username = value.get("username") as String
                    val imageurl = value.get("image_url") as String
                    Picasso.get().load(imageurl).into(binding.storyPhoto)
                    binding.storyUsername.text=username
                }


            }

        }




    }

    private fun addView(storyId:String){
        Firebase.firestore.collection("Story").document(userId).update("$storyId.views.${Firebase.auth.currentUser!!.uid}",true)
    }

    private fun getStories(userId:String){

        val ref=Firebase.firestore.collection("Story").document(userId)


        ref.addSnapshotListener { value, error ->
            if (error != null) {
            } else {
                if (value != null && value.exists()) {
                    imageList.clear()
                    storyIds.clear()
                    val doc = value.data as HashMap<*, *>
                    try {

                        val timecurrent=System.currentTimeMillis()
                        for (i in doc) {
                            val story = i.value as HashMap<*, *>
                            val timestart= story["timeStart"] as Long
                            val timeend= story["timeEnd"] as Long
                            val imageurl= story["imageurl"] as String
                            val storyId= story["storyId"] as String

                            if (timecurrent>timestart&&timecurrent<timeend){
                                imageList.add(imageurl)
                                storyIds.add(storyId)
                            }
                        }
                        imageList.reverse()
                        storyIds.reverse()

                        storiesProgressView.setStoriesCount(imageList.size)
                        storiesProgressView.setStoryDuration(5000L)
                        storiesProgressView.setStoriesListener(this)
                        storiesProgressView.startStories(counter)

                        Picasso.get().load(imageList[counter]).into(binding.image)

                        addView(storyIds[counter])




                    } catch (_: java.lang.NullPointerException) {


                    }


                }

            }


        }

    }



    private val onTouchListener = object : OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    prestime = System.currentTimeMillis()
                    storiesProgressView.pause()
                    return false
                }
                MotionEvent.ACTION_UP -> {
                    val now = System.currentTimeMillis()
                    storiesProgressView.resume()
                    return limit < now - prestime
                }

            }


            return false
        }

    }


    override fun onNext() {
        Picasso.get().load(imageList[++counter]).into(binding.image)
        addView(storyIds[counter])
    }

    override fun onPrev() {
        if (counter-1<0) return
        Picasso.get().load(imageList[--counter]).into(binding.image)
    }

    override fun onComplete() {
        finish()
    }

    override fun onDestroy() {
        storiesProgressView.destroy()
        super.onDestroy()
    }

    override fun onPause() {
        storiesProgressView.pause()
        super.onPause()
    }

    override fun onRestart() {
        storiesProgressView.resume()
        super.onRestart()
    }

}