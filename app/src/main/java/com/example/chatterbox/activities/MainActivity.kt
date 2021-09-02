package com.example.chatterbox.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatterbox.databinding.ActivityMainBinding
import com.example.chatterbox.utilities.Constants
import com.example.chatterbox.utilities.PreferenceManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    lateinit var preferenceManager : PreferenceManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        loadUserDetails()
        getToken()
        setListeners()

    }

    private fun setListeners(){
        binding.imageSignOut.setOnClickListener{
            signOut()
        }
        binding.fabNewChat.setOnClickListener {
            startActivity(Intent(this,UsersActivity::class.java))
        }
    }

    private fun loadUserDetails(){
        binding.textName.text = preferenceManager.getString(Constants.KEY_NAME)
        val bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.size)
        binding.imageProfile.setImageBitmap(bitmap)
    }
    private fun showToast(message :String){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
    }
    private fun getToken() = FirebaseMessaging.getInstance().token.addOnSuccessListener(this::updateToken)
    private fun updateToken(token :String){
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
            .document(preferenceManager.getString(Constants.KEY_USER_ID)!!)
        documentReference.update(Constants.KEY_FCM_TOKEN,token)
            .addOnFailureListener { e-> showToast("Unable to update token") }
    }

    private fun signOut(){
        showToast("Signing out...")
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
            .document(preferenceManager.getString(Constants.KEY_USER_ID)!!)

        val updates = HashMap<String,Any>()
        updates[Constants.KEY_FCM_TOKEN] = FieldValue.delete()
        documentReference.update(updates)
            .addOnSuccessListener {
                preferenceManager.clear()
                startActivity(Intent(this,SignInActivity::class.java))
                finish()
            }
            .addOnFailureListener { exception->
                showToast("Unable to sign out")
            }
    }
}