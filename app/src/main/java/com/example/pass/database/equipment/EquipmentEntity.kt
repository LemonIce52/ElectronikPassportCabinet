package com.example.pass.database.equipment

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.pass.database.cabinets.CabinetEntity
import java.util.Date

@Entity(tableName = "Equipment",
    foreignKeys = [
        ForeignKey(
            entity = CabinetEntity::class,
            parentColumns = ["cabinetId"],
            childColumns = ["cabinetId"],
            onDelete = ForeignKey.SET_NULL
        )
    ])
data class EquipmentEntity(
    @PrimaryKey(autoGenerate = true) val equipmentId: Long = 0,
    val identificationNumber: String,
    val name: String,
    val cabinetId: Long?,
    val group: Int?,
    val stateEquipment: StateEquipment,
    val equipmentType: EquipmentType,
    val lastDateCheck: Date
)