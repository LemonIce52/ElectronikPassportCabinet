package com.example.pass.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pass.R
import com.example.pass.activities.Animates
import com.example.pass.activities.Buys

class BuysAdapter(
    private val onDeleteClick: (Int) -> Unit // Передаем позицию для удаления
) : ListAdapter<Buys, BuysAdapter.BuyViewHolder>(BuyDiffCallback()) {

    class BuyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.nameText)
        val countText: TextView = view.findViewById(R.id.countText)
        val priceText: TextView = view.findViewById(R.id.priceText)
        val deleteBtn: ImageButton = view.findViewById(R.id.deleteItemButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_add_can_buy, parent, false)
        return BuyViewHolder(view)
    }

    override fun onBindViewHolder(holder: BuyViewHolder, position: Int) {
        val item = getItem(position)
        holder.nameText.text = item.name
        holder.countText.text = holder.itemView.context.getString(R.string.count_text, item.count)
        holder.priceText.text = holder.itemView.context.getString(R.string.price_text, item.price)

        holder.deleteBtn.setOnClickListener {
            Animates().animatesButton(it) {
                onDeleteClick(holder.bindingAdapterPosition)
            }
        }
    }

    class BuyDiffCallback : DiffUtil.ItemCallback<Buys>() {
        override fun areItemsTheSame(oldItem: Buys, newItem: Buys): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Buys, newItem: Buys): Boolean {
            return oldItem == newItem
        }
    }
}