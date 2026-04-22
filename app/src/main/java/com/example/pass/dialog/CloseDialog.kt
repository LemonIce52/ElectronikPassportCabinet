package com.example.pass.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import com.example.pass.R
import com.example.pass.otherClasses.Animates

class CloseDialog : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_window_close, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val acceptButton: FrameLayout = view.findViewById(R.id.acceptButton)

        acceptButton.setOnClickListener {
            Animates().animatesButton(it) {
                dismiss()
                activity?.finish()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}