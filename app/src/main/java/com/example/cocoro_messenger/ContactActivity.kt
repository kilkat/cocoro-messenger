package com.example.cocoro_messenger

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cocoro_messenger.databinding.ActivityContactBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.socket.client.IO
import io.socket.client.Socket
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

        // 소켓 초기화
        try {
            mSocket = IO.socket("http://172.30.1.79:80") // 서버 URL로 교체
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

                    loadFriend(friendsJson)
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

                    loadFriend(friendsJson)
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

    private fun loadFriend(friendsJson: String) {
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
}
