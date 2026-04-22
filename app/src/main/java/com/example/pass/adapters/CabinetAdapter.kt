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
import com.example.pass.database.cabinets.CabinetEntity

class CabinetAdapter(private val callback: (Long) -> Unit) : ListAdapter<CabinetEntity, CabinetAdapter.CabinetViewHolder>(CabinetDiffCallback()) {

    class CabinetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cabinetName: TextView = view.findViewById(R.id.cabinetId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CabinetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cabinet, parent, false)
        return CabinetViewHolder(view)
    }

    override fun onBindViewHolder(holder: CabinetViewHolder, position: Int) {
        val cabinet = getItem(position)

        holder.cabinetName.text = cabinet.name

        holder.itemView.setOnClickListener {
            Animates().animatesButton(it) {
                callback(cabinet.cabinetId)
            }
        }
    }
    class CabinetDiffCallback : DiffUtil.ItemCallback<CabinetEntity>() {
        override fun areItemsTheSame(oldItem: CabinetEntity, newItem: CabinetEntity): Boolean {
            return oldItem.cabinetId == newItem.cabinetId
        }

        override fun areContentsTheSame(oldItem: CabinetEntity, newItem: CabinetEntity): Boolean {
            return oldItem == newItem
        }
    }
}
