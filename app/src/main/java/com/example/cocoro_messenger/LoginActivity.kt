// LoginActivity.kt
package com.example.cocoro_messenger

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import kotlinx.coroutines.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val homeBtn = findViewById<Button>(R.id.home_btn)
        val loginBtn = findViewById<Button>(R.id.login_btn)
        val signupBtn = findViewById<Button>(R.id.sign_up_btn)

        val email = findViewById<TextInputLayout>(R.id.email_field)
        val password = findViewById<TextInputLayout>(R.id.password_field)
        val loginSubmit = findViewById<Button>(R.id.login_submit)

        loginSubmit.setOnClickListener {
            val emailValue = email.editText?.text?.toString()
            val passwordValue = password.editText?.text?.toString()

            if (emailValue.isNullOrEmpty() || passwordValue.isNullOrEmpty()) {
                Toast.makeText(this, "이메일 및 비밀번호 입력은 필수 입니다.", Toast.LENGTH_SHORT).show()
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
                when (response.code()) {
                    200 -> {
                        val loginResponse = response.body()
                        if (loginResponse?.token != null) {
                            val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putString("token", loginResponse.token)
                                apply()
                            }
                            Toast.makeText(this@LoginActivity, "${loginResponse.name}님, 로그인에 성공했습니다.", Toast.LENGTH_SHORT).show()

                            val gson = Gson()
                            val friendsJson = gson.toJson(loginResponse.friends)

                            val intent = Intent(this@LoginActivity, ContactActivity::class.java).apply {
                                putExtra("token", loginResponse.token)
                                putExtra("userEmail", loginResponse.email)
                                putExtra("name", loginResponse.name)
                                putExtra("friendsJson", friendsJson)
                            }
                            startActivity(intent)
                            finish()
                        }
                    }
                    400 -> {
                        Toast.makeText(this@LoginActivity, "이메일과 비밀번호는 필수 입력 사항입니다.", Toast.LENGTH_SHORT).show()
                    }
                    401 -> {
                        Toast.makeText(this@LoginActivity, "이메일 또는 비밀번호가 잘못되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                    500 -> {
                        Toast.makeText(this@LoginActivity, "서버 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(this@LoginActivity, "알 수 없는 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this@LoginActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
