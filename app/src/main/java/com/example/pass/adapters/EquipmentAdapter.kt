package com.example.pass.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pass.R
import com.example.pass.activities.Animates
import com.example.pass.database.equipment.EquipmentEntity
import com.example.pass.database.equipment.EquipmentWithNameCabinet
import java.text.SimpleDateFormat
import java.util.Locale

class EquipmentAdapter(
    private val onCabinet: Boolean = false,
    private val callback: (Long) -> Unit
) : ListAdapter<EquipmentWithNameCabinet, EquipmentAdapter.EquipmentViewHolder>(EquipmentDiffCallback()) {
    class EquipmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.nameEquipment)
        val type: TextView = view.findViewById(R.id.typeEquipment)
        val group: TextView = view.findViewById(R.id.groupEquipment)
        val number: TextView = view.findViewById(R.id.numberEquipment)
        val state: TextView = view.findViewById(R.id.stateEquipment)
        val cabinet: TextView = view.findViewById(R.id.cabinetEquipment)
        val date: TextView = view.findViewById(R.id.dateEquipment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_equipment, parent, false)
        return EquipmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: EquipmentViewHolder, position: Int) {
        val equipment = getItem(position)

        holder.name.text = equipment.equipment.name
        holder.group.text = holder.itemView.context.getString(
            R.string.equipment_group_text,
            if (equipment.equipment.group != null) equipment.equipment.group.toString() else "Не указанно"
        )
        holder.type.text = holder.itemView.context.getString(
            R.string.equipment_type_text,
            equipment.equipment.equipmentType.nameDescription
        )
        holder.number.text = holder.itemView.context.getString(
            R.string.equipment_number_text,
            equipment.equipment.identificationNumber
        )
        holder.state.text = holder.itemView.context.getString(
            R.string.equipment_state_text,
            equipment.equipment.stateEquipment.nameDescription
        )
        holder.cabinet.text = holder.itemView.context.getString(
            R.string.equipment_cabinet_text,
            equipment.nameCabinet ?: "Не закреплен"
        )

        val formater = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        holder.date.text = holder.itemView.context.getString(
            R.string.equipment_date_text,
            formater.format(equipment.equipment.lastDateCheck)
        )

        if (!onCabinet) {
            holder.itemView.setOnClickListener {
                Animates().animatesButton(it) {
                    callback(equipment.equipment.equipmentId)
                }
            }
        }
    }
    class EquipmentDiffCallback : DiffUtil.ItemCallback<EquipmentWithNameCabinet>() {
        override fun areItemsTheSame(oldItem: EquipmentWithNameCabinet, newItem: EquipmentWithNameCabinet): Boolean {
            return oldItem.equipment.equipmentId == newItem.equipment.equipmentId
        }

        override fun areContentsTheSame(oldItem: EquipmentWithNameCabinet, newItem: EquipmentWithNameCabinet): Boolean {
            return oldItem == newItem
        }
    }
}