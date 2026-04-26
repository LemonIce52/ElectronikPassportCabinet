package com.example.pass.dialog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.pass.R
import com.example.pass.activities.CabinetActivity
import com.example.pass.activities.InventScanActivity
import com.example.pass.database.AppDatabase
import com.example.pass.otherClasses.Animates
import kotlinx.coroutines.launch

class CartCabinetScanningDialog : DialogFragment()  {

    companion object {
        fun newInstance(cabinetId: Long): CartCabinetScanningDialog {
            val args = Bundle()
            args.putLong("cabinetId", cabinetId)
            val fragment = CartCabinetScanningDialog()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_cabinet_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val openCardCabinetButton: FrameLayout = view.findViewById(R.id.openCardCabinet)
        val startInventButton: FrameLayout = view.findViewById(R.id.startInvent)

        val cabinetId: Long? = arguments?.getLong("cabinetId")
        val db: AppDatabase = AppDatabase.getDatabase(view.context)

        lifecycleScope.launch {
            if (cabinetId != null) {
                val cabinetEntity = db.cabinetDao().getCabinetById(cabinetId)
                if (cabinetEntity != null)
                    view.findViewById<TextView>(R.id.nameCabinet).text = cabinetEntity.name
            }
        }

        openCardCabinetButton.setOnClickListener {
            Animates().animatesButton(it) {
                startCabinetActivity(cabinetId)
            }
        }

        startInventButton.setOnClickListener {
            Animates().animatesButton(it) {
                startInvent(cabinetId)
            }
        }
    }

    private fun startCabinetActivity(cabinetId: Long?) {
        if (cabinetId != null) {
            val intent = Intent(requireContext(), CabinetActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            intent.putExtra("cabinetId", cabinetId)
            startActivity(intent)
        }

        dismiss()
    }

    fun startInvent(cabinetId: Long?) {
        if (cabinetId != null) {
            val intent = Intent(requireContext(), InventScanActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            intent.putExtra("cabinetId", cabinetId)
            startActivity(intent)
        }

        dismiss()
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