package com.example.pass.dialog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.pass.R
import com.example.pass.activities.CabinetActivity
import com.example.pass.activities.EditEquipmentActivity
import com.example.pass.database.AppDatabase
import com.example.pass.database.equipment.StateEquipment
import com.example.pass.otherClasses.Animates
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CardEquipmentDialog : DialogFragment() {

    private val firstElementSpinner: String = "Выберите состояние обр-ния"

    companion object {
        fun newInstance(equipmentId: Long, isInventor: Boolean): CardEquipmentDialog {
            val args = Bundle()
            args.putLong("equipmentId", equipmentId) // Сохраняем данные
            args.putBoolean("isInvent", isInventor)
            val fragment = CardEquipmentDialog()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_equipment_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val savedChangesButton: FrameLayout = view.findViewById(R.id.savedChangesButton)
        val editDataButton: FrameLayout = view.findViewById(R.id.editDataButton)
        val spinnerState: Spinner = view.findViewById(R.id.state_equipment)

        val equipmentId: Long? = arguments?.getLong("equipmentId")
        val isInvent: Boolean = arguments?.getBoolean("isInvent") ?: false
        val db: AppDatabase = AppDatabase.getDatabase(view.context)
        val stateList = StateEquipment.entries.toList()

        if (isInvent) {
            editDataButton.visibility = View.GONE
        } else {
            editDataButton.visibility = View.VISIBLE
        }

        lifecycleScope.launch {
            if (equipmentId != null) {
                val equipmentEntity = db.equipmentDao().getEquipmentById(equipmentId)
                if (equipmentEntity != null) {
                    val formater = SimpleDateFormat("dd.MM.yyyy", Locale.US)

                    view.findViewById<TextView>(R.id.nameEquipment).text = equipmentEntity.name
                    view.findViewById<TextView>(R.id.dateEquipment).text = view.context.getString(
                        R.string.equipment_date_text,
                        formater.format(equipmentEntity.lastDateCheck)
                    )
                    view.findViewById<TextView>(R.id.stateEquipment).text = view.context.getString(
                        R.string.equipment_state_text,
                        equipmentEntity.stateEquipment.nameDescription
                    )
                    view.findViewById<TextView>(R.id.numberEquipment).text = view.context.getString(
                        R.string.equipment_number_text,
                        equipmentEntity.identificationNumber
                    )
                }
            }
        }

        setSpinner(spinnerState, view.context, stateList)

        editDataButton.setOnClickListener {
            Animates().animatesButton(it) {
                if (equipmentId != null) startEquipmentEditActivity(equipmentId)
                dismiss()
            }
        }

        savedChangesButton.setOnClickListener {
            Animates().animatesButton(it) {
                if (equipmentId != null) savedChanges(db, equipmentId, view, spinnerState, stateList)
                else dismiss()
            }
        }
    }


    fun savedChanges(
        db: AppDatabase,
        equipmentId: Long,
        view: View,
        spinnerState: Spinner,
        stateList: List<StateEquipment>
    ) {

        if (spinnerState.selectedItem.equals(firstElementSpinner)) {
            Toast.makeText(view.context, "Требуется выбрать состояние", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            db.equipmentDao().updateStateAndLastDateCheckById(
                equipmentId, stateList[spinnerState.selectedItemPosition - 1],
                Date()
            )
        }

        dismiss()
    }

    private fun startEquipmentEditActivity(equipmentId: Long) {
        val intent = Intent(requireContext(), EditEquipmentActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        intent.putExtra("equipmentId", equipmentId)
        startActivity(intent)
    }

    private fun setSpinner(
        stateEquipmentSpinner: Spinner,
        context: Context,
        stateList: List<StateEquipment>
    ) {
        val list = mutableListOf(firstElementSpinner)
        stateList.forEach { list.add(it.nameDescription) }

        val stateEquipmentAdapter = object : ArrayAdapter<String>(
            context,
            android.R.layout.simple_spinner_item,
            list
        ) {
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                return if (position == 0 || position == 1) {
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

    override fun onStart() {
        super.onStart()

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        parentFragmentManager.setFragmentResult("dialogClosed", androidx.core.os.bundleOf())
    }
}