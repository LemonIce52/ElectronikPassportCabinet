package com.example.pass.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.compose.ui.platform.isDebugInspectorInfoEnabled
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pass.R
import com.example.pass.otherClasses.Animates
import com.example.pass.otherClasses.CheckedEquipment
import java.text.SimpleDateFormat
import java.util.Locale

class AssigningAndUnpinAdapter : ListAdapter<CheckedEquipment, AssigningAndUnpinAdapter.CheckedEquipmentViewHolder>(CheckedEquipmentDiffCallback()) {
    class CheckedEquipmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.name)
        val stateText: TextView = view.findViewById(R.id.state)
        val identificationText: TextView = view.findViewById(R.id.identification)
        val checkBox: CheckBox = view.findViewById(R.id.checked)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckedEquipmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_equipment_chek_and_unchek, parent, false)
        return CheckedEquipmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CheckedEquipmentViewHolder, position: Int) {
        val checkedEquipment = getItem(position)

        holder.nameText.text = checkedEquipment.equipment.name
        holder.stateText.text = holder.itemView.context.getString(
            R.string.equipment_state_text,
            checkedEquipment.equipment.stateEquipment.nameDescription
        )
        holder.identificationText.text = holder.itemView.context.getString(
            R.string.equipment_number_text,
            checkedEquipment.equipment.identificationNumber
        )

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = checkedEquipment.isChecked

        holder.itemView.setOnClickListener {
            Animates().animatesButton(it) {
                checkedEquipment.isChecked = !checkedEquipment.isChecked
                holder.checkBox.isChecked = checkedEquipment.isChecked
            }
        }

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            checkedEquipment.isChecked = isChecked
        }
    }
    class CheckedEquipmentDiffCallback : DiffUtil.ItemCallback<CheckedEquipment>() {
        override fun areItemsTheSame(oldItem: CheckedEquipment, newItem: CheckedEquipment): Boolean {
            return oldItem.equipment.equipmentId == newItem.equipment.equipmentId
        }

        override fun areContentsTheSame(oldItem: CheckedEquipment, newItem: CheckedEquipment): Boolean {
            return oldItem == newItem
        }
    }
}