package com.example.cocoro_messenger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cocoro_messenger.databinding.ActivityContactBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactBinding
    private lateinit var friendAdapter: FriendAdapter
    private val friend = mutableListOf<Friend>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_contact)

        val token = intent.getStringExtra("token")
        val userEmail = intent.getStringExtra("userEmail")
        val friendsJson = intent.getStringExtra("friendsJson")

        if (token.isNullOrEmpty() || userEmail.isNullOrEmpty() || friendsJson.isNullOrEmpty()) {
            val intent = Intent(this, LoginActivity::class.java)
            Toast.makeText(this, "Session is expired", Toast.LENGTH_SHORT).show()
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Session is active: $token", Toast.LENGTH_SHORT).show()
        }

        binding = ActivityContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        friendAdapter = FriendAdapter(friend)
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = friendAdapter

        binding.addFriendBtn.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.add_friend_dialog, null)
            val builder = AlertDialog.Builder(this)
                .setView(dialogView)

            val addFriendDialog = builder.create()
            addFriendDialog.show()

            val width = (resources.displayMetrics.widthPixels * 0.8).toInt()
            val height = (resources.displayMetrics.heightPixels * 0.5).toInt()
            addFriendDialog.window?.setLayout(width, height)

            val searchBtn = dialogView.findViewById<Button>(R.id.friend_search_btn)
            searchBtn.setOnClickListener {
                val emailEditText = dialogView.findViewById<EditText>(R.id.friend_search_field)
                val emailInput = emailEditText.text.toString()
                if (emailInput.isNotEmpty()) {
                    searchUser(emailInput) { name, emailResult ->
                        if (userEmail != null) {
                            showSearchResultDialog(name, emailResult, userEmail)
                        }
                        addFriendDialog.dismiss()
                    }
                } else {
                    Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
                }
            }

            val closeBtn = dialogView.findViewById<Button>(R.id.close_btn)
            closeBtn.setOnClickListener {
                Toast.makeText(this, "close", Toast.LENGTH_SHORT).show()
                addFriendDialog.dismiss()
            }
        }

        val contactBtn = findViewById<Button>(R.id.contact_btn)
        val chatsBtn = findViewById<Button>(R.id.chat_btn)
        val mapBtn = findViewById<Button>(R.id.map_btn)
        val moreBtn = findViewById<Button>(R.id.more_btn)

        contactBtn.setOnClickListener {
            val intent = Intent(this, ContactActivity::class.java)
            intent.putExtra("token", token)
            intent.putExtra("userEmail", userEmail)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Load Friend list
        if (userEmail != null && friendsJson != null) {
            loadFriend(friendsJson)
        }
    }

    private fun searchUser(email: String, onSuccess: (String, String) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            val userSearch = UserSearch(email)
            val response = withContext(Dispatchers.IO) {
                try {
                    RetrofitClient.instance.searchUser(userSearch)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            if (response != null) {
                when (response.code()) {
                    200 -> {
                        val searchResponse = response.body()
                        if (searchResponse != null) {
                            onSuccess(searchResponse.name, searchResponse.email)
                        } else {
                            Toast.makeText(this@ContactActivity, "User not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                    400 -> {
                        Toast.makeText(this@ContactActivity, "Email is required", Toast.LENGTH_SHORT).show()
                    }
                    401 -> {
                        Toast.makeText(this@ContactActivity, "Invalid email", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(this@ContactActivity, "Unknown error", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this@ContactActivity, "Error searching user", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSearchResultDialog(name: String, email: String, userEmail: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.friend_search_result_dialog, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)

        val searchResultDialog = builder.create()
        searchResultDialog.show()

        val width = (resources.displayMetrics.widthPixels * 0.8).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.5).toInt()
        searchResultDialog.window?.setLayout(width, height)

        dialogView.findViewById<TextView>(R.id.search_result_name).text = "Name: $name"
        dialogView.findViewById<TextView>(R.id.search_result_email).text = "Email: $email"

        val addFriendBtn = dialogView.findViewById<Button>(R.id.add_friend_btn)
        addFriendBtn.setOnClickListener {
            addFriend(userEmail, email) {
                friendAdapter.addFriend(Friend(name, email))
            }
            searchResultDialog.dismiss()
        }

        val closeBtn = dialogView.findViewById<Button>(R.id.close_search_result_btn)
        closeBtn.setOnClickListener {
            searchResultDialog.dismiss()
        }
    }

    private fun addFriend(userEmail: String, friendEmail: String, onSuccess: () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            val addFriendRequest = AddFriend(userEmail, friendEmail)
            val response = withContext(Dispatchers.IO) {
                try {
                    RetrofitClient.instance.addFriend(addFriendRequest)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            if (response != null) {
                when (response.code()) {
                    200 -> {
                        Toast.makeText(this@ContactActivity, "Friend added successfully", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    }
                    400 -> {
                        Toast.makeText(this@ContactActivity, "User email and friend email are required", Toast.LENGTH_SHORT).show()
                    }
                    404 -> {
                        Toast.makeText(this@ContactActivity, "User or friend not found", Toast.LENGTH_SHORT).show()
                    }
                    500 -> {
                        Toast.makeText(this@ContactActivity, "Internal server error", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(this@ContactActivity, "Unknown error", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this@ContactActivity, "Error adding friend", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadFriend(friendsJson: String) {
        val gson = Gson()
        val friendsType = object : TypeToken<List<Friend>>() {}.type
        val friends: List<Friend> = gson.fromJson(friendsJson, friendsType)

        friendAdapter.updateFriend(friends)
    }
}
