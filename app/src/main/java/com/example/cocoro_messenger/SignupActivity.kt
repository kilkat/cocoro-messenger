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
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)

        val homeBtn = findViewById<Button>(R.id.home_btn)
        val loginBtn = findViewById<Button>(R.id.login_btn)
        val signupBtn = findViewById<Button>(R.id.sign_up_btn)

        val email = findViewById<TextInputLayout>(R.id.email_field)
        val name = findViewById<TextInputLayout>(R.id.name_field)
        val phoneField = findViewById<TextInputLayout>(R.id.phone_field)
        val password = findViewById<TextInputLayout>(R.id.password_field)
        val confirmPassword = findViewById<TextInputLayout>(R.id.confirm_password_field)
        val loginSubmit = findViewById<Button>(R.id.sign_up_submit)

        loginSubmit.setOnClickListener {
            val emailValue = email.editText?.text.toString()
            val nameValue = name.editText?.text.toString()
            val phoneValue = phoneField.editText?.text.toString()
            val passwordValue = password.editText?.text.toString()
            val confirmPasswordValue = confirmPassword.editText?.text.toString()

            if (emailValue.isEmpty() || nameValue.isEmpty() || phoneValue.isEmpty() || passwordValue.isEmpty() || confirmPasswordValue.isEmpty()) {
                Toast.makeText(this, "すべてのアイテムは必須入力項目です。", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (passwordValue != confirmPasswordValue) {
                Toast.makeText(this, "パスワードが一致しません。", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                createAccount(emailValue, nameValue, phoneValue, passwordValue, confirmPasswordValue)
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

    private fun createAccount(email: String, name: String, phone: String, password: String, confirmPassword: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val user = User(email, name, phone, password, confirmPassword)
            val response = withContext(Dispatchers.IO) {
                try {
                    RetrofitClient.instance.createUser(user)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            if (response != null) {
                when (response.code()) {
                    201 -> {
                        Toast.makeText(this@SignupActivity, "cocoro 会員登録を完了しました。", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@SignupActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    400 -> {
                        Toast.makeText(this@SignupActivity, "入力項目に不備があります。", Toast.LENGTH_SHORT).show()
                    }
                    409 -> {
                        val errorBody = response.errorBody()?.string()
                        println("Error response: $errorBody")

                        when {
                            errorBody?.contains("Email already exists.") == true -> {
                                Toast.makeText(this@SignupActivity, "このメールアドレスは既に登録されています。", Toast.LENGTH_SHORT).show()
                            }
                            errorBody?.contains("Name already exists.") == true -> {
                                Toast.makeText(this@SignupActivity, "この名前は既に登録されています。", Toast.LENGTH_SHORT).show()
                            }
                            errorBody?.contains("Phone number already exists.") == true -> {
                                Toast.makeText(this@SignupActivity, "この電話番号は既に登録されています。", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(this@SignupActivity, "登録に失敗しました。再試行してください。", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    else -> {
                        val errorBody = response.errorBody()?.string()
                        println("Error response: $errorBody")
                        Toast.makeText(this@SignupActivity, "登録に失敗しました。再試行してください。", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this@SignupActivity, "ネットワークエラーが発生しました。", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

interface ApiService {
    @POST("/userCreate")
    suspend fun createUser(@Body user: User): Response<ApiResponse>
}

data class User(
    val email: String,
    val name: String,
    val phone: String,
    val password: String,
    val confirmPassword: String
)

data class ApiResponse(
    val message: String
)

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:80"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}
