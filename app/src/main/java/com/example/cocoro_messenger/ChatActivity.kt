package com.example.cocoro_messenger

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.cocoro_messenger.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat)

        // 추가 설정
        val contactBtn = binding.contactBtn
        val chatBtn = binding.chatBtn

        contactBtn.setOnClickListener {
            // ContactActivity로 이동
            val intent = Intent(this, ContactActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_up, R.anim.slide_down)
            startActivity(intent, options.toBundle())
        }

        chatBtn.setOnClickListener {
            // ChatActivity로 이동
            val intent = Intent(this, ChatActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_up, R.anim.slide_down)
            startActivity(intent, options.toBundle())
        }

        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", null)
        val userEmail = sharedPref.getString("userEmail", null)

        if (token.isNullOrEmpty() || userEmail.isNullOrEmpty()) {
            val intent = Intent(this, LoginActivity::class.java)
            Toast.makeText(this, "Session is expired", Toast.LENGTH_SHORT).show()
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Session is active: $token", Toast.LENGTH_SHORT).show()
        }



    }
}