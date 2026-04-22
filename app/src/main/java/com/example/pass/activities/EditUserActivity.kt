package com.example.pass.activities

import android.os.Bundle
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import com.example.pass.R
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pass.database.AppDatabase
import com.example.pass.database.users.Role
import com.example.pass.database.users.UsersEntity
import com.example.pass.dialog.CloseDialog
import com.example.pass.dialog.DeleteUserDialog
import com.example.pass.otherClasses.Animates
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditUserActivity : AppCompatActivity() {

    private var selectedDateInMs: Long = MaterialDatePicker.todayInUtcMilliseconds()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user)

        val db: AppDatabase = AppDatabase.getDatabase(this)
        val userId = intent.getLongExtra("USER_ID", -1)
        var currUserEntity: UsersEntity? = null

        val name: EditText = findViewById(R.id.name_equipment_input)
        val lastName: EditText = findViewById(R.id.lastNameInput)
        val email: EditText = findViewById(R.id.emailInput)
        val password: EditText = findViewById(R.id.passwordInput)
        val dateInput: EditText = findViewById(R.id.dateInput)

        val closeButton: ImageView = findViewById(R.id.closeButtonEdit)
        val deleteButton: FrameLayout = findViewById(R.id.deleteUserButton)
        val savedChangesButton: FrameLayout = findViewById(R.id.savedChangesButton)

        if (userId == -1L) {
            Toast.makeText(this, "Произошла ошибка или пользователь был удален!", Toast.LENGTH_LONG).show()
            finish()
        }


        lifecycleScope.launch {
            db.usersDao().getUserOnIdFlow(userId).collect { latestUser ->
                currUserEntity = latestUser
            }
        }

        setData(
            db,
            userId,
            name,
            lastName,
            email,
            dateInput
        )

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
                val dateString = formatter.format(Date(selection))

                dateInput.setText(dateString)
            }
        }

        closeButton.setOnClickListener {
            Animates().animatesButton(it) {
                closeButton(
                    db,
                    userId,
                    name,
                    lastName,
                    email,
                    password,
                    dateInput
                )
            }
        }

        deleteButton.setOnClickListener {
            Animates().animatesButton(it) { deleteButton(userId) }
        }

        savedChangesButton.setOnClickListener {
            Animates().animatesButton(it) {
                savedChangesButton(
                    currUserEntity,
                    db,
                    userId,
                    name,
                    lastName,
                    email,
                    password,
                    dateInput
                )
            }
        }
    }

    private fun savedChangesButton(
        userEntity: UsersEntity?,
        db: AppDatabase,
        userId: Long,
        name: EditText,
        lastName: EditText,
        email: EditText,
        password: EditText,
        dateInput: EditText
    ) {
        lifecycleScope.launch {

            val formater = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val date: Date? = formater.parse(dateInput.text.toString())

            if (validationUserInputs(userId, email, db, name, lastName, date)) {
                return@launch
            }

            if (userEntity != null) {

                val currUserEntity: UsersEntity? = getCreatedUserEntity(
                    userId,
                    dateInput,
                    name,
                    lastName,
                    email,
                    if (password.text.isEmpty()) userEntity.password else hashPass(password.text.toString())
                )

                if (currUserEntity != null && currUserEntity != userEntity) {

                    db.usersDao().updateUserData(currUserEntity)

                    Toast.makeText(
                        this@EditUserActivity, "Данные пользователя успешно изменены!",
                        Toast.LENGTH_LONG
                    ).show()

                    finish()
                } else {
                    Toast.makeText(
                        this@EditUserActivity, "Данные пользователя не были изменены или возникла проблема!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    this@EditUserActivity, "Произошла ошибка или пользователь был удален!",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }


    private fun deleteButton(
        userId: Long,
    ) {
        val dialog = DeleteUserDialog.newInstance(userId)
        dialog.show(supportFragmentManager, "CloseDialog")
    }

    private fun closeButton(
        db: AppDatabase,
        userId: Long,
        name: EditText,
        lastName: EditText,
        email: EditText,
        password: EditText,
        dateInput: EditText
    ) {
        lifecycleScope.launch {
            val userEntity: UsersEntity? = db.usersDao().getUserOnId(userId)
            if (userEntity != null) {

                val currUserEntity: UsersEntity? = getCreatedUserEntity(
                    userId = userId,
                    birthday = dateInput,
                    name = name,
                    lastName = lastName,
                    email = email,
                    password = if (password.text.isEmpty()) userEntity.password else hashPass(password.text.toString())
                )

                if (userEntity == currUserEntity) {
                    val dialog = CloseDialog()
                    dialog.show(supportFragmentManager, "CloseDialog")
                } else {
                    finish()
                }
            } else {
                finish()
            }
        }
    }

    private suspend fun validationUserInputs(
        userId: Long,
        emailInput: EditText,
        database: AppDatabase,
        nameInput: EditText,
        lastNameInput: EditText,
        birthday: Date?,
    ): Boolean {
        val errorMessage: String = emailValidation(emailInput.text.toString())

        if (nameInput.text.isEmpty()) {
            nameInput.error = "Имя не может быть пустым!"
            return true
        }

        if (lastNameInput.text.isEmpty()) {
            lastNameInput.error = "Фамилия не может быть пустым!"
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

        if (database.usersDao().getUsersOnEmailEdit(emailInput.text.toString(), userId) > 0) {
            emailInput.error = "Пользователь с такой почтой уже имеется!"
            return true
        }

        return false
    }

    private fun emailValidation(email: String): String {
        if (email.isEmpty()) return "Почта не может быть пустой!"
        if (!email.contains('@')) return "Почта должна содержать @!"

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

    private fun setData(
        db: AppDatabase,
        userId: Long,
        name: EditText,
        lastName: EditText,
        email: EditText,
        dateInput: EditText
    ) {


        lifecycleScope.launch {
            val userEntity: UsersEntity? = db.usersDao().getUserOnId(userId)

            if (userEntity != null) {
                name.setText(userEntity.name)
                lastName.setText(userEntity.lastName)
                email.setText(userEntity.email)

                val birthday: Date = userEntity.birthday
                val formater = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

                dateInput.setText(formater.format(birthday))

                selectedDateInMs = birthday.time
            } else {
                finish()
            }
        }
    }

    private fun getCreatedUserEntity(
        userId: Long,
        birthday: EditText,
        name: EditText,
        lastName: EditText,
        email: EditText,
        password: String
    ): UsersEntity? {
        try {
            val formater = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val date: Date? = formater.parse(birthday.text.toString())

            if (date != null) {
                val calendar = Calendar.getInstance()
                calendar.clear()
                calendar.time = date

                val currUsersEntity = UsersEntity(
                    userId = userId,
                    name = name.text.toString(),
                    lastName = lastName.text.toString(),
                    birthday = calendar.time,
                    email = email.text.toString(),
                    password = password,
                    role = Role.TECH_SPECIALIST
                )

                return currUsersEntity
            }

            return null
        } catch (_: Exception) {
            Toast.makeText(this, "Произошла ошибка!", Toast.LENGTH_LONG).show()
            return null
        }
    }

    private fun hashPass(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }
}