package com.example.pass.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.pass.R
import com.example.pass.database.AppDatabase
import com.example.pass.database.equipment.EquipmentEntity
import com.example.pass.database.users.UsersEntity
import com.example.pass.otherClasses.Animates
import kotlinx.coroutines.launch

class DeleteEquipmentDialog : DialogFragment() {

    companion object {
        fun newInstance(equipmentId: Long): DeleteEquipmentDialog {
            val args = Bundle()
            args.putLong("equipment_id", equipmentId) // Сохраняем данные
            val fragment = DeleteEquipmentDialog()
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_delete_equipment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val acceptButton: FrameLayout = view.findViewById(R.id.acceptDeleteButton)
        val equipmentId: Long? = arguments?.getLong("equipment_id")
        val db: AppDatabase = AppDatabase.getDatabase(view.context)

        acceptButton.setOnClickListener {

            Animates().animatesButton(it) {
                deleteEquipment(equipmentId, db)

                closeDialog()
            }
        }
    }

    private fun closeDialog() {
        dismiss()
        activity?.finish()
    }

    private fun deleteEquipment(equipmentId: Long?, db: AppDatabase) {
        if (equipmentId != null) {
            lifecycleScope.launch {
                val equipmentEntity: EquipmentEntity? =
                    db.equipmentDao().getEquipmentById(equipmentId)

                if (equipmentEntity != null) {
                    db.equipmentDao().deleteEquipment(equipmentEntity)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}