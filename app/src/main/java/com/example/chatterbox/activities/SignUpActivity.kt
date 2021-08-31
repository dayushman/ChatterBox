package com.example.chatterbox.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.chatterbox.databinding.ActivitySignUpBinding
import com.example.chatterbox.utilities.Constants
import com.example.chatterbox.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

class SignUpActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignUpBinding
    lateinit var preferenceManager: PreferenceManager

    private var encodedImage : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = PreferenceManager(applicationContext)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
    }

    private fun setListeners() {
        binding.textSignIn.setOnClickListener {
            startActivity(Intent(this,SignInActivity::class.java))
        }
        binding.buttonSignUp.setOnClickListener { v->
            if (isValidSignUpDetail()){
                signUp()
            }
        }
        binding.layoutImage.setOnClickListener { view->
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }
    }
    private fun showToast(message: String){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }

    private fun signUp(){
        loading(true)
        val database = FirebaseFirestore.getInstance()
        val map = HashMap<String,Any>()
        map[Constants.KEY_EMAIL] = binding.inputEmail.text.toString()
        map[Constants.KEY_NAME] = binding.inputName.text.toString()
        map[Constants.KEY_PASSWORD] = binding.inputPassword.text.toString()
        map[Constants.KEY_IMAGE] = encodedImage!!
        database.collection(Constants.KEY_COLLECTION_USERS)
            .add(map)
            .addOnSuccessListener {documentReference->
                loading(false)
                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true)
                preferenceManager.putString(Constants.KEY_USER_ID,documentReference.id)
                preferenceManager.putString(Constants.KEY_NAME,binding.inputName.text.toString())
                preferenceManager.putString(Constants.KEY_IMAGE,encodedImage!!)
                val intent = Intent(this,MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)

            }
            .addOnFailureListener {exception ->
                loading(false)
                showToast(exception.message.toString())
            }
    }

    private fun encodedImage(bitmap: Bitmap) : String{
        val previewWidth = 150
        val previewHeight = bitmap.height *previewWidth/bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream)
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes,Base64.DEFAULT)
    }

    private val activityResultCallback = ActivityResultCallback<ActivityResult> { result ->
            if (result?.resultCode == RESULT_OK){
                if (result.data != null){
                    val imageUrl = result.data!!.data!!
                    try {
                        val inputStream = contentResolver.openInputStream(imageUrl)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        binding.imageProfile.setImageBitmap(bitmap)
                        binding.textAddImage.visibility = View.GONE
                        encodedImage = encodedImage(bitmap)
                    }catch (e: FileNotFoundException){
                        e.printStackTrace()
                    }
                }
            }
        }

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        activityResultCallback
    )

    private fun isValidSignUpDetail() : Boolean{
        if (encodedImage == null){
            showToast("Please select an Image")
            return false
        }else if (binding.inputName.text.toString().trim().isEmpty()){
            showToast("Enter your name")
            return false
        }else if (binding.inputEmail.text.toString().trim().isEmpty()){
            showToast("Enter your email")
            return false
        }else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString().trim()).matches()){
            showToast("Enter valid email")
            return false
        }else if (binding.inputPassword.text.toString().trim().isEmpty()){
            showToast("Enter password")
            return false
        }else if (binding.inputConfirmPassword.text.toString().trim() != binding.inputPassword.text.toString().trim()){
            showToast("Passwords do not match")
            return false
        }else{
            return true
        }
    }

    private fun loading(isLoading : Boolean){
        if (isLoading){
            binding.buttonSignUp.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.buttonSignUp.visibility = View.VISIBLE
            binding.progressBar.visibility = View.INVISIBLE
        }
    }
}