package com.example.cocoro_messenger

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
                                putString("jwt_token", loginResponse.token)
                                apply()
                            }
                            Toast.makeText(this@LoginActivity, "${loginResponse.name}様、ログインに成功しました。", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@LoginActivity, ContactActivity::class.java)
                            intent.putExtra("token", loginResponse.token)
                            startActivity(intent)
                            finish()
                        }
                    }
                    400 -> {
                        Toast.makeText(this@LoginActivity, "メールアドレスとパスワードの入力は必須です。", Toast.LENGTH_SHORT).show()
                    }
                    401 -> {
                        Toast.makeText(this@LoginActivity, "メールアドレスまたはパスワードが一致しません。", Toast.LENGTH_SHORT).show()
                    }
                    500 -> {
                        Toast.makeText(this@LoginActivity, "システムエラーです。", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(this@LoginActivity, "不明なエラーが発生しました。", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this@LoginActivity, "ネットワークエラーが発生しました。", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
