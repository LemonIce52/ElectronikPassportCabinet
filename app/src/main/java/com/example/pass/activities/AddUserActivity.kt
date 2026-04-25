package com.example.pass.activities

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pass.R
import com.example.pass.database.AppDatabase
import com.example.pass.database.users.Role
import com.example.pass.database.users.UsersEntity
import com.example.pass.dialog.CloseDialog
import com.example.pass.otherClasses.Animates
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import org.mindrot.jbcrypt.BCrypt
import kotlin.collections.forEach

class AddUserActivity : AppCompatActivity() {

    private var selectedDateInMs: Long = MaterialDatePicker.todayInUtcMilliseconds()
    private val firstElementSpinner: String = "Выберите роль пользователя"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user)

        val currUserRole = Role.valueOf(intent.getStringExtra("currUserRole") ?: Role.ADMIN.name)

        val closeButton: ImageView = findViewById(R.id.closeButton)
        val registerButton: FrameLayout = findViewById(R.id.savedButton)
        val roleUserInput: LinearLayout = findViewById(R.id.userRoleInput)

        if (currUserRole == Role.MAIN_ADMIN) roleUserInput.visibility = View.VISIBLE
        else roleUserInput.visibility = View.GONE

        val nameInput: EditText = findViewById(R.id.name_equipment_input);
        val lastNameInput: EditText = findViewById(R.id.lastNameInput)
        val emailInput: EditText = findViewById(R.id.emailInput)
        val passwordInput: EditText = findViewById(R.id.passwordInput)
        val dateInput: EditText = findViewById(R.id.dateInput)
        var birthday: Date? = null
        val usersRoleSpinner: Spinner = findViewById(R.id.usersRoleSpinner)

        val listRole = Role.entries.toList()
        val usersRoleList = mutableListOf<Role>()

        listRole.forEach {
            if (it != Role.MAIN_ADMIN) usersRoleList.add(it)
        }

        createSpinnerDropDown(usersRoleList, usersRoleSpinner)

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
                    birthday,
                    emailInput,
                    passwordInput,
                    usersRoleSpinner,
                    currUserRole,
                    usersRoleList
                )
            }
        }

    }

    private fun createSpinnerDropDown(
        roleUsersList: List<Role>,
        roleUsersSpinner: Spinner,
    ) {
        val list = mutableListOf(firstElementSpinner)
        roleUsersList.forEach {
            list.add(it.nameRole)
        }

        val typeDocumentsAdapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            list
        ) {
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                return if (position == 0) {
                    View(context).apply {
                        layoutParams = LayoutParams(0, 0)
                        visibility = View.GONE
                    }
                } else {
                    super.getDropDownView(position, null, parent)
                }
            }
        }

        typeDocumentsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        roleUsersSpinner.adapter = typeDocumentsAdapter
    }

    private fun registerUser(
        nameInput: EditText,
        lastNameInput: EditText,
        birthday: Date?,
        emailInput: EditText,
        passwordInput: EditText,
        usersRoleSpinner: Spinner,
        currUserRole: Role,
        roleList: List<Role>
    ) {
        lifecycleScope.launch {

            val database: AppDatabase = AppDatabase.getDatabase(this@AddUserActivity)
            val name: String = nameInput.text.toString()
            val lastName: String = lastNameInput.text.toString()

            val email: String = emailInput.text.toString()
            val password: String = passwordInput.text.toString()

            if (validationUserInputs(
                    emailInput,
                    database,
                    nameInput,
                    lastNameInput,
                    birthday,
                    passwordInput
                )
            ) return@launch

            val userEntity = UsersEntity(
                name = name,
                lastName = lastName,
                birthday = birthday!!,
                email = email,
                password = hashPass(password),
                role = if (currUserRole == Role.MAIN_ADMIN) roleList[usersRoleSpinner.selectedItemPosition - 1] else Role.TECH_SPECIALIST
            )

            database.usersDao().savedUser(userEntity)

            finish()
        }
    }

    private fun hashPass(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    private suspend fun validationUserInputs(
        emailInput: EditText,
        database: AppDatabase,
        nameInput: EditText,
        lastNameInput: EditText,
        birthday: Date?,
        passwordInput: EditText
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

        if (database.usersDao().getUsersOnEmail(emailInput.text.toString()) > 0) {
            emailInput.error = "Пользователь с такой почтой уже имеется!"
            return true
        }

        if (passwordInput.text.isEmpty()) {
            passwordInput.error = "Пароль не может быть пустым!"
            return true
        }

        if (passwordInput.text.length < 8) {
            passwordInput.error = "Пароль должен состоять из 8 и более символов!"
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

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                // Если нажатие произошло вне области текущего EditText
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}