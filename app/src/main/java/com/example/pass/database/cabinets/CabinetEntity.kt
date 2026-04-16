package com.example.pass.database.cabinets

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Cabinet")
data class CabinetEntity(
    @PrimaryKey(autoGenerate = true) val cabinetId: Long = 0,
    val name: String,
    val typeCabinet: TypeCabinet,
    val height: Int,
    val width: Int,
    val length: Int
)