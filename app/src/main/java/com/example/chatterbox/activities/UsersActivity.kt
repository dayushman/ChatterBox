package com.example.chatterbox.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.chatterbox.adapters.UserAdapter
import com.example.chatterbox.databinding.ActivityUsersBinding
import com.example.chatterbox.models.User
import com.example.chatterbox.utilities.Constants
import com.example.chatterbox.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore

class UsersActivity : AppCompatActivity() {

    lateinit var binding: ActivityUsersBinding
    lateinit var preferenceManager: PreferenceManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)

        setContentView(binding.root)
        preferenceManager = PreferenceManager(this.applicationContext)
        setListeners()
        getUsers()
    }

    private fun setListeners() {
        binding.imageBack.setOnClickListener { onBackPressed() }
    }

    private fun getUsers(){
        loading(true)
        val database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USERS)
            .get()
            .addOnCompleteListener { task->
                loading(false)
                val currentUserId = preferenceManager.getString(Constants.KEY_USER_ID)
                if (task.isSuccessful && task.result != null){
                    val users = ArrayList<User>()
                    task.result!!.forEach { queryDocumentSnapshot ->
                        val user = User(
                            queryDocumentSnapshot.getString(Constants.KEY_NAME).toString(),
                            queryDocumentSnapshot.getString(Constants.KEY_IMAGE).toString(),
                            queryDocumentSnapshot.getString(Constants.KEY_EMAIL).toString(),
                            queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN).toString(),
                        )
                        users.add(user)
                    }
                    if (users.isNotEmpty()){
                        val adaptor = UserAdapter(users)
                        binding.recyclerView.adapter = adaptor
                        binding.recyclerView.visibility = View.VISIBLE
                    }else{
                        showError()
                    }
                }else{
                    showError()
                }
            }
    }

    private fun showError(){
        binding.textErrorMessage.text = String.format("%s","No user available")
        binding.textErrorMessage.visibility = View.VISIBLE
    }

    private fun loading(isLoading : Boolean){
        if (isLoading){
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.progressBar.visibility = View.INVISIBLE
        }
    }
}