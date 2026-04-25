package com.example.pass.database.documents

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.pass.database.cabinets.CabinetEntity
import com.example.pass.database.users.UsersEntity
import java.util.Date

@Entity(tableName = "Document",
    foreignKeys = [
        ForeignKey(
            entity = UsersEntity::class,
            parentColumns = ["userId"],
            childColumns = ["creatorId"],
            onDelete = ForeignKey.SET_NULL
        )
    ])
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val documentId: Long = 0,
    val typeDocument: TypeDocument,
    val nameDocument: String,
    val description: String,
    val dateCreated: Date,
    val creatorId: Long
)