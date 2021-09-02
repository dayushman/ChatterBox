package com.example.chatterbox.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatterbox.databinding.ItemContainerUserBinding
import com.example.chatterbox.models.User

class UserAdapter(private val users: List<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemContainerUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.setUser(users[position])
    }

    override fun getItemCount(): Int {
        return users.size
    }

    class UserViewHolder(private val binding: ItemContainerUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setUser(user : User){
            binding.textName.text = user.name
            binding.textEmail.text = user.email
            binding.imageProfile.setImageBitmap(getBitmap(user.image))
        }
        private fun getBitmap(encodedImage : String) : Bitmap{
            val bytes = Base64.decode(encodedImage,Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes,0,bytes.size)
        }
    }

}