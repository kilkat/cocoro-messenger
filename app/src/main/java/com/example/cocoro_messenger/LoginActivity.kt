package com.example.cocoro_messenger

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cocoro_messenger.models.*
import com.example.cocoro_messenger.network.*
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val homeBtn = findViewById<Button>(R.id.home_btn)
        val loginBtn = findViewById<Button>(R.id.login_btn)
        val signupBtn = findViewById<Button>(R.id.sign_up_btn)

        val email = findViewById<TextInputLayout>(R.id.email_field)
        val password = findViewById<TextInputLayout>(R.id.password_field)
        val loginSubmit = findViewById<Button>(R.id.login_submit)

        loginSubmit.setOnClickListener {
            val emailValue = email.editText?.text.toString()
            val passwordValue = password.editText?.text.toString()

            if (emailValue.isEmpty() || passwordValue.isEmpty()) {
                return@setOnClickListener
            } else {
                loginAccount(emailValue, passwordValue)
            }
        }

        homeBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        loginBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        signupBtn.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loginAccount(email: String, password: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val userLogin = UserLogin(email, password)
            val response = withContext(Dispatchers.IO) {
                try {
                    RetrofitClient.instance.loginUser(userLogin)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            if (response != null) {
                when (response.code()){
                    201 -> {
                        Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                        // 로그인 성공 로직 구현
                        finish()
                    }
                    // 로그인 실패 로직 구현
                }
            } else {
                Toast.makeText(this@LoginActivity, "ネットワークエラーが発生しました。", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
