package com.example.cocoro_messenger

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cocoro_messenger.databinding.ActivityContactBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.socket.client.Socket
import io.socket.client.IO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URISyntaxException

class ContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactBinding
    private lateinit var friendAdapter: FriendAdapter
    private val friends = mutableListOf<Friend>()
    private lateinit var mSocket: Socket
    private val TAG = "ContactActivity"
    private lateinit var dbHelper: FriendSQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = FriendSQLiteHelper(this)

        val contactBtn = binding.contactBtn
        val chatBtn = binding.chatBtn
        val mapBtn = binding.mapBtn
        val moreBtn = binding.moreBtn

        contactBtn.setOnClickListener {
            val intent = Intent(this, ContactActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_up, R.anim.slide_down)
            startActivity(intent, options.toBundle())
        }

        chatBtn.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_up, R.anim.slide_down)
            startActivity(intent, options.toBundle())
        }

        // SharedPreferences에서 토큰과 이메일 불러오기
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

        friendAdapter = FriendAdapter(friends) { friend ->
            showFriendInfoDialog(friend)
        }
        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = friendAdapter

        binding.addFriendBtn.setOnClickListener{
            val dialogView = layoutInflater.inflate(R.layout.add_friend_dialog, null)
            val builder = AlertDialog.Builder(this)
                .setView(dialogView)

            val addFriendDialog = builder.create()
            addFriendDialog.show()

            val width = (resources.displayMetrics.widthPixels * 0.8).toInt()
            val height = (resources.displayMetrics.heightPixels * 0.6).toInt()
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

        // 소켓 초기화
        try {
            mSocket = IO.socket("http://10.0.2.2:80") // 서버 URL로 교체
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }

        mSocket.connect()
        mSocket.on(Socket.EVENT_CONNECT) {
            runOnUiThread {
                Log.d(TAG, "Socket connected")
                mSocket.emit("authenticate", token)
            }
        }

        mSocket.on("friend_list") { args ->
            runOnUiThread {
                if (args.isNotEmpty() && args[0] is JSONObject) {
                    val data = args[0] as JSONObject
                    val friendsJson = data.getJSONArray("friends").toString()

                    // Logcat을 위한 로그 메시지
                    Log.d(TAG, "Received friend list: $friendsJson")

                    loadFriendsFromJson(friendsJson)
                } else {
                    Log.d(TAG, "Received empty friend list or invalid data")
                }
            }
        }

        mSocket.on("friend_list_updated") { args ->
            runOnUiThread {
                if (args.isNotEmpty() && args[0] is JSONObject) {
                    val data = args[0] as JSONObject
                    val friendsJson = data.getJSONArray("friends").toString()

                    // Logcat을 위한 로그 메시지
                    Log.d(TAG, "Friend list updated: $friendsJson")

                    loadFriendsFromJson(friendsJson)
                } else {
                    Log.d(TAG, "Received empty friend list or invalid data")
                }
            }
        }

        fetchFriendList()

        // 로컬 데이터베이스에서 친구 목록 로드
        loadFriendsFromLocalDb()
    }

    private fun fetchFriendList() {
        mSocket.emit("fetch_friend_list")
        Log.d(TAG, "fetch_friend_list emitted")
    }

    override fun onDestroy() {
        super.onDestroy()
        mSocket.disconnect()
        Log.d(TAG, "Socket disconnected")
    }

    private fun loadFriendsFromJson(friendsJson: String) {
        val gson = Gson()
        val friendsType = object : TypeToken<List<Friend>>() {}.type
        val friends: List<Friend> = gson.fromJson(friendsJson, friendsType)

        // Logcat을 위한 로그 메시지
        Log.d(TAG, "Parsed friend list: ${friends.size} friends")

        friendAdapter.updateFriend(friends)

        // 로컬 데이터베이스에 친구 목록 저장
        saveFriendsToLocalDb(friends)
    }

    private fun saveFriendsToLocalDb(friends: List<Friend>) {
        dbHelper.addFriends(friends)
    }

    private fun loadFriendsFromLocalDb() {
        val localFriends = dbHelper.getAllFriends()
        friendAdapter.updateFriend(localFriends)
    }

    private fun showFriendInfoDialog(friend: Friend) {
        val dialogView = layoutInflater.inflate(R.layout.friend_info, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)

        val friendInfoDialog = builder.create()
        friendInfoDialog.show()

        val width = (resources.displayMetrics.widthPixels * 1).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.45).toInt()
        friendInfoDialog.window?.setLayout(width, height)

        dialogView.findViewById<TextView>(R.id.friend_name).text = "Name: ${friend.name}"
        dialogView.findViewById<TextView>(R.id.friend_email).text = "Email: ${friend.email}"

        val chatBtn = dialogView.findViewById<Button>(R.id.chat_btn)
        chatBtn.setOnClickListener {
            friendInfoDialog.dismiss()
        }

        val closeBtn = dialogView.findViewById<Button>(R.id.close_btn)
        closeBtn.setOnClickListener {
            friendInfoDialog.dismiss()
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
        val height = (resources.displayMetrics.heightPixels * 0.6).toInt()
        searchResultDialog.window?.setLayout(width, height)

        dialogView.findViewById<TextView>(R.id.search_result_name).text = "Name: $name"
        dialogView.findViewById<TextView>(R.id.search_result_email).text = "Email: $email"

        val addFriendBtn = dialogView.findViewById<Button>(R.id.add_friend_btn)
        addFriendBtn.setOnClickListener {
            addFriend(userEmail, email) {
                // 소켓 통신용 Friend 객체로 변환하여 추가
                val friend = Friend(null, name, email, null)
                friendAdapter.addFriend(friend)
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
}
