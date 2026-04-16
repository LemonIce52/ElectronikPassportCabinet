package com.example.pass.activities

import android.os.Bundle
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pass.R
import com.example.pass.database.AppDatabase
import com.example.pass.database.users.Role
import com.example.pass.database.users.UsersEntity
import com.example.pass.dialog.CloseDialog
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AddUserActivity : AppCompatActivity() {

    private var selectedDateInMs: Long = MaterialDatePicker.todayInUtcMilliseconds()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user)

        val closeButton: ImageView = findViewById(R.id.closeButton)
        val registerButton: FrameLayout = findViewById(R.id.savedButton)

        val nameInput: EditText = findViewById(R.id.name_equipment_input);
        val lastNameInput: EditText = findViewById(R.id.lastNameInput)
        val emailInput: EditText = findViewById(R.id.emailInput)
        val passwordInput: EditText = findViewById(R.id.passwordInput)
        val dateInput: EditText = findViewById(R.id.dateInput)
        var birthday: Date? = null

        dateInput.setOnClickListener {
            val builder = MaterialDatePicker.Builder.datePicker()
                .setTheme(R.style.MyDatePickerStyle)
                .setTitleText("Выберете дату рождения")
                .setSelection(selectedDateInMs)

            val datePicker = builder.build()

            datePicker.show(supportFragmentManager, "DATE_PICKER")

            datePicker.addOnPositiveButtonClickListener { selection ->
                selectedDateInMs = selection
                val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone("UTC")

                val dateString = formatter.format(Date(selection))

                dateInput.setText(dateString)

                birthday = Date(selection)
            }
        }

        closeButton.setOnClickListener {
            Animates().animatesButton(it) {
                closeActivity(
                    nameInput,
                    lastNameInput,
                    dateInput,
                    emailInput,
                    passwordInput
                )
            }
        }

        registerButton.setOnClickListener {
            Animates().animatesButton(it) {
                registerUser(
                    nameInput,
                    lastNameInput,
                    dateInput,
                    birthday,
                    emailInput,
                    passwordInput
                ) {
                    finish()
                }
            }
        }

    }

    private fun registerUser(
        nameInput: EditText,
        lastNameInput: EditText,
        dateInput: EditText,
        birthday: Date?,
        emailInput: EditText,
        passwordInput: EditText,
        callback: () -> Unit
    ) {
        lifecycleScope.launch {

            val database: AppDatabase = AppDatabase.getDatabase(this@AddUserActivity)
            val name: String = nameInput.text.toString()
            val lastName: String = lastNameInput.text.toString()

            val birthdayStr: String = dateInput.text.toString()
            val email: String = emailInput.text.toString()
            val password: String = passwordInput.text.toString()

            if (validationUserInputs(
                    email,
                    emailInput,
                    database,
                    name,
                    nameInput,
                    lastName,
                    lastNameInput,
                    birthdayStr,
                    birthday,
                    password,
                    passwordInput
                )
            ) return@launch

            val userEntity = UsersEntity(
                name = name,
                lastName = lastName,
                birthday = birthday!!,
                email = email,
                password = password,
                role = Role.TECH_SPECIALIST
            )

            database.usersDao().savedUser(userEntity)

            callback()
        }
    }

    private suspend fun validationUserInputs(
        email: String,
        emailInput: EditText,
        database: AppDatabase,
        name: String,
        nameInput: EditText,
        lastName: String,
        lastNameInput: EditText,
        date: String,
        birthday: Date?,
        password: String,
        passwordInput: EditText
    ): Boolean {
        val errorMessage: String = emailValidation(email)

        if (name.isEmpty()) {
            nameInput.error = "Имя не может быть пустым!"
            return true
        }

        if (lastName.isEmpty()) {
            lastNameInput.error = "Фамилия не может быть пустым!"
            return true
        }

        if (date.isEmpty()) {
            Toast.makeText(this, "Дата рождения не может быть пустой!", Toast.LENGTH_LONG).show()
            return true
        }

        if (birthday == null) {
            Toast.makeText(this, "Не верно ведена дата!", Toast.LENGTH_LONG).show()
            return true
        }

        if (!isAdult(birthday)) {
            Toast.makeText(this, "Пользователю меньше 18 лет!", Toast.LENGTH_LONG).show()
            return true
        }

        if (!errorMessage.isEmpty()) {
            emailInput.error = errorMessage
            return true
        }

        if (database.usersDao().getUsersOnEmail(email) > 0) {
            emailInput.error = "Пользователь с такой почтой уже имеется!"
            return true
        }

        if (password.isEmpty()) {
            passwordInput.error = "Пароль не может быть пустым!"
            return true
        }

        if (database.usersDao().getUsersOnPassword(password) > 0) {
            passwordInput.error = "Пароль занят придумайте другой!"
            return true
        }
        return false
    }

    private fun emailValidation(email: String): String {
        if (email.isEmpty()) return "Почта не может быть пустой!"
        if (!email.contains('@')) return "Почта должна сожержать @!"

        return ""
    }

    fun isAdult(birthdayDate: Date): Boolean {
        val birthday = Calendar.getInstance()
        birthday.time = birthdayDate

        val cutOffDate = Calendar.getInstance()
        cutOffDate.set(Calendar.MILLISECOND, 0)
        cutOffDate.add(Calendar.YEAR, -18)

        return birthday.before(cutOffDate)
    }

    private fun closeActivity(
        nameInput: EditText,
        lastNameInput: EditText,
        dateInput: EditText,
        emailInput: EditText,
        passwordInput: EditText,
    ) {
        if (nameInput.text.toString().isEmpty() &&
            lastNameInput.text.toString().isEmpty() &&
            emailInput.text.toString().isEmpty() &&
            passwordInput.text.toString().isEmpty() &&
            dateInput.text.toString().isEmpty()
        ) {
            finish()
        } else {
            val dialog = CloseDialog()
            dialog.show(supportFragmentManager, "CloseDialog")
        }
    }
}