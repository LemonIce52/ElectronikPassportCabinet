package com.example.pass.database.converters

import androidx.room.TypeConverter
import com.example.pass.database.equipment.StateEquipment

class ConverterStateEquipment {

    @TypeConverter
    fun fromState(stateEquipment: StateEquipment): String {
        return stateEquipment.toString()
    }

    @TypeConverter
    fun toState(stateEquipment: String): StateEquipment {
        return StateEquipment.valueOf(stateEquipment)
    }

}