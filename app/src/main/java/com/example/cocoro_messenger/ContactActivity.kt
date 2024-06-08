package com.example.cocoro_messenger

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cocoro_messenger.databinding.ActivityContactBinding

class ContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_contact)

        val token = intent.getStringExtra("token")

        if (token.isNullOrEmpty()){
            val intent = Intent(this, LoginActivity::class.java)
            Toast.makeText(this, "Session is expired", Toast.LENGTH_SHORT).show()
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Session is active: $token", Toast.LENGTH_SHORT).show()
        }

        binding = ActivityContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addFriendsBtn.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.add_friends_dialog, null)
            val builder = AlertDialog.Builder(this)
                .setView(dialogView)

            val addFriendsDialog = builder.create()
            addFriendsDialog.show()

            val width = (resources.displayMetrics.widthPixels * 0.8).toInt()
            val height = (resources.displayMetrics.heightPixels * 0.5).toInt()
            addFriendsDialog.window?.setLayout(width, height)

            val searchBtn = dialogView.findViewById<Button>(R.id.friends_search_btn)
            searchBtn.setOnClickListener {
                Toast.makeText(this, "search", Toast.LENGTH_SHORT).show()
            }

            val closeBtn = dialogView.findViewById<Button>(R.id.close_btn)
            closeBtn.setOnClickListener {
                Toast.makeText(this, "close", Toast.LENGTH_SHORT).show()
                addFriendsDialog.dismiss()
            }
        }

        val contactBtn = findViewById<Button>(R.id.contact_btn)
        val chatsBtn = findViewById<Button>(R.id.chat_btn)
        val mapBtn = findViewById<Button>(R.id.map_btn)
        val moreBtn = findViewById<Button>(R.id.more_btn)

        contactBtn.setOnClickListener {
            val intent = Intent(this, ContactActivity::class.java)
            intent.putExtra("token", token)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
