package com.example.pass.adapters

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pass.R
import com.example.pass.otherClasses.Animates
import com.example.pass.database.users.Role
import com.example.pass.database.users.UsersEntity

// 1. Наследуемся от ListAdapter. Убираем (val users) из конструктора!
class UserAdapter(private val callback: (Long) -> Unit) :
    ListAdapter<UsersEntity, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFullName: TextView = view.findViewById(R.id.tvFullName)
        val tvBirthday: TextView = view.findViewById(R.id.tvDate)
        val tvRole: TextView = view.findViewById(R.id.tvRole)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_sotrudnik, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        // 2. Используем getItem(position) вместо users[position]
        val user = getItem(position)

        holder.tvFullName.text = holder.itemView.context.getString(
            R.string.name_lastName_card,
            user.name,
            user.lastName
        )

        holder.tvBirthday.text = DateFormat
            .format("dd.MM.yyyy", user.birthday)
            .toString()

        holder.tvRole.text = user.role.nameRole

        holder.itemView.setOnClickListener {
            Animates().animatesButton(it) {
                callback(user.userId)
            }
        }
    }

    // 3. Добавляем DiffUtil. Он сравнивает элементы, чтобы анимация была плавной
    class UserDiffCallback : DiffUtil.ItemCallback<UsersEntity>() {
        override fun areItemsTheSame(oldItem: UsersEntity, newItem: UsersEntity): Boolean {
            return oldItem.userId == newItem.userId // Сравнение по ID
        }

        override fun areContentsTheSame(oldItem: UsersEntity, newItem: UsersEntity): Boolean {
            return oldItem == newItem // Сравнение всех полей данных
        }
    }
}
