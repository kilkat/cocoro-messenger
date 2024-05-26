package com.example.cocoro_messenger

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputLayout

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)

        val homeBtn = findViewById<Button>(R.id.home_btn)
        val loginBtn = findViewById<Button>(R.id.login_btn)
        val signupBtn = findViewById<Button>(R.id.sign_up_btn)

        val email = findViewById<TextInputLayout>(R.id.email_field)
        val phoneField = findViewById<TextInputLayout>(R.id.phone_field)
        val password = findViewById<TextInputLayout>(R.id.password_field)
        val confirmPassword = findViewById<TextInputLayout>(R.id.confirm_password_field)
        val loginSubmit = findViewById<Button>(R.id.sign_up_submit)

        loginSubmit.setOnClickListener {
            val emailValue = email.editText?.text.toString()
            val phoneValue = phoneField.editText?.text.toString()
            val passwordValue = password.editText?.text.toString()
            val confirmPasswordValue = confirmPassword.editText?.text.toString()

            if (emailValue.isEmpty() || phoneValue.isEmpty() || passwordValue.isEmpty() || confirmPasswordValue.isEmpty()) {
                Toast.makeText(this, "すべてのアイテムは必須入力項目です。", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else if (passwordValue != confirmPasswordValue) {
                Toast.makeText(this, "パスワードが一致しません。", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else {
                Toast.makeText(this, "cocoro 会員登録を完了しました。", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
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
}