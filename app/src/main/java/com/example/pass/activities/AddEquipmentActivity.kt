package com.example.pass.activities

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.AdapterView
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
import com.example.pass.database.equipment.EquipmentEntity
import com.example.pass.database.equipment.EquipmentType
import com.example.pass.database.equipment.StateEquipment
import com.example.pass.dialog.CloseDialog
import com.example.pass.otherClasses.Animates
import kotlinx.coroutines.launch
import java.util.Date

class AddEquipmentActivity : AppCompatActivity() {

    private val firstType: String = "Выберете тип оборудования"
    private val firstGroup: String = "Выберете ростовую группу"
    private val firstState: String = "Выберете состояние оборудования"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_equipment)

        val database: AppDatabase = AppDatabase.getDatabase(this)

        val closeButton: ImageView = findViewById(R.id.closeButton)
        val addEquipmentButton: FrameLayout = findViewById(R.id.addEquipmentButton)

        val nameEquipmentInput: EditText = findViewById(R.id.name_equipment_input)
        val identificationNumberInput: EditText = findViewById(R.id.identification_input)
        val typeEquipmentSpinner: Spinner = findViewById(R.id.type_equipment)
        val groupEquipmentSpinner: Spinner = findViewById(R.id.group_equipment_spinner)
        val stateEquipmentSpinner: Spinner = findViewById(R.id.state_equipment)
        val groupFrame: LinearLayout = findViewById(R.id.group_equipment)

        val equipmentTypeList: List<EquipmentType> = EquipmentType.entries.toList()
        val groupList: List<Int> = (0..6).toList()
        val stateList: List<StateEquipment> = StateEquipment.entries.toList()

        createTypeSpinnerDropDown(equipmentTypeList, typeEquipmentSpinner, groupFrame)
        createGroupSpinnerDropDown(groupList, groupEquipmentSpinner)
        createStateSpinnerDropDown(stateList, stateEquipmentSpinner)

        closeButton.setOnClickListener {
            Animates().animatesButton(it) {
                closeActivity(
                    nameEquipmentInput,
                    identificationNumberInput,
                    typeEquipmentSpinner,
                    groupEquipmentSpinner,
                    stateEquipmentSpinner
                )
            }
        }
        addEquipmentButton.setOnClickListener {
            Animates().animatesButton(it) {
                addEquipment(
                    database,
                    nameEquipmentInput,
                    identificationNumberInput,
                    typeEquipmentSpinner,
                    groupEquipmentSpinner,
                    stateEquipmentSpinner,
                    equipmentTypeList,
                    groupList,
                    stateList
                )
            }
        }
    }

    private fun addEquipment(
        database: AppDatabase,
        nameEquipmentInput: EditText,
        identificationNumberInput: EditText,
        typeEquipmentSpinner: Spinner,
        groupEquipmentSpinner: Spinner,
        stateEquipmentSpinner: Spinner,
        equipmentTypeList: List<EquipmentType>,
        groupList: List<Int>,
        stateList: List<StateEquipment>
    ) {
        lifecycleScope.launch {

            if (validationDataInputs(
                    nameEquipmentInput,
                    identificationNumberInput,
                    database,
                    typeEquipmentSpinner,
                    stateEquipmentSpinner,
                    groupEquipmentSpinner
                )
            ) return@launch

            val equipment = EquipmentEntity(
                identificationNumber = identificationNumberInput.text.toString(),
                name = nameEquipmentInput.text.toString(),
                cabinetId = null,
                group = if (typeEquipmentSpinner.selectedItem.equals(EquipmentType.FURNITURE.nameDescription)) groupList[groupEquipmentSpinner.selectedItemPosition - 1] else null,
                equipmentType = equipmentTypeList[typeEquipmentSpinner.selectedItemPosition - 1],
                stateEquipment = stateList[stateEquipmentSpinner.selectedItemPosition - 1],
                lastDateCheck = Date()
            )

            database.equipmentDao().savedEquipment(equipment)
            finish()
        }
    }

    private suspend fun validationDataInputs(
        nameEquipmentInput: EditText,
        identificationNumberInput: EditText,
        database: AppDatabase,
        typeEquipmentSpinner: Spinner,
        stateEquipmentSpinner: Spinner,
        groupEquipmentSpinner: Spinner
    ): Boolean {
        if (nameEquipmentInput.text.isEmpty()) {
            nameEquipmentInput.error = "Название не может быть пустым!"
            return true
        }

        if (identificationNumberInput.text.isEmpty()) {
            identificationNumberInput.error = "Номер оборудования не может быть пустой!"
            return true
        }

        if (database.equipmentDao()
                .getCountOnNumber(identificationNumberInput.text.toString()) > 0
        ) {
            identificationNumberInput.error = "Номер оборудования должен быть уникальным!"
            return true
        }

        if (typeEquipmentSpinner.selectedItem.equals(firstType)) {
            Toast.makeText(
                this@AddEquipmentActivity, "Нужно выбрать тип оборудования!",
                Toast.LENGTH_LONG
            ).show()
            return true
        }

        if (stateEquipmentSpinner.selectedItem.equals(firstState)) {
            Toast.makeText(
                this@AddEquipmentActivity, "Нужно выбрать состояние оборудования!",
                Toast.LENGTH_LONG
            ).show()
            return true
        }

        if (typeEquipmentSpinner.selectedItem.equals(EquipmentType.FURNITURE.nameDescription) && groupEquipmentSpinner.selectedItem.equals(
                firstGroup
            )
        ) {
            Toast.makeText(
                this@AddEquipmentActivity, "Нужно выбрать остовую группу!",
                Toast.LENGTH_LONG
            ).show()
            return true
        }
        return false
    }

    private fun createTypeSpinnerDropDown(
        equipmentTypeList: List<EquipmentType>,
        typeEquipmentSpinner: Spinner,
        groupFrame: LinearLayout
    ) {
        val list = mutableListOf(firstType)
        equipmentTypeList.forEach { list.add(it.nameDescription) }

        val typeEquipmentAdapter = object : ArrayAdapter<String>(
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

        typeEquipmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        typeEquipmentSpinner.adapter = typeEquipmentAdapter


        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (typeEquipmentSpinner.selectedItem.equals(EquipmentType.ELECTRONICS.nameDescription) || typeEquipmentSpinner.selectedItem.equals(firstType)) {
                    groupFrame.visibility = View.GONE
                } else {
                    groupFrame.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        typeEquipmentSpinner.onItemSelectedListener = listener
    }

    private fun createGroupSpinnerDropDown(
        groupList: List<Int>,
        groupEquipmentSpinner: Spinner,
    ) {
        val list = mutableListOf(firstGroup)
        groupList.forEach { list.add(it.toString()) }

        val groupEquipmentAdapter = object : ArrayAdapter<String>(
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

        groupEquipmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        groupEquipmentSpinner.adapter = groupEquipmentAdapter
    }

    private fun createStateSpinnerDropDown(
        stateList: List<StateEquipment>,
        stateEquipmentSpinner: Spinner,
    ) {

        val list = mutableListOf(firstState)
        stateList.forEach { list.add(it.nameDescription) }

        val stateEquipmentAdapter = object : ArrayAdapter<String>(
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

        stateEquipmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        stateEquipmentSpinner.adapter = stateEquipmentAdapter
    }

    private fun closeActivity(
        nameEquipmentInput: EditText,
        identificationNumberInput: EditText,
        typeEquipmentSpinner: Spinner,
        groupEquipmentSpinner: Spinner,
        stateEquipmentSpinner: Spinner
    ) {
        if (!nameEquipmentInput.text.isEmpty() ||
            !identificationNumberInput.text.isEmpty() ||
            !typeEquipmentSpinner.selectedItem.equals(firstType) ||
            !groupEquipmentSpinner.selectedItem.equals(firstGroup) ||
            !stateEquipmentSpinner.selectedItem.equals(firstState)
        ) {
            val dialog = CloseDialog()
            dialog.show(supportFragmentManager, "CloseDialog")
        } else {
            finish()
        }
    }

}