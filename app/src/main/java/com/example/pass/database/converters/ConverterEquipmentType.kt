package com.example.pass.database.converters

import androidx.room.TypeConverter
import com.example.pass.database.equipment.EquipmentType

class ConverterEquipmentType {

    @TypeConverter
    fun fromState(equipmentType: EquipmentType): String {
        return equipmentType.toString()
    }

    @TypeConverter
    fun toState(equipmentType: String): EquipmentType {
        return EquipmentType.valueOf(equipmentType)
    }

}