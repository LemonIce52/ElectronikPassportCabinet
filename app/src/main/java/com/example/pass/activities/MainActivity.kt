package com.example.pass.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pass.R
import com.example.pass.database.AppDatabase
import com.example.pass.database.users.Role
import com.example.pass.database.users.UsersEntity
import com.example.pass.otherClasses.Animates
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authorization)

        val appDatabase: AppDatabase = AppDatabase.getDatabase(this)

//        lifecycleScope.launch {
//
//            val calendar = Calendar.getInstance()
//            calendar.set(2023, Calendar.DECEMBER, 31, 0, 0, 0)
//            calendar.set(Calendar.MILLISECOND, 0)
//
//            val admin: UsersEntity = UsersEntity(
//                name = "vas",
//                lastName = "fag",
//                birthday = calendar.time,
//                email = "a@r",
//                password = "1234",
//                role = Role.ADMIN
//            )
//
//            appDatabase.usersDao().savedUser(admin)
//        }

        val emailInputEditText: EditText = findViewById(R.id.emailInput)
        val passwordInputEditText: EditText = findViewById(R.id.passwordInput)
        val loginButton: FrameLayout = findViewById(R.id.loginButton)
        val notFoundText: TextView = findViewById(R.id.notFoundText)

        loginButton.setOnClickListener {
            Animates().animatesButton(it) {
                login(
                    emailInputEditText,
                    passwordInputEditText,
                    appDatabase,
                    notFoundText
                )
            }
        }
    }

    private fun login(
        emailEditText: EditText,
        passwordEditText: EditText,
        database: AppDatabase,
        notFoundText: TextView
    ) {
        lifecycleScope.launch {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            val messageEmail: String = emailValidation(email)

            if (!messageEmail.isEmpty()) {
                emailEditText.error = messageEmail
                return@launch
            }

            val user: UsersEntity? = database.usersDao().getUser(email, password)

            if (user != null) {
                val nextActivity = when (user.role) {
                    Role.ADMIN -> AdminActivity::class.java
                    Role.TECH_SPECIALIST -> SpecialistActivity::class.java
                }

                val intent = Intent(this@MainActivity, nextActivity)
                intent.putExtra("currUserId", user.userId)

                startActivity(intent)
                finish()
            } else {
                notFoundText.visibility = View.VISIBLE
            }


        }
    }

    private fun emailValidation(email: String): String {
        if (email.isEmpty()) return "Почта не может быть пустой!"
        if (!email.contains('@')) return "Почта должна содержать @!"

        return ""
    }
}