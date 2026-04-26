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
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pass.R
import com.example.pass.database.AppDatabase
import com.example.pass.database.cabinets.CabinetEntity
import com.example.pass.database.cabinets.TypeCabinet
import com.example.pass.dialog.CloseDialog
import com.example.pass.otherClasses.Animates
import kotlinx.coroutines.launch

class AddCabinetActivity : AppCompatActivity() {

    private val firstElementSpinner: String = "Выберете тип кабинета"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_cabinet)

        val database: AppDatabase = AppDatabase.getDatabase(this)
        val listTypes: List<TypeCabinet> = TypeCabinet.entries.toList()

        val closeButton: ImageView = findViewById(R.id.closeButton)
        val addCabinetButton: FrameLayout = findViewById(R.id.addButton)
        val nameInput: EditText = findViewById(R.id.nameCabinetInput)
        val typeInputs: Spinner = findViewById(R.id.typeCabinet)
        val widthInput: EditText = findViewById(R.id.widthInput)
        val heightInput: EditText = findViewById(R.id.heightInput)
        val lengthInput: EditText = findViewById(R.id.lengthInput)

        createDropDownTypes(typeInputs, listTypes)

        closeButton.setOnClickListener {
            Animates().animatesButton(it) {
                closeActivity(
                    nameInput,
                    typeInputs,
                    widthInput,
                    heightInput,
                    lengthInput
                )
            }
        }

        addCabinetButton.setOnClickListener {
            Animates().animatesButton(it) {
                addCabinet(
                    database,
                    nameInput,
                    typeInputs,
                    widthInput,
                    heightInput,
                    lengthInput,
                    listTypes
                )
            }
        }

    }

    private fun addCabinet(
        database: AppDatabase,
        nameInput: EditText,
        typeInputs: Spinner,
        widthInput: EditText,
        heightInput: EditText,
        lengthInput: EditText,
        listType: List<TypeCabinet>
    ) {

        lifecycleScope.launch {
            if (validationDataInputs(
                    nameInput,
                    database,
                    typeInputs,
                    widthInput,
                    heightInput,
                    lengthInput
                )
            ) return@launch

            val cabinet = CabinetEntity(
                name = nameInput.text.toString(),
                typeCabinet = listType[typeInputs.selectedItemPosition - 1],
                width = widthInput.text.toString().toInt(),
                height = heightInput.text.toString().toInt(),
                length = lengthInput.text.toString().toInt()
            )

            database.cabinetDao().saveCabinet(cabinet)
            finish()
        }

    }

    private suspend fun validationDataInputs(
        nameInput: EditText,
        database: AppDatabase,
        typeInputs: Spinner,
        widthInput: EditText,
        heightInput: EditText,
        lengthInput: EditText
    ): Boolean {
        if (nameInput.text.isEmpty()) {
            nameInput.error = "Название не может быть пустым"
            return true
        }

        if (database.cabinetDao().getCountCabinetForNameCreate(nameInput.text.toString()) > 0) {
            nameInput.error = "Имя кабинета должно быть уникальным!"
            return true
        }

        if (typeInputs.selectedItem.equals(firstElementSpinner)) {
            Toast.makeText(
                this@AddCabinetActivity,
                "Требуется выбрать тип кабинета",
                Toast.LENGTH_LONG
            ).show()
            return true
        }

        if (widthInput.text.isEmpty() || widthInput.text.toString()
                .toIntOrNull() == null || widthInput.text.toString().toInt() <= 0
        ) {
            widthInput.error = "Ширина не может быть пустой, меньшей или равной 0!"
            return true
        }

        if (heightInput.text.isEmpty() || heightInput.text.toString()
                .toIntOrNull() == null || heightInput.text.toString().toInt() <= 0
        ) {
            heightInput.error = "Высота не может быть пустой, меньшей или равной 0!"
            return true
        }

        if (lengthInput.text.isEmpty() || lengthInput.text.toString()
                .toIntOrNull() == null || lengthInput.text.toString().toInt() <= 0
        ) {
            lengthInput.error = "Длинна не может быть пустой, меньшей или равной 0!"
            return true
        }
        return false
    }

    private fun closeActivity(
        nameInput: EditText,
        typeInputs: Spinner,
        widthInput: EditText,
        heightInput: EditText,
        lengthInput: EditText
    ) {

        if (!nameInput.text.isEmpty() ||
            !widthInput.text.isEmpty() ||
            !heightInput.text.isEmpty() ||
            !lengthInput.text.isEmpty() ||
            !typeInputs.selectedItem.equals(firstElementSpinner)
            ) {
            val dialog = CloseDialog()
            dialog.show(supportFragmentManager, "CloseDialog")
        } else {
            finish()
        }
    }

    private fun createDropDownTypes(spinner: Spinner, listType: List<TypeCabinet>) {
        val list = mutableListOf(firstElementSpinner)

        listType.forEach {
            list.add(it.nameDescription)
        }

        val adapter = object : ArrayAdapter<String>(
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

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter
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