package com.example.pass.activities

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.provider.MediaStore
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
import com.example.pass.database.equipment.EquipmentEntity
import com.example.pass.database.equipment.EquipmentType
import com.example.pass.database.equipment.StateEquipment
import com.example.pass.dialog.CloseDialog
import com.example.pass.dialog.DeleteEquipmentDialog
import com.example.pass.otherClasses.Animates
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.launch
import java.util.Date

class EditEquipmentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_equipment)

        val database: AppDatabase = AppDatabase.getDatabase(this)
        val equipmentId: Long = intent.getLongExtra("equipmentId", -1L)

        if (equipmentId == -1L) {
            Toast.makeText(this, "Произошла ошибка!", Toast.LENGTH_LONG).show()
            finish()
        }

        val nameEquipmentInput: EditText = findViewById(R.id.name_equipment_input)
        val identificationNumberInput: EditText = findViewById(R.id.identification_input)
        val groupEquipmentSpinner: Spinner = findViewById(R.id.group_equipment_spinner)
        val stateEquipmentSpinner: Spinner = findViewById(R.id.state_equipment)
        val groupFrame: LinearLayout = findViewById(R.id.group_equipment)

        val listState = StateEquipment.entries.toList()
        val listGroup = (0..6).toList()

        val closeButton: ImageView = findViewById(R.id.closeButton)
        val saveChangesButton: FrameLayout = findViewById(R.id.savedChangesButton)
        val deleteEquipmentButton: FrameLayout = findViewById(R.id.deleteEquipmentButton)
        val getQrButton: FrameLayout = findViewById(R.id.getQrButton)

        createStateSpinnerDropDown(listState, stateEquipmentSpinner)
        createGroupSpinnerDropDown(listGroup, groupEquipmentSpinner)

        setData(
            database,
            equipmentId,
            nameEquipmentInput,
            identificationNumberInput,
            groupEquipmentSpinner,
            stateEquipmentSpinner,
            groupFrame
        )

        closeButton.setOnClickListener {
            Animates().animatesButton(it) {
                closeView(
                    database,
                    equipmentId,
                    nameEquipmentInput,
                    identificationNumberInput,
                    groupEquipmentSpinner,
                    stateEquipmentSpinner,
                    listGroup,
                    listState
                )
            }
        }

        saveChangesButton.setOnClickListener {
            Animates().animatesButton(it) {
                savedChanges(
                    database,
                    equipmentId,
                    nameEquipmentInput,
                    identificationNumberInput,
                    groupEquipmentSpinner,
                    stateEquipmentSpinner,
                    listGroup,
                    listState
                ) {
                    finish()
                }
            }
        }

        deleteEquipmentButton.setOnClickListener {
            Animates().animatesButton(it) {
                deleteEquipment(equipmentId)
            }
        }

        getQrButton.setOnClickListener {
            Animates().animatesButton(it) {
                generateQrAndDownload(database, equipmentId)
            }
        }

    }

    private fun generateQrAndDownload(database: AppDatabase, equipmentId: Long) {
        lifecycleScope.launch {
            val equipment = database.equipmentDao().getEquipmentById(equipmentId)

            if (equipment != null) {
                val qr: Bitmap? = generateQr("equipment:/:${equipment.identificationNumber}")

                if (qr != null) {
                    downloadQr(this@EditEquipmentActivity, qr, equipment.identificationNumber)
                } else {
                    Toast.makeText(
                        this@EditEquipmentActivity,
                        "Произошла ошибка, не получилось достать qr код!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    this@EditEquipmentActivity, "Произошла ошибка, возможно оборудование удалено!",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    private fun downloadQr(context: Context, qr: Bitmap, name: String) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "${name}_qr.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "Pictures/${context.getString(R.string.app_name)}"
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        uri?.let {
            try {
                resolver.openOutputStream(it).use { stream ->
                    if (stream != null) {
                        qr.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    }
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(it, values, null, null)
                Toast.makeText(context, "Сохранено в галерею", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                resolver.delete(it, null, null)
                Toast.makeText(context, "Ошибка при сохранении", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateQr(text: String): Bitmap? {
        return try {
            val barcodeEncoder = BarcodeEncoder()
            barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 512, 512)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun deleteEquipment(equipmentId: Long) {
        val dialog = DeleteEquipmentDialog.newInstance(equipmentId)
        dialog.show(supportFragmentManager, "CloseDialog")
    }

    private fun savedChanges(
        database: AppDatabase,
        equipmentId: Long,
        nameEquipmentInput: EditText,
        identificationNumberInput: EditText,
        groupEquipmentSpinner: Spinner,
        stateEquipmentSpinner: Spinner,
        listGroup: List<Int>,
        listState: List<StateEquipment>,
        callback: () -> Unit
    ) {
        lifecycleScope.launch {

            if (nameEquipmentInput.text.isEmpty()) {
                Toast.makeText(
                    this@EditEquipmentActivity, "Наименование не может быть пустым!",
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            if (identificationNumberInput.text.isEmpty()) {
                identificationNumberInput.error = "Номер оборудования не может быть пустой!"
                return@launch
            }

            if (database.equipmentDao()
                    .getCountOnNumberEdit(
                        identificationNumberInput.text.toString(),
                        equipmentId
                    ) > 0
            ) {
                identificationNumberInput.error = "Номер оборудования должен быть уникальным!"
                return@launch
            }

            val equipmentDb: EquipmentEntity? =
                database.equipmentDao().getEquipmentById(equipmentId)

            if (equipmentDb != null) {
                val group =
                    if (equipmentDb.equipmentType == EquipmentType.FURNITURE) listGroup[groupEquipmentSpinner.selectedItemPosition] else null

                val equipmentEdits: EquipmentEntity = getEquipment(
                    equipmentId,
                    nameEquipmentInput.text.toString(),
                    identificationNumberInput.text.toString(),
                    group,
                    listState[stateEquipmentSpinner.selectedItemPosition],
                    equipmentDb.cabinetId,
                    equipmentDb.equipmentType,
                    Date()
                )

                database.equipmentDao().updateEquipment(equipmentEdits)

                Toast.makeText(
                    this@EditEquipmentActivity, "Данные оборудования успешно изменены!",
                    Toast.LENGTH_LONG
                ).show()

                callback()
            } else {
                Toast.makeText(
                    this@EditEquipmentActivity, "Произошла ошибка или оборудование было удаленно!",
                    Toast.LENGTH_LONG
                ).show()
                callback()
            }
        }
    }

    private fun closeView(
        database: AppDatabase,
        equipmentId: Long,
        nameEquipmentInput: EditText,
        identificationNumberInput: EditText,
        groupEquipmentSpinner: Spinner,
        stateEquipmentSpinner: Spinner,
        groupList: List<Int>,
        listState: List<StateEquipment>
    ) {
        lifecycleScope.launch {
            val equipmentDb = database.equipmentDao().getEquipmentById(equipmentId)

            if (equipmentDb != null) {

                val group =
                    if (equipmentDb.equipmentType == EquipmentType.FURNITURE) groupList[groupEquipmentSpinner.selectedItemPosition] else null

                val currEquipment = getEquipment(
                    equipmentId = equipmentId,
                    name = nameEquipmentInput.text.toString(),
                    identification = identificationNumberInput.text.toString(),
                    group = group,
                    state = listState[stateEquipmentSpinner.selectedItemPosition],
                    cabinetId = equipmentDb.cabinetId,
                    equipmentType = equipmentDb.equipmentType,
                    lastDateCheck = equipmentDb.lastDateCheck
                )

                if (currEquipment != equipmentDb) {
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

    private fun getEquipment(
        equipmentId: Long,
        name: String,
        identification: String,
        group: Int?,
        state: StateEquipment,
        cabinetId: Long?,
        equipmentType: EquipmentType,
        lastDateCheck: Date
    ): EquipmentEntity {
        return EquipmentEntity(
            equipmentId = equipmentId,
            name = name,
            identificationNumber = identification,
            group = group,
            stateEquipment = state,
            cabinetId = cabinetId,
            equipmentType = equipmentType,
            lastDateCheck = lastDateCheck
        )
    }

    private fun createStateSpinnerDropDown(
        stateList: List<StateEquipment>,
        stateEquipmentSpinner: Spinner,
    ) {

        val list = stateList.map { it.nameDescription }

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

    private fun createGroupSpinnerDropDown(
        groupList: List<Int>,
        groupEquipmentSpinner: Spinner,
    ) {
        val groupEquipmentAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            groupList
        )

        groupEquipmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        groupEquipmentSpinner.adapter = groupEquipmentAdapter
    }

    private fun setData(
        database: AppDatabase,
        equipmentId: Long,
        nameInput: EditText,
        identificationInput: EditText,
        groupSpinner: Spinner,
        stateSpinner: Spinner,
        groupFrame: LinearLayout
    ) {
        lifecycleScope.launch {

            val equipment = database.equipmentDao().getEquipmentById(equipmentId)

            if (equipment != null) {
                nameInput.setText(equipment.name)
                identificationInput.setText(equipment.identificationNumber)

                if (equipment.equipmentType == EquipmentType.ELECTRONICS) {
                    groupFrame.visibility = View.GONE
                } else {
                    val adapter = groupSpinner.adapter as ArrayAdapter<Int>
                    val position = adapter.getPosition(equipment.group)
                    groupSpinner.setSelection(position)
                }

                val adapter = stateSpinner.adapter as ArrayAdapter<String>
                val position = adapter.getPosition(equipment.stateEquipment.nameDescription)
                stateSpinner.setSelection(position)
            }
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