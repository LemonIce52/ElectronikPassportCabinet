package com.example.pass.dialog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.pass.R
import com.example.pass.activities.SpecialistActivity
import com.example.pass.database.AppDatabase
import com.example.pass.database.cabinets.CabinetEntity
import com.example.pass.otherClasses.Animates
import kotlinx.coroutines.launch

class DeleteCabinetDialog : DialogFragment() {

    companion object {
        fun newInstance(cabinetId: Long): DeleteCabinetDialog {
            val args = Bundle()
            args.putLong("cabinetId", cabinetId) // Сохраняем данные
            val fragment = DeleteCabinetDialog()
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
        val cabinetId: Long? = arguments?.getLong("cabinetId")
        val db: AppDatabase = AppDatabase.getDatabase(view.context)

        acceptButton.setOnClickListener {
            Animates().animatesButton(it) {
                deleteCabinet(cabinetId, db)
                closeDialog()
            }
        }
    }

    private fun closeDialog() {
        val intent = Intent(requireContext(), SpecialistActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        startActivity(intent)
        dismiss()
    }

    private fun deleteCabinet(cabinetId: Long?, db: AppDatabase) {
        if (cabinetId != null) {
            lifecycleScope.launch {
                val cabinetEntity: CabinetEntity? = db.cabinetDao().getCabinetById(cabinetId)

                if (cabinetEntity != null) {
                    db.cabinetDao().deleteCabinet(cabinetEntity)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

}