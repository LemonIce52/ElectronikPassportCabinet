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
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditUserActivity : AppCompatActivity() {

    private var selectedDateInMs: Long = MaterialDatePicker.todayInUtcMilliseconds()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activiti_edit_user)

        val userId = intent.getLongExtra("USER_ID", -1)
        var currUserEntity: UsersEntity? = null
        if (userId == -1L) finish()

        val db: AppDatabase = AppDatabase.getDatabase(this)

        lifecycleScope.launch {
            db.usersDao().getUserOnIdFlow(userId).collect { latestUser ->
                currUserEntity = latestUser
            }
        }

        val name: EditText = findViewById(R.id.name_equipment_input)
        val lastName: EditText = findViewById(R.id.lastNameInput)
        val email: EditText = findViewById(R.id.emailInput)
        val password: EditText = findViewById(R.id.passwordInput)
        val dateInput: EditText = findViewById(R.id.dateInput)

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

        setData(
            db,
            userId,
            name,
            lastName,
            email,
            password,
            dateInput
        ) {
            finish()
        }

        val closeButton: ImageView = findViewById(R.id.closeButtonEdit)
        val deleteButton: FrameLayout = findViewById(R.id.deleteUserButton)
        val savedChangesButton: FrameLayout = findViewById(R.id.savedChangesButton)

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
                ) {
                    finish()
                }
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
                ) {
                    finish()
                }
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
        dateInput: EditText,
        function: () -> Unit
    ) {

        val currUserEntity: UsersEntity? = getCreatedUserEntity(
            userId,
            dateInput,
            name,
            lastName,
            email,
            password
        )

        lifecycleScope.launch {
            if (userEntity != null && currUserEntity != null) {

                if (!validationUserEntity(currUserEntity, userEntity)) {

                    db.usersDao().updateUserData(currUserEntity)

                    Toast.makeText(
                        this@EditUserActivity, "Данные пользователя успешно изменены!",
                        Toast.LENGTH_LONG
                    ).show()

                    function()
                } else {
                    Toast.makeText(
                        this@EditUserActivity, "Данные пользователя не были изменены!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    this@EditUserActivity, "Произошла ошибка или пользователь был удален!",
                    Toast.LENGTH_LONG
                ).show()
                function()
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
        dateInput: EditText,
        callback: () -> Unit
    ) {
        val currUserEntity: UsersEntity? = getCreatedUserEntity(
            birthday = dateInput,
            name = name,
            lastName = lastName,
            email = email,
            password = password
        )

        lifecycleScope.launch {
            val userEntity: UsersEntity? = db.usersDao().getUserOnId(userId)

            if (userEntity != null && currUserEntity != null) {
                if (!validationUserEntity(currUserEntity, userEntity)) {
                    val dialog = CloseDialog()
                    dialog.show(supportFragmentManager, "CloseDialog")
                } else {
                    callback()
                }
            } else {
                callback()
            }
        }
    }

    private fun validationUserEntity(
        currUsersEntity: UsersEntity,
        userEntity: UsersEntity
    ): Boolean {
        return currUsersEntity.name == userEntity.name &&
                currUsersEntity.lastName == userEntity.lastName &&
                currUsersEntity.birthday.time == userEntity.birthday.time &&
                currUsersEntity.email == userEntity.email &&
                currUsersEntity.password == userEntity.password
    }

    private fun setData(
        db: AppDatabase,
        userId: Long,
        name: EditText,
        lastName: EditText,
        email: EditText,
        password: EditText,
        dateInput: EditText,
        callback: () -> Unit
    ) {


        lifecycleScope.launch {
            val userEntity: UsersEntity? = db.usersDao().getUserOnId(userId)

            if (userEntity != null) {
                name.setText(userEntity.name)
                lastName.setText(userEntity.lastName)
                email.setText(userEntity.email)
                password.setText(userEntity.password)

                val birthday: Date = userEntity.birthday
                val formater = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

                dateInput.setText(formater.format(birthday))

                selectedDateInMs = birthday.time
            } else {
                callback()
            }
        }
    }

    private fun getCreatedUserEntity(
        userId: Long = -1,
        birthday: EditText,
        name: EditText,
        lastName: EditText,
        email: EditText,
        password: EditText
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
                    password = password.text.toString(),
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
}