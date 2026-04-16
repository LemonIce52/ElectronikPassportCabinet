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
import com.example.pass.database.documents.DocumentEntity
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class DocumentAdapter(private val callback: (String, String, String) -> Unit) :
    ListAdapter<DocumentEntity, DocumentAdapter.DocumentViewHolder>(DocumentDiffCallback()) {

    class DocumentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val documentName: TextView = view.findViewById(R.id.docName)
        val documentDateCreated: TextView = view.findViewById(R.id.docDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_document, parent, false)
        return DocumentViewHolder(view)
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        val document = getItem(position)
        val formater = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val date = formater.format(document.dateCreated)

        holder.documentName.text = document.typeDocument.nameDocument
        holder.documentDateCreated.text = holder.itemView.context.getString(
            R.string.date_created,
            date
        )

        holder.itemView.setOnClickListener {
            Animates().animatesButton(it) {
                callback(document.description, document.typeDocument.nameDocument, date)
            }
        }
    }

    class DocumentDiffCallback : DiffUtil.ItemCallback<DocumentEntity>() {
        override fun areItemsTheSame(oldItem: DocumentEntity, newItem: DocumentEntity): Boolean {
            return oldItem.documentId == newItem.documentId
        }

        override fun areContentsTheSame(oldItem: DocumentEntity, newItem: DocumentEntity): Boolean {
            return oldItem == newItem
        }
    }
}
