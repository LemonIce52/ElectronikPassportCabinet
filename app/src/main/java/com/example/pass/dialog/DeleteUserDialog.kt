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
import com.example.pass.database.users.UsersEntity
import kotlinx.coroutines.launch

class DeleteUserDialog : DialogFragment() {

    companion object {
        fun newInstance(userId: Long): DeleteUserDialog {
            val args = Bundle()
            args.putLong("user_id", userId) // Сохраняем данные
            val fragment = DeleteUserDialog()
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_delete_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val acceptButton: FrameLayout = view.findViewById(R.id.acceptDeleteButton)
        val userId: Long? = arguments?.getLong("user_id")
        val db: AppDatabase = AppDatabase.getDatabase(view.context)

        acceptButton.setOnClickListener {
            it.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()

                    if (userId != null) {
                        lifecycleScope.launch {
                            val userEntity: UsersEntity? = db.usersDao().getUserOnId(userId)

                            if (userEntity != null) {
                                db.usersDao().deleteUser(userEntity)
                            }
                        }
                    }

                    dismiss()
                    activity?.finish()
                }
                .start()
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}