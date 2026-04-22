package com.example.pass.activities

import android.os.Bundle
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
import com.example.pass.dialog.DeleteCabinetDialog
import com.example.pass.otherClasses.Animates
import kotlinx.coroutines.launch

class EditCabinetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_cabinet)

        val cabinetId: Long = intent.getLongExtra("cabinetId", -1)

        if (cabinetId == -1L) {
            Toast.makeText(this, "Произошла ошибка!", Toast.LENGTH_LONG).show()
            finish()
        }

        val database: AppDatabase = AppDatabase.getDatabase(this)

        val nameInput: EditText = findViewById(R.id.nameCabinetInput)
        val typeInputs: Spinner = findViewById(R.id.typeCabinet)
        val widthInput: EditText = findViewById(R.id.widthInput)
        val heightInput: EditText = findViewById(R.id.heightInput)
        val lengthInput: EditText = findViewById(R.id.lengthInput)

        val listTypesCabinet: List<TypeCabinet> = TypeCabinet.entries.toList()

        createDropDownTypes(typeInputs, listTypesCabinet)

        setData(
            database,
            cabinetId,
            nameInput,
            typeInputs,
            widthInput,
            heightInput,
            lengthInput
        )


        val closeButton: ImageView = findViewById(R.id.closeButton)
        val updateCabinetButton: FrameLayout = findViewById(R.id.savedChangesButton)
        val deleteCabinetButton: FrameLayout = findViewById(R.id.deleteCabinetButton)

        closeButton.setOnClickListener {
            Animates().animatesButton(it) {
                closeActivity(
                    database,
                    cabinetId,
                    nameInput,
                    listTypesCabinet,
                    typeInputs,
                    widthInput,
                    heightInput,
                    lengthInput
                )
            }
        }

        updateCabinetButton.setOnClickListener {
            Animates().animatesButton(it) {
                updateCabinet(
                    database,
                    cabinetId,
                    nameInput,
                    listTypesCabinet,
                    typeInputs,
                    widthInput,
                    heightInput,
                    lengthInput
                )
            }
        }

        deleteCabinetButton.setOnClickListener {
            Animates().animatesButton(it) {
                deleteCabinet(cabinetId)
            }
        }
    }

    private fun deleteCabinet(cabinetId: Long) {
        val dialog = DeleteCabinetDialog.newInstance(cabinetId)
        dialog.show(supportFragmentManager, "CloseDialog")
    }

    private fun updateCabinet(
        database: AppDatabase,
        cabinetId: Long,
        nameInput: EditText,
        listTypesCabinet: List<TypeCabinet>,
        typeInputs: Spinner,
        widthInput: EditText,
        heightInput: EditText,
        lengthInput: EditText
    ) {
        lifecycleScope.launch {
            if (nameInput.text.isEmpty()) {
                nameInput.error = "Название не может быть пустым"
                return@launch
            }

            if (database.cabinetDao().getCountCabinetForNameEdit(nameInput.text.toString(), cabinetId) > 0) {
                nameInput.error = "Имя кабинета должно быть уникальным!"
                return@launch
            }

            if (widthInput.text.isEmpty() || widthInput.text.toString().toIntOrNull() == null || widthInput.text.toString().toInt() <= 0) {
                widthInput.error = "Ширина не может быть пустой, меньшей или равной 0!"
                return@launch
            }

            if (heightInput.text.isEmpty() || heightInput.text.toString().toIntOrNull() == null || heightInput.text.toString().toInt() <= 0) {
                heightInput.error = "Высота не может быть пустой, меньшей или равной 0!"
                return@launch
            }

            if (lengthInput.text.isEmpty() || lengthInput.text.toString().toIntOrNull() == null || lengthInput.text.toString().toInt() <= 0) {
                lengthInput.error = "Длинна не может быть пустой, меньшей или равной 0!"
                return@launch
            }

            val cabinet: CabinetEntity? = database.cabinetDao().getCabinetById(cabinetId)

            if (cabinet != null) {
                val currEntityCabinet = getCabinetEntity(
                    cabinetId,
                    nameInput.text.toString(),
                    listTypesCabinet[typeInputs.selectedItemPosition],
                    widthInput.text.toString().toInt(),
                    heightInput.text.toString().toInt(),
                    lengthInput.text.toString().toInt()
                )

                if (cabinet != currEntityCabinet) {
                    database.cabinetDao().updateCabinet(currEntityCabinet)

                    Toast.makeText(
                        this@EditCabinetActivity, "Данные кабинета успешно изменены!",
                        Toast.LENGTH_LONG
                    ).show()

                    finish()
                } else {
                    Toast.makeText(this@EditCabinetActivity, "Данные не был изменены!",
                        Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this@EditCabinetActivity, "Произошла ошибка, возможно кабинет удален!",
                    Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun createDropDownTypes(spinner: Spinner, listType: List<TypeCabinet>) {

        val adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            listType.map { it.nameDescription }
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter
    }

    private fun setData(
        database: AppDatabase,
        cabinetId: Long,
        nameInput: EditText,
        typeInputs: Spinner,
        widthInput: EditText,
        heightInput: EditText,
        lengthInput: EditText
    ) {
        lifecycleScope.launch {
            val cabinet: CabinetEntity? = database.cabinetDao().getCabinetById(cabinetId)

            if (cabinet != null) {
                nameInput.setText(cabinet.name)
                widthInput.setText(cabinet.width.toString())
                heightInput.setText(cabinet.height.toString())
                lengthInput.setText(cabinet.length.toString())

                val adapter = typeInputs.adapter as ArrayAdapter<String>
                val position = adapter.getPosition(cabinet.typeCabinet.nameDescription)
                typeInputs.setSelection(position)

            } else {
                Toast.makeText(this@EditCabinetActivity, "Произошла ошибка, возможно кабинет удален!",
                    Toast.LENGTH_LONG).show()
                finish()
            }

        }
    }

    private fun closeActivity(
        database: AppDatabase,
        cabinetId: Long,
        nameInput: EditText,
        listTypes: List<TypeCabinet>,
        typeInputs: Spinner,
        widthInput: EditText,
        heightInput: EditText,
        lengthInput: EditText
    ) {
        lifecycleScope.launch {
            val cabinet: CabinetEntity? = database.cabinetDao().getCabinetById(cabinetId)

            if (cabinet != null) {
                val currEntityCabinet = getCabinetEntity(
                    cabinetId,
                    nameInput.text.toString(),
                    listTypes[typeInputs.selectedItemPosition],
                    widthInput.text.toString().toInt(),
                    heightInput.text.toString().toInt(),
                    lengthInput.text.toString().toInt()
                )

                if (cabinet != currEntityCabinet) {
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

    private fun getCabinetEntity(
        id: Long,
        name: String,
        type: TypeCabinet,
        width: Int,
        height: Int,
        length: Int
    ): CabinetEntity {
        return CabinetEntity (
            id,
            name,
            type,
            height,
            width,
            length
        )
    }

}