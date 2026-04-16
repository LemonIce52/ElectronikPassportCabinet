package com.example.pass.database.documents

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "Document")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val documentId: Long = 0,
    val typeDocument: TypeDocument,
    val nameDocument: String,
    val description: String,
    val dateCreated: Date,
    val creatorId: Long
)