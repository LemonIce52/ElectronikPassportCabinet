package com.example.pass.activities

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
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
import org.mindrot.jbcrypt.BCrypt
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authorization)

        val appDatabase: AppDatabase = AppDatabase.getDatabase(this)

        createFirstUser(appDatabase)

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

            val user: UsersEntity? = database.usersDao().getUser(email)

            if (user != null && checkPass(password, user.password)) {
                val nextActivity = when (user.role) {
                    Role.ADMIN, Role.MAIN_ADMIN -> AdminActivity::class.java
                    Role.TECH_SPECIALIST -> SpecialistActivity::class.java
                }

                val intent = Intent(this@MainActivity, nextActivity)
                intent.putExtra("currUserId", user.userId)
                intent.putExtra("currUserRole", user.role.name)

                startActivity(intent)
                finish()
            } else {
                notFoundText.visibility = View.VISIBLE
            }


        }
    }

    private fun checkPass(enterPassword: String, hashedPassword: String): Boolean {
        return BCrypt.checkpw(enterPassword, hashedPassword)
    }

    private fun emailValidation(email: String): String {
        if (email.isEmpty()) return "Почта не может быть пустой!"
        if (!email.contains('@')) return "Почта должна содержать @!"

        return ""
    }

    private fun createFirstUser(database: AppDatabase) {
        lifecycleScope.launch {
            if (database.usersDao().getFirstUser() >= 1) return@launch

            val calendar = Calendar.getInstance()
            calendar.set(2023, Calendar.DECEMBER, 31, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val admin = UsersEntity(
                name = "admin",
                lastName = "admin",
                birthday = calendar.time,
                email = "admin@admin",
                password = BCrypt.hashpw("ADMIN2435", BCrypt.gensalt()),
                role = Role.MAIN_ADMIN
            )

            database.usersDao().savedUser(admin)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}