package com.example.chatterbox.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatterbox.databinding.ActivitySignInBinding
import com.example.chatterbox.utilities.Constants
import com.example.chatterbox.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore

class SignInActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignInBinding
    lateinit var preferenceManager : PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = PreferenceManager(applicationContext)
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            val intent = Intent(applicationContext,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()

    }

    private fun setListeners(){
        binding.textCreateNewAccount.setOnClickListener { v->
            startActivity(Intent(this,SignUpActivity::class.java))
        }
        binding.buttonSignIn.setOnClickListener { v->
            if (isValidSignInDetails()){
                signIn()
            }
        }
    }

    private fun showToast(message :String){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }

    private fun signIn(){
        loading(true)
        val database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USERS)
            .whereEqualTo(Constants.KEY_EMAIL,binding.inputEmail.text.toString())
            .whereEqualTo(Constants.KEY_PASSWORD,binding.inputPassword.text.toString())
            .get()
            .addOnCompleteListener { task->
                if (task.isComplete && task.result != null && task.result!!.documents.size > 0){
                    val documentSnapShot = task.result!!.documents[0]
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true)
                    preferenceManager.putString(Constants.KEY_USER_ID,documentSnapShot.id)
                    preferenceManager.putString(Constants.KEY_NAME,documentSnapShot.getString(Constants.KEY_NAME)!!)
                    preferenceManager.putString(Constants.KEY_IMAGE,documentSnapShot.getString(Constants.KEY_IMAGE)!!)

                    val intent = Intent(this,MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }else{
                    loading(false)
                    showToast("Unable to sign in")
                }
            }
            .addOnFailureListener { exception->
                loading(false)
                exception.printStackTrace()
            }
    }

    private fun loading(isLoading : Boolean){
        if (isLoading){
            binding.buttonSignIn.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.buttonSignIn.visibility = View.VISIBLE
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    private fun isValidSignInDetails() : Boolean{
        if (binding.inputEmail.text.toString().trim().isEmpty()){
            showToast("Enter your email")
            return false
        }else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString().trim()).matches()){
            showToast("Enter valid Email Id")
            return false
        }else if (binding.inputPassword.text.toString().trim().isEmpty()){
            showToast("Enter your password")
            return false
        }else{
            return true
        }
    }


}